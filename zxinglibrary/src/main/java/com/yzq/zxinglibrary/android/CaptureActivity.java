package com.yzq.zxinglibrary.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.yzq.zxinglibrary.R;
import com.yzq.zxinglibrary.bean.ZxingConfig;
import com.yzq.zxinglibrary.camera.CameraManager;
import com.yzq.zxinglibrary.common.Constant;
import com.yzq.zxinglibrary.decode.DecodeImgCallback;
import com.yzq.zxinglibrary.decode.DecodeImgThread;
import com.yzq.zxinglibrary.decode.ImageUtil;
import com.yzq.zxinglibrary.view.ViewfinderView;

import java.io.IOException;


public class CaptureActivity extends Activity implements SurfaceHolder.Callback, View.OnClickListener {

    private static final String TAG = CaptureActivity.class.getSimpleName();
    private ZxingConfig config;
    private SurfaceView preview_view;
    //private Toolbar toolbar;
    private ViewfinderView viewfinder_view;
    private AppCompatImageView flashLightIv;
    private TextView flashLightTv;
    private LinearLayout flashLightLayout;
    private LinearLayout albumLayout;
    private LinearLayoutCompat bottomLayout;
    private boolean hasSurface;
    private InactivityTimer inactivityTimer;
    private BeepManager beepManager;
    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private SurfaceHolder surfaceHolder;


    public ViewfinderView getViewfinderView() {
        return viewfinder_view;
    }

    public Handler getHandler() {
        return handler;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    public void drawViewfinder() {
        viewfinder_view.drawViewfinder();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 保持Activity处于唤醒状态
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_capture);

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        /*先获取配置信息*/
        try {
            config = (ZxingConfig) getIntent().getExtras().get(Constant.INTENT_ZXING_CONFIG);
        } catch (Exception e) {
            config = new ZxingConfig();
        }




        initView();


        hasSurface = false;

        inactivityTimer = new InactivityTimer(this);
        beepManager = new BeepManager(this);
        beepManager.setPlayBeep(config.isPlayBeep());
        beepManager.setVibrate(config.isShake());


    }


    private void initView() {
        preview_view = (SurfaceView) findViewById(R.id.preview_view);
        preview_view.setOnClickListener(this);
        //toolbar = (Toolbar) findViewById(R.id.toolbar);

        viewfinder_view = (ViewfinderView) findViewById(R.id.viewfinder_view);
        viewfinder_view.setOnClickListener(this);
        flashLightIv = (AppCompatImageView) findViewById(R.id.flashLightIv);
        flashLightTv = (TextView) findViewById(R.id.flashLightTv);
        flashLightLayout = (LinearLayout) findViewById(R.id.flashLightLayout);
        flashLightLayout.setOnClickListener(this);
        albumLayout = (LinearLayout) findViewById(R.id.albumLayout);
        albumLayout.setOnClickListener(this);
        bottomLayout = (LinearLayoutCompat) findViewById(R.id.bottomLayout);



        switchVisibility(bottomLayout, config.isShowbottomLayout());
        switchVisibility(flashLightLayout, config.isShowFlashLight());
        switchVisibility(albumLayout, config.isShowAlbum());
//
//        toolbar.setTitle("扫一扫");
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


            /*有闪光灯就显示手电筒按钮  否则不显示*/
        if (isSupportCameraLedFlash(getPackageManager())) {
            flashLightLayout.setVisibility(View.VISIBLE);
        } else {
            flashLightLayout.setVisibility(View.GONE);
        }

    }

    /*判断设备是否支持闪光灯*/
    public static boolean isSupportCameraLedFlash(PackageManager pm) {
        if (pm != null) {
            FeatureInfo[] features = pm.getSystemAvailableFeatures();
            if (features != null) {
                for (FeatureInfo f : features) {
                    if (f != null && PackageManager.FEATURE_CAMERA_FLASH.equals(f.name))
                        return true;
                }
            }
        }
        return false;
    }

        /*切换手电筒图片*/

    public void switchFlashImg(int flashState) {

        if (flashState == Constant.FLASH_OPEN) {
            flashLightIv.setImageResource(R.drawable.ic_open);
            flashLightTv.setText("关闭闪光灯");
        } else {
            flashLightIv.setImageResource(R.drawable.ic_close);
            flashLightTv.setText("打开闪光灯");
        }

    }

    /**
     * 扫描成功，处理反馈信息
     *
     * @param rawResult
     */
    public void handleDecode(Result rawResult) {

        inactivityTimer.onActivity();
        beepManager.playBeepSoundAndVibrate();

        Intent intent = getIntent();
        intent.putExtra(Constant.CODED_CONTENT, rawResult.getText());
        //      intent.putExtra(Constant.CODED_BITMAP, barcode);
        setResult(RESULT_OK, intent);
        this.finish();


    }


    /*切换view的显示*/
    private void switchVisibility(View view, boolean b) {
        if (b) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        cameraManager = new CameraManager(getApplication());

        viewfinder_view.setCameraManager(cameraManager);
        handler = null;

        surfaceHolder = preview_view.getHolder();
        if (hasSurface) {

            initCamera(surfaceHolder);
        } else {
            // 重置callback，等待surfaceCreated()来初始化camera
            surfaceHolder.addCallback(this);
        }

        beepManager.updatePrefs();
        inactivityTimer.onResume();

    }

    /**
     * 初始化Camera
     *
     * @param surfaceHolder
     */
    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            return;
        }
        try {
            // 打开Camera硬件设备
            cameraManager.openDriver(surfaceHolder);
            // 创建一个handler来打开预览，并抛出一个运行时异常
            if (handler == null) {
                handler = new CaptureActivityHandler(this, cameraManager);
            }
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            Log.w(TAG, "Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }
    }

    /**
     * 显示错误信息
     */
    private void displayFrameworkBugMessageAndExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("扫一扫");
        builder.setMessage(getString(R.string.msg_camera_framework_bug));
        builder.setPositiveButton(R.string.button_ok, new FinishListener(this));
        builder.setOnCancelListener(new FinishListener(this));
        builder.show();
    }

    @Override
    protected void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        beepManager.close();
        cameraManager.closeDriver();

        if (!hasSurface) {

            surfaceHolder.removeCallback(this);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    /*点击事件*/
    @Override
    public void onClick(View view) {

        int id = view.getId();
        if (id == R.id.flashLightLayout) {
            /*切换闪光灯*/
            cameraManager.switchFlashLight(handler);
        } else if (id == R.id.albumLayout) {
            /*打开相册*/
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, Constant.REQUEST_IMAGE);
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constant.REQUEST_IMAGE && resultCode == RESULT_OK) {
            String path = ImageUtil.getImageAbsolutePath(this, data.getData());

            new DecodeImgThread(path, new DecodeImgCallback() {
                @Override
                public void onImageDecodeSuccess(Result result) {
                    handleDecode(result);
                }

                @Override
                public void onImageDecodeFailed() {
                    Toast.makeText(CaptureActivity.this, "抱歉，解析失败,换个图片试试.", Toast.LENGTH_SHORT).show();
                }
            }).run();


        }
    }


}
