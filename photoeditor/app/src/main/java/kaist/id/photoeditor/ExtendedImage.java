package kaist.id.photoeditor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;

import java.io.InputStream;

public class ExtendedImage {
    private int width;
    private int height;

    public int getWidth() { return this.width; }
    public int getHeight() { return this.height; }

    private Bitmap bitmap;
    private Bitmap histogram;

    public Bitmap makeHistogram() {
        Bitmap res = Bitmap.createBitmap(256, 101, Bitmap.Config.ARGB_8888);
        int[] histogram_r = new int[256];
        int[] histogram_g = new int[256];
        int[] histogram_b = new int[256];
        int max = 0;

        for(int x = 0; x < width; x ++) {
            for(int y = 0; y < height; y ++) {
                int pixel = bitmap.getPixel(x, y);
                histogram_r[Color.red(pixel)] ++;
                histogram_g[Color.green(pixel)] ++;
                histogram_b[Color.blue(pixel)] ++;
            }
        }

        for(int i = 0; i < 256; i ++) {
            if(histogram_r[i] > max)
                max = histogram_r[i];
            if(histogram_g[i] > max)
                max = histogram_g[i];
            if(histogram_b[i] > max)
                max = histogram_b[i];
        }

        Log.d("debug", "" + max);

        for(int i = 0; i < 256; i ++) {
            histogram_r[i] = histogram_r[i] * 100 / max;
            histogram_g[i] = histogram_g[i] * 100 / max;
            histogram_b[i] = histogram_b[i] * 100 / max;

            for(int j = 0; j <= 100; j ++) {
                if(j > 100 - histogram_r[i] && j > 100 - histogram_g[i] && j > 100 - histogram_b[i]) {
                    res.setPixel(i, j, 0x88000000);
                } else if(j > 100 - histogram_r[i] || j > 100 - histogram_g[i] || j > 100 - histogram_b[i]){
                    res.setPixel(i, j, Color.argb(0x88,
                                                  j > 100 - histogram_r[i] ? 0xff : 0x00,
                                                  j > 100 - histogram_g[i] ? 0xff : 0x00,
                                                  j > 100 - histogram_b[i] ? 0xff : 0x00));
                }
            }
        }
        return res;
    }

    public Bitmap getBitmap() {
        //return bitmap;
        return histogram;
    }

    public ExtendedImage (InputStream inputStream) {
        bitmap = BitmapFactory.decodeStream(inputStream);
        this.width = bitmap.getWidth();
        this.height = bitmap.getHeight();
        histogram = makeHistogram();
    }
}
