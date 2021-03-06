package kaist.id.photoeditor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

/**
 * Created by Sangyeob on 11/29/2016.
 */

class PhotoView extends View {

    private int viewWidth;
    private int viewHeight;

    private ExtendedImage image;

    private ScaleGestureDetector gd;

    public PhotoView (Context context) {
        super(context);
        setWillNotDraw(false);
        image = null;
    }

    public PhotoView (Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        image = null;
    }

    public PhotoView (Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWillNotDraw(false);
        image = null;
    }

    public void setImage(ExtendedImage image) {
            this.image = image;
            this.image.setResizedSize(viewWidth, viewHeight);
        }
    public ExtendedImage getImage() { return this.image; }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);
        viewWidth = viewHeight * 4 / 3;
        super.onMeasure(MeasureSpec.makeMeasureSpec(viewWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(heightMeasureSpec, MeasureSpec.EXACTLY));
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
        Log.d("PhotoView", "onDrawCalled");
        super.onDraw(canvas);
        Paint p = new Paint();
        p.setColor(getResources().getColor(R.color.colorDarkerBackground));
        p.setStyle(Paint.Style.FILL);
        canvas.drawPaint(p);
        if(image != null) {
            Log.d("PhotoView", "resizingImage");
            Bitmap bitmap = image.makeResizedBitmapOnce(viewWidth, viewHeight);
            if(bitmap.getHeight() * 4 / 3 < bitmap.getWidth()) {
                canvas.drawBitmap(bitmap, 0, (viewHeight - viewWidth * image.getBitmap().getHeight() / image.getBitmap().getWidth()) / 2, null);
            } else {
                canvas.drawBitmap(bitmap, (viewWidth - viewHeight * image.getBitmap().getWidth() / image.getBitmap().getHeight()) / 2, 0, null);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }
}
