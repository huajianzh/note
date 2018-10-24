package com.aji.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.TextureView;

/**
 * Created by Administrator on 2018\10\23 0023.
 */

public class CameraCustomPreview extends TextureView {

    private int mRatioWidth = 0;
    private int mRatioHeight = 0;

    public CameraCustomPreview(Context context) {
        this(context, null);
    }

    public CameraCustomPreview(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraCustomPreview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Sets the aspect ratio for this view. The size of the view will be measured based on the ratio
     * calculated from the parameters. Note that the actual sizes of parameters don't matter, that
     * is, calling setAspectRatio(2, 3) and setAspectRatio(4, 6) make the same result.
     *
     * @param width  Relative horizontal size
     * @param height Relative vertical size
     */
    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        matrix.reset();
        //图片镜像并旋转90度
        matrix.setScale(-1, 1);
        matrix.postTranslate(width, 0);
        matrix.postRotate(90, width / 2, height / 2);
//        matrix.postTranslate(0, (bitmap.getWidth() - bitmap.getHeight()) / 2);
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
            }
        }
    }

    Matrix matrix = new Matrix();

    public void drawFrame(Bitmap bitmap) {
        Canvas canvas = lockCanvas();
        canvas.drawBitmap(bitmap, 0, 0, null);
//        canvas.drawBitmap(bitmap, matrix, null);
        unlockCanvasAndPost(canvas);
        bitmap.recycle();
    }
}
