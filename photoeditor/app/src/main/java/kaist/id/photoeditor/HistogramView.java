package kaist.id.photoeditor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Sangyeob on 11/29/2016.
 */

class HistogramView extends View {

    private int viewWidth;
    private int viewHeight;

    private Bitmap image;
    private Bitmap scaledImage;

    public HistogramView (Context context) {
        super(context);
        setWillNotDraw(false);
        image = null;
    }

    public HistogramView (Context context, AttributeSet attrs) {
        super(context, attrs);
        image = null;
    }

    public HistogramView (Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        image = null;
    }

    public void setImage(Bitmap image) {
        Log.d("HistogramView", "Image Set");
        this.image = image;
        scaledImage = Bitmap.createScaledBitmap(image, viewWidth, viewHeight, true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = viewWidth * 100 / 255;
        super.onMeasure(MeasureSpec.makeMeasureSpec(viewWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(viewHeight, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d("HistogramView", "onDrawCalled");
        super.onDraw(canvas);
        Paint p = new Paint();
        p.setColor(getResources().getColor(R.color.colorDarkerBackground));
        p.setStyle(Paint.Style.FILL);
        canvas.drawPaint(p);
        if(scaledImage != null) {
            Paint aa = new Paint();
            aa.setAntiAlias(true);
            aa.setFilterBitmap(true);
            aa.setDither(true);
            canvas.drawBitmap(scaledImage, 1, 1, aa);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }
}
