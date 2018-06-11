
package com.yzq.zxinglibrary.view;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.google.zxing.ResultPoint;
import com.yzq.zxinglibrary.R;
import com.yzq.zxinglibrary.bean.ZxingConfig;
import com.yzq.zxinglibrary.camera.CameraManager;

import java.util.ArrayList;
import java.util.List;

public final class ViewfinderView extends View {

    /*界面刷新间隔时间*/
    private static final long ANIMATION_DELAY = 80L;
    private static final int CURRENT_POINT_OPACITY = 0xA0;
    private static final int MAX_RESULT_POINTS = 20;
    private static final int POINT_SIZE = 6;
    private final View scanLineView;

    private CameraManager cameraManager;
    private final Paint paint;
    private Bitmap resultBitmap;
    private int maskColor; // 取景框外的背景颜色
    private int resultColor;// result Bitmap的颜色
    private int resultPointColor; // 特征点的颜色
    private int statusColor; // 提示文字颜色
    private int reactColor;//四个角的颜色


    private List<ResultPoint> possibleResultPoints;
    private List<ResultPoint> lastPossibleResultPoints;
    // 扫描线移动的y
    private int scanLineTop;
    // 扫描线移动速度
    private int SCAN_VELOCITY = 20;
    //扫描线高度
    private int scanLightHeight = 20;
    // 扫描线
    Bitmap scanLight;
    private ZxingConfig config;


    public ViewfinderView(Context context) {
        this(context, null);

    }

    public ViewfinderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);


    }


    public void setZxingConfig(ZxingConfig config) {

        this.config = config;
        reactColor = ContextCompat.getColor(getContext(), config.getReactColor());
    }


    public ViewfinderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);


        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Resources resources = getResources();
        maskColor = ContextCompat.getColor(getContext(), R.color.viewfinder_mask);
        resultColor = ContextCompat.getColor(getContext(), R.color.result_view);
        resultPointColor = ContextCompat.getColor(getContext(), R.color.possible_result_points);
        statusColor = ContextCompat.getColor(getContext(), R.color.status_text);

        possibleResultPoints = new ArrayList<ResultPoint>(10);
        lastPossibleResultPoints = null;
        scanLight = BitmapFactory.decodeResource(resources, R.drawable.scan_light);



        scanLineView=new View(context);

    }

    public void setCameraManager(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
    }

    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {
        if (cameraManager == null) {
            return; // not ready yet, early draw before done configuring
        }

        // frame为取景框
        Rect frame = cameraManager.getFramingRect();
        Rect previewFrame = cameraManager.getFramingRectInPreview();
        if (frame == null || previewFrame == null) {
            return;
        }
        int width = canvas.getWidth();
        int height = canvas.getHeight();


        scanLineView.setLayoutParams(new ViewGroup.LayoutParams(frame.width(),10));
        scanLineView.setBackgroundColor(reactColor);


        /*绘制遮罩*/
        drawMaskView(canvas, frame, width, height);

        /*绘制取景框边框*/
        drawFrameBounds(canvas, frame);



        if (resultBitmap != null) {
            // Draw the opaque result bitmap over the scanning rectangle
            // 如果有二维码结果的Bitmap，在扫取景框内绘制不透明的result Bitmap
            paint.setAlpha(CURRENT_POINT_OPACITY);
            canvas.drawBitmap(resultBitmap, null, frame, paint);
        } else {


            /*绘制提示文字*/
            //  drawStatusText(canvas, frame, width);

            /*绘制扫描线*/
           // drawScanLight(canvas, frame);

            /*绘制闪动的点*/
            drawPoint(canvas, frame, previewFrame);
        }
    }

    private void drawPoint(Canvas canvas, Rect frame, Rect previewFrame) {
        float scaleX = frame.width() / (float) previewFrame.width();
        float scaleY = frame.height() / (float) previewFrame.height();

        // 绘制扫描线周围的特征点
        List<ResultPoint> currentPossible = possibleResultPoints;
        List<ResultPoint> currentLast = lastPossibleResultPoints;
        int frameLeft = frame.left;
        int frameTop = frame.top;
        if (currentPossible.isEmpty()) {
            lastPossibleResultPoints = null;
        } else {
            possibleResultPoints = new ArrayList<ResultPoint>(5);
            lastPossibleResultPoints = currentPossible;
            paint.setAlpha(CURRENT_POINT_OPACITY);
            paint.setColor(resultPointColor);
            synchronized (currentPossible) {
                for (ResultPoint point : currentPossible) {
                    canvas.drawCircle(frameLeft
                                    + (int) (point.getX() * scaleX), frameTop
                                    + (int) (point.getY() * scaleY), POINT_SIZE,
                            paint);
                }
            }
        }
        if (currentLast != null) {
            paint.setAlpha(CURRENT_POINT_OPACITY / 2);
            paint.setColor(resultPointColor);
            synchronized (currentLast) {
                float radius = POINT_SIZE / 2.0f;
                for (ResultPoint point : currentLast) {
                    canvas.drawCircle(frameLeft
                            + (int) (point.getX() * scaleX), frameTop
                            + (int) (point.getY() * scaleY), radius, paint);
                }
            }
        }

        // Request another update at the animation interval, but only
        // repaint the laser line,
        // not the entire viewfinder mask.
        postInvalidateDelayed(ANIMATION_DELAY, frame.left - POINT_SIZE,
                frame.top - POINT_SIZE, frame.right + POINT_SIZE,
                frame.bottom + POINT_SIZE);
    }

    private void drawMaskView(Canvas canvas, Rect frame, int width, int height) {
        // Draw the exterior (i.e. outside the framing rect) darkened
        // 绘制取景框外的暗灰色的表面，分四个矩形绘制
        paint.setColor(resultBitmap != null ? resultColor : maskColor);
        /*上面的框*/
        canvas.drawRect(0, 0, width, frame.top, paint);
        /*绘制左边的框*/
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        /*绘制右边的框*/
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1,
                paint);
        /*绘制下面的框*/
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);
    }


    /**
     * 绘制取景框边框
     *
     * @param canvas
     * @param frame
     */
    private void drawFrameBounds(Canvas canvas, Rect frame) {

        /*扫描框的边框线*/
        if (config.getFrameLineColor() != -1) {
            paint.setColor(ContextCompat.getColor(getContext(), config.getFrameLineColor()));
            paint.setStrokeWidth(2);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(frame, paint);

        }

        /*扫描框的四个角*/
        paint.setColor(reactColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        /*四个角的长度和宽度*/
        int width = frame.width();
        int corLength = (int) (width * 0.07);
        int corWidth = (int) (corLength * 0.2);

        corWidth = corWidth > 15 ? 15 : corWidth;


        /*角在线外*/
        // 左上角
        canvas.drawRect(frame.left - corWidth, frame.top, frame.left, frame.top
                + corLength, paint);
        canvas.drawRect(frame.left - corWidth, frame.top - corWidth, frame.left
                + corLength, frame.top, paint);
        // 右上角
        canvas.drawRect(frame.right, frame.top, frame.right + corWidth,
                frame.top + corLength, paint);
        canvas.drawRect(frame.right - corLength, frame.top - corWidth,
                frame.right + corWidth, frame.top, paint);
        // 左下角
        canvas.drawRect(frame.left - corWidth, frame.bottom - corLength,
                frame.left, frame.bottom, paint);
        canvas.drawRect(frame.left - corWidth, frame.bottom, frame.left
                + corLength, frame.bottom + corWidth, paint);
        // 右下角
        canvas.drawRect(frame.right, frame.bottom - corLength, frame.right
                + corWidth, frame.bottom, paint);
        canvas.drawRect(frame.right - corLength, frame.bottom, frame.right
                + corWidth, frame.bottom + corWidth, paint);
    }

    /**
     * 绘制提示文字
     *
     * @param canvas
     * @param frame
     * @param width
     */
    private void drawStatusText(Canvas canvas, Rect frame, int width) {

        // Log.i("绘制提示文字", "width:" + width);

        String statusText1 = getResources().getString(
                R.string.viewfinderview_status_text1);
        String statusText2 = getResources().getString(
                R.string.viewfinderview_status_text2);

        int statusTextSize;

        /*低分辨率处理*/
        if (width >= 480 && width <= 600) {
            statusTextSize = 22;
        } else if (width > 600 && width <= 720) {
            statusTextSize = 26;
        } else {
            statusTextSize = 45;
        }

        int statusPaddingTop = 180;

        paint.setColor(statusColor);
        paint.setTextSize(statusTextSize);

        int textWidth1 = (int) paint.measureText(statusText1);
        canvas.drawText(statusText1, (width - textWidth1) / 2, frame.top
                - statusPaddingTop, paint);

        int textWidth2 = (int) paint.measureText(statusText2);
        canvas.drawText(statusText2, (width - textWidth2) / 2, frame.top
                - statusPaddingTop + 60, paint);
    }


    /**
     * 绘制移动扫描线
     *
     * @param canvas
     * @param frame
     */
    private void drawScanLight(Canvas canvas, Rect frame) {

        if (scanLineTop == 0 || scanLineTop + SCAN_VELOCITY >= frame.bottom) {
            scanLineTop = frame.top;
        } else {

            /*缓动动画*/
//            SCAN_VELOCITY = (frame.bottom - scanLineTop) / 12;
//            SCAN_VELOCITY = (int) (SCAN_VELOCITY > 10 ? Math.ceil(SCAN_VELOCITY) : 10);
            scanLineTop += SCAN_VELOCITY;
        }

//        Rect scanRect = new Rect(frame.left, scanLineTop, frame.right,
//                scanLineTop + scanLightHeight);
//        canvas.drawBitmap(scanLight, null, scanRect, paint);


        paint.setColor(reactColor);
        paint.setStrokeWidth(8);
        canvas.drawLine(frame.left, scanLineTop, frame.right, scanLineTop, paint);


    }

    public void drawViewfinder() {
        Bitmap resultBitmap = this.resultBitmap;
        this.resultBitmap = null;
        if (resultBitmap != null) {
            resultBitmap.recycle();
        }
        invalidate();
    }

    /**
     * Draw a bitmap with the result points highlighted instead of the live
     * scanning display.
     *
     * @param barcode An image of the decoded barcode.
     */
    public void drawResultBitmap(Bitmap barcode) {
        resultBitmap = barcode;
        invalidate();
    }

    public void addPossibleResultPoint(ResultPoint point) {
        List<ResultPoint> points = possibleResultPoints;
        synchronized (points) {
            points.add(point);
            int size = points.size();
            if (size > MAX_RESULT_POINTS) {
                // trim it
                points.subList(0, size - MAX_RESULT_POINTS / 2).clear();
            }
        }
    }


}
