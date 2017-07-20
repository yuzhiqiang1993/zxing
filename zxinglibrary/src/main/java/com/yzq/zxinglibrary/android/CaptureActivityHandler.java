/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yzq.zxinglibrary.android;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Browser;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.yzq.zxinglibrary.Consants;
import com.yzq.zxinglibrary.camera.CameraManager;
import com.yzq.zxinglibrary.decode.DecodeThread;
import com.yzq.zxinglibrary.view.ViewfinderResultPointCallback;


import java.util.Collection;
import java.util.Map;

/**
 * This class handles all the messaging which comprises the state machine for
 * capture. 该类用于处理有关拍摄状态的所有信息
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CaptureActivityHandler extends Handler {

    private static final String TAG = CaptureActivityHandler.class
            .getSimpleName();

    private final CaptureActivity activity;
    private final DecodeThread decodeThread;
    private State state;
    private final CameraManager cameraManager;

    private enum State {
        PREVIEW, SUCCESS, DONE
    }

    public CaptureActivityHandler(CaptureActivity activity,
                                  Collection<BarcodeFormat> decodeFormats,
                                  Map<DecodeHintType, ?> baseHints, String characterSet,
                                  CameraManager cameraManager) {
        this.activity = activity;
        decodeThread = new DecodeThread(activity, decodeFormats, baseHints,
                characterSet, new ViewfinderResultPointCallback(
                activity.getViewfinderView()));
        decodeThread.start();
        state = State.SUCCESS;

        // Start ourselves capturing previews and decoding.
        // 开始拍摄预览和解码
        this.cameraManager = cameraManager;
        cameraManager.startPreview();
        restartPreviewAndDecode();
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case Consants.RESTART_PREVIEW:
                // 重新预览
                restartPreviewAndDecode();
                break;
            case Consants.DECODE_SUCCEEDED:
                // 解码成功
                state = State.SUCCESS;
                Bundle bundle = message.getData();
                Bitmap barcode = null;
                float scaleFactor = 1.0f;
                if (bundle != null) {
                    byte[] compressedBitmap = bundle
                            .getByteArray(DecodeThread.BARCODE_BITMAP);
                    if (compressedBitmap != null) {
                        barcode = BitmapFactory.decodeByteArray(compressedBitmap,
                                0, compressedBitmap.length, null);
                        // Mutable copy:
                        barcode = barcode.copy(Bitmap.Config.ARGB_8888, true);
                    }
                    scaleFactor = bundle
                            .getFloat(DecodeThread.BARCODE_SCALED_FACTOR);
                }
                activity.handleDecode((Result) message.obj, barcode, scaleFactor);
                break;
            case Consants.DECODE_FAILED:
                // We're decoding as fast as possible, so when one decode fails,
                // start another.
                // 尽可能快的解码，以便可以在解码失败时，开始另一次解码
                state = State.PREVIEW;
                cameraManager.requestPreviewFrame(decodeThread.getHandler(),
                        Consants.DECODE);
                break;
            case Consants.RETURN_SCAN_RESULT:
                //扫描结果，返回CaptureActivity处理
                activity.setResult(Activity.RESULT_OK, (Intent) message.obj);
                activity.finish();
                break;
            case Consants.LAUNCH_PRODUCT_QUERY:
                String url = (String) message.obj;

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                intent.setData(Uri.parse(url));

                ResolveInfo resolveInfo = activity.getPackageManager()
                        .resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
                String browserPackageName = null;
                if (resolveInfo != null && resolveInfo.activityInfo != null) {
                    browserPackageName = resolveInfo.activityInfo.packageName;
                    Log.d(TAG, "Using browser in package " + browserPackageName);
                }

                // Needed for default Android browser / Chrome only apparently
                //需要默认的Android浏览器或者Google
                if ("com.android.browser".equals(browserPackageName)
                        || "com.android.chrome".equals(browserPackageName)) {
                    intent.setPackage(browserPackageName);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(Browser.EXTRA_APPLICATION_ID,
                            browserPackageName);
                }

                try {
                    activity.startActivity(intent);
                } catch (ActivityNotFoundException ignored) {
                    Log.w(TAG, "Can't find anything to handle VIEW of URI " + url);
                }
                break;
        }
    }

    /**
     * 完全退出
     */
    public void quitSynchronously() {
        state = State.DONE;
        cameraManager.stopPreview();
        Message quit = Message.obtain(decodeThread.getHandler(), Consants.QUIT);
        quit.sendToTarget();
        try {
            // Wait at most half a second; should be enough time, and onPause()
            // will timeout quickly
            decodeThread.join(500L);
        } catch (InterruptedException e) {
            // continue
        }

        // Be absolutely sure we don't send any queued up messages
        //确保不会发送任何队列消息
        removeMessages(Consants.DECODE_SUCCEEDED);
        removeMessages(Consants.DECODE_FAILED);
    }

    public void restartPreviewAndDecode() {
        if (state == State.SUCCESS) {
            state = State.PREVIEW;
            cameraManager.requestPreviewFrame(decodeThread.getHandler(),
                    Consants.DECODE);
            activity.drawViewfinder();
        }
    }

}
