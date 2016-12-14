package kaist.id.photoeditor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Handler;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicConvolve3x3;
import android.util.Log;

import java.io.InputStream;

class ExtendedImage {
    private int resizedBitmapWidthMax = 400;
    private int resizedBitmapHeightMax = 400;

    private int width;
    private int height;

    private float pvBlack;
    private float pvClarity;
    private float pvContrast;
    private float pvHighlight;
    private float pvSaturation;
    private float pvShadow;
    private float pvWhite;
    private float pvVibrance;
    private float pvExposure;

    private ColorMatrix finalMatrix;

    private Context appContext;
    private PhotoView pv;
    private HistogramView hv;

    public int getWidth() { return this.width; }
    public int getHeight() { return this.height; }

    private Handler viewUpdateHandler = new Handler();
    private Thread photoEditingThread;

    private Bitmap bitmap;
    Bitmap getBitmap() {
        return bitmap;
    }

    private Bitmap resizedBitmap;
    public Bitmap getResizedBitmap() {
        return resizedBitmap;
    }
    private void setResizedBitmap(Bitmap resizedBitmap) { this.resizedBitmap = resizedBitmap; }

    private Bitmap editedResizedBitmap;
    public Bitmap getEditedResizedBitmap() { return editedResizedBitmap; }
    public void setEditedResizedBitmap(Bitmap editedResizedBitmap) { this.editedResizedBitmap = editedResizedBitmap; }

    private void resizeBitmap() {
        if(bitmap.getHeight() * 4 / 3 < bitmap.getWidth()) {
            setResizedBitmap(Bitmap.createScaledBitmap(this.bitmap, resizedBitmapWidthMax, this.bitmap.getHeight() * resizedBitmapWidthMax / this.bitmap.getWidth(), false));
        } else {
            setResizedBitmap(Bitmap.createScaledBitmap(this.bitmap, this.bitmap.getWidth() * resizedBitmapHeightMax / this.bitmap.getHeight(), resizedBitmapHeightMax, false));
        }
        editedResizedBitmap = resizedBitmap;
        hv.setImage(makeHistogram());
        hv.invalidate();
        Log.d("resizedBitmapWidthMax", "" + this.resizedBitmap.getWidth());
        Log.d("resizedBitmapHeightMax", "" + this.resizedBitmap.getHeight());
    }

    Bitmap makeResizedBitmapOnce(int wmax, int hmax) {
        if(bitmap.getHeight() * 4 / 3 < bitmap.getWidth()) {
            return Bitmap.createScaledBitmap(editedResizedBitmap, wmax, this.bitmap.getHeight() * wmax / editedResizedBitmap.getWidth(), false);
        } else {
            return Bitmap.createScaledBitmap(editedResizedBitmap, editedResizedBitmap.getWidth() * hmax / editedResizedBitmap.getHeight(), hmax, false);
        }
    }

    void setResizedSize(int w, int h) {
        //this.resizedBitmapWidthMax = w;
        //this.resizedBitmapHeightMax = viewUpdateHandler;
        this.resizeBitmap();
    }

    private Bitmap makeHistogram() {
        Bitmap histogram = Bitmap.createBitmap(256, 101, Bitmap.Config.ARGB_8888);
        int[] histogram_r = new int[256];
        int[] histogram_g = new int[256];
        int[] histogram_b = new int[256];
        int max = 0;
        int pixel;
        for(int x = 0; x < editedResizedBitmap.getWidth(); x ++) {
            for(int y = 0; y < editedResizedBitmap.getHeight(); y ++) {
                pixel = editedResizedBitmap.getPixel(x, y);
                histogram_r[Color.red(pixel)] ++;
                histogram_g[Color.green(pixel)] ++;
                histogram_b[Color.blue(pixel)] ++;
            }
        }
        /*
        int min;
        for(int i = 0; i < 256; i ++) {
            min = 2147483647;
            if(histogram_r[i] < min)
                min = histogram_r[i];
            if(histogram_g[i] < min)
                min = histogram_g[i];
            if(histogram_b[i] < min)
                min = histogram_b[i];
            if(min > max) max = min;
        }
        */
        max = editedResizedBitmap.getWidth() * editedResizedBitmap.getHeight() / 50;

        for(int i = 0; i < 256; i ++) {
            histogram_r[i] = histogram_r[i] * 100 / max;
            histogram_g[i] = histogram_g[i] * 100 / max;
            histogram_b[i] = histogram_b[i] * 100 / max;

            for(int j = 0; j <= 100; j ++) {
                if(j > 100 - histogram_r[i] && j > 100 - histogram_g[i] && j > 100 - histogram_b[i]) {
                    histogram.setPixel(i, j, 0x88000000);
                } else if(j > 100 - histogram_r[i] || j > 100 - histogram_g[i] || j > 100 - histogram_b[i]){
                    histogram.setPixel(i, j, Color.argb(0x88,
                                                  j > 100 - histogram_r[i] ? 0xff : 0x00,
                                                  j > 100 - histogram_g[i] ? 0xff : 0x00,
                                                  j > 100 - histogram_b[i] ? 0xff : 0x00));
                }
            }
        }
        return histogram;
    }

    // Reference : https://chiuki.github.io/android-shaders-filters/
    private Bitmap convolve(Bitmap original, float[] coefficients) {
        Bitmap bitmap = Bitmap.createBitmap(
                original.getWidth(), original.getHeight(),
                Bitmap.Config.ARGB_8888);

        RenderScript rs = RenderScript.create(appContext);

        Allocation allocIn = Allocation.createFromBitmap(rs, original);
        Allocation allocOut = Allocation.createFromBitmap(rs, bitmap);

        ScriptIntrinsicConvolve3x3 convolution
                = ScriptIntrinsicConvolve3x3.create(rs, Element.U8_4(rs));
        convolution.setInput(allocIn);
        convolution.setCoefficients(coefficients);
        convolution.forEach(allocOut);

        allocOut.copyTo(bitmap);
        rs.destroy();
        return bitmap;
    }

    private int safeValue(float f) {
        return f > 255 ? 255 : (f < 0 ? 0 : (int)f);
    }

    private int clarity(float ori) {
        float scale = -(4 * (ori - 128) * (ori - 128)) / (10 * 128 * 128) + 0.5f;
        scale *= pvClarity;
        scale += 1f;
        return safeValue(ori * scale + (-.5f * scale + .5f) * 255f);
    }

    private void updateEditedResizedBitmap() {
        Bitmap ret = Bitmap.createBitmap(resizedBitmap.getWidth(), resizedBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(ret);

        finalMatrix = new ColorMatrix();
        finalMatrix.reset();

        float scale = pvContrast + 1f;
        float translate = (-.5f * scale + .5f) * 255f;
        finalMatrix.postConcat(new ColorMatrix(new float[] {
                scale, 0, 0, 0, translate,
                0, scale, 0, 0, translate,
                0, 0, scale, 0, translate,
                0, 0, 0, 1, 0
        }));

        float R = 0.213f * (1 - pvSaturation);
        float G = 0.715f * (1 - pvSaturation);
        float B = 0.072f * (1 - pvSaturation);

        finalMatrix.postConcat(new ColorMatrix(new float[] {
                R + pvSaturation, G, B, 0, 0,
                R, G + pvSaturation, B, 0, 0,
                R, G, B + pvSaturation, 0, 0,
                0, 0, 0, 1, 0

        }));

        R = 0.299f * (1 - pvVibrance);
        G = 0.587f * (1 - pvVibrance);
        B = 0.114f * (1 - pvVibrance);

        finalMatrix.postConcat(new ColorMatrix(new float[] {
                R + pvVibrance, G, B, 0, 0,
                R, G + pvVibrance, B, 0, 0,
                R, G, B + pvVibrance, 0, 0,
                0, 0, 0, 1, 0

        }));

        finalMatrix.postConcat(new ColorMatrix(new float[] {
                1, 0, 0, 0, -pvClarity * 20,
                0, 1, 0, 0, -pvClarity * 20,
                0, 0, 1, 0, -pvClarity * 20,
                0, 0, 0, 1, 0
        }));

        Paint p = new Paint();
        p.setColorFilter(new ColorMatrixColorFilter(finalMatrix));
        c.drawBitmap(resizedBitmap, 0, 0, p);

        ret = convolve(ret, new float[] {
                0, -1 * pvClarity / 5f, 0,
                -1 * pvClarity / 5f, 1 + 5 * pvClarity / 5f, -1 * pvClarity / 5f,
                0, -1 * pvClarity / 5f, 0
        });


        for(int i = 0; i < ret.getWidth(); i ++) {
            for(int j = 0; j < ret.getHeight(); j ++) {
                int pixel = ret.getPixel(i, j);
                ret.setPixel(i, j, Color.argb(Color.alpha(pixel),
                        clarity(Color.red(pixel)),
                        clarity(Color.green(pixel)),
                        clarity(Color.blue(pixel))));
            }
        }

        editedResizedBitmap = ret;
    }

    void changePhotoValue(PhotoValues key, float value) {
        if(key == PhotoValues.BLACK)
            this.pvBlack = (int)value;
        if(key == PhotoValues.CLARITY)
            this.pvClarity = value / 100f;
        if(key == PhotoValues.CONTRAST)
            this.pvContrast = value / 100f;
        if(key == PhotoValues.EXPOSURE)
            this.pvExposure = (float)Math.pow(2, value / 5f);
        if(key == PhotoValues.HIGHLIGHT)
            this.pvHighlight = value / 20f;
        if(key == PhotoValues.SATURATION)
            this.pvSaturation = 1f + value / 100f;
        if(key == PhotoValues.SHADOW)
            this.pvShadow = (int)value;
        if(key == PhotoValues.VIBRANCE)
            this.pvVibrance = 1f + value / 200f;
        if(key == PhotoValues.WHITE)
            this.pvWhite = (int)value;

        if(photoEditingThread != null) {
            if(photoEditingThread.isAlive()) {
                photoEditingThread.interrupt();
            }
        }
        photoEditingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                updateEditedResizedBitmap();
                viewUpdateHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        pv.invalidate();
                        hv.setImage(makeHistogram());
                        hv.invalidate();
                    }
                });
            }
        });
        photoEditingThread.start();
    }

    ExtendedImage (InputStream inputStream, Context c, PhotoView pv, HistogramView hv) {
        bitmap = BitmapFactory.decodeStream(inputStream);
        this.appContext = c;
        this.pv = pv;
        this.hv = hv;
        this.width = bitmap.getWidth();
        this.height = bitmap.getHeight();
        pvExposure = 1f ;
        pvSaturation = 1f;
        pvVibrance = 1f;
        editedResizedBitmap = resizedBitmap = bitmap;
    }
}
