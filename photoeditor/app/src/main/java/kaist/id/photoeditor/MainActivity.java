package kaist.id.photoeditor;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DecimalFormat;

class MainActivity extends AppCompatActivity {
    private PhotoView pvMain;
    private HistogramView hvMain;
    private Button btnLoadImage;

    private static final int PICK_IMAGE = 1;

    private Handler seekBarHandler = new Handler();

    class Triplet<A, B, C> {
        public final A a;
        public final B b;
        public final C c;

        public Triplet (A a, B b, C c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }
    }

    private Triplet<Integer, Integer, PhotoValues>[] standardSeekBars;

    private class standardSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        private int idx;
        public standardSeekBarChangeListener (int idx) {
            this.idx = idx;
        }
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            ((TextView)findViewById(standardSeekBars[idx].b)).setText("" + (seekBar.getProgress() - 100));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            ((TextView)findViewById(standardSeekBars[idx].b)).setText("" + (seekBar.getProgress() - 100));
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            ((TextView)findViewById(standardSeekBars[idx].b)).setText("" + (seekBar.getProgress() - 100));
            if(pvMain.getImage() != null)
                pvMain.getImage().changePhotoValue(standardSeekBars[idx].c, seekBar.getProgress() - 100);
        }
    }

    private void initializeSeekBars() {
        seekBarHandler.post(new Runnable() {
            @Override
            public void run() {
                ((SeekBar)findViewById(R.id.seekBarEv)).setProgress(42);
                ((TextView)findViewById(R.id.tvEvValue)).setText("0");
                for(int i = 0; i < standardSeekBars.length; i ++) {
                    ((SeekBar)findViewById(standardSeekBars[i].a)).setProgress(100);
                    ((TextView)findViewById(standardSeekBars[i].b)).setText("0");
                }
            }
        });
    }

    private void loadImageForPhotoView() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent.createChooser(intent, "Select Picture to Edit"), PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("MyPerfectLog", "ON ACTIVITY RESULT !!!");
        initializeSeekBars();
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if(data == null) return;
            InputStream inputStream = null;
            try {
                inputStream = getApplicationContext().getContentResolver().openInputStream(data.getData());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            }
            pvMain.setImage(new ExtendedImage(inputStream, this, pvMain, hvMain));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        standardSeekBars = new Triplet[] { // R.id.seekBarEv excluded
            new Triplet<>(R.id.seekBarBlack, R.id.tvBlackValue, PhotoValues.BLACK),
            new Triplet<>(R.id.seekBarClarity, R.id.tvClarityValue, PhotoValues.CLARITY),
            new Triplet<>(R.id.seekBarCont, R.id.tvContValue, PhotoValues.CONTRAST),
            new Triplet<>(R.id.seekBarHl, R.id.tvHlValue, PhotoValues.HIGHLIGHT),
            new Triplet<>(R.id.seekBarSat, R.id.tvSatValue, PhotoValues.SATURATION),
            new Triplet<>(R.id.seekBarShad, R.id.tvShadValue, PhotoValues.SHADOW),
            new Triplet<>(R.id.seekBarVib, R.id.tvVibValue, PhotoValues.VIBRANCE),
            new Triplet<>(R.id.seekBarWhite, R.id.tvWhiteValue, PhotoValues.WHITE),
        };

        pvMain = (PhotoView)findViewById(R.id.pvMain);
        hvMain = (HistogramView)findViewById(R.id.hvMain);
        btnLoadImage = (Button)findViewById(R.id.btnLoadImage);

        ((SeekBar)findViewById(R.id.seekBarEv)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                float f = (i - 42) * 5 / 42f;
                ((TextView)findViewById(R.id.tvEvValue)).setText((new DecimalFormat("#.##").format(f)));
                /*
                if(pvMain.getImage() != null)
                    pvMain.getImage().changePhotoValue(PhotoValues.EXPOSURE, f);
                */
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                float f = (seekBar.getProgress() - 42) * 5 / 42f;
                ((TextView)findViewById(R.id.tvEvValue)).setText((new DecimalFormat("#.##").format(f)));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float f = (seekBar.getProgress() - 42) * 5 / 42f;
                ((TextView)findViewById(R.id.tvEvValue)).setText((new DecimalFormat("#.##").format(f)));
                if(pvMain.getImage() != null)
                    pvMain.getImage().changePhotoValue(PhotoValues.EXPOSURE, f);
            }
        });

        for(int i = 0; i < standardSeekBars.length; i ++) {
            ((SeekBar)findViewById(standardSeekBars[i].a)).setOnSeekBarChangeListener(new standardSeekBarChangeListener(i));
        }

        btnLoadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadImageForPhotoView();
            }
        });
        pvMain.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pvMain.getImage() == null) {
                    loadImageForPhotoView();
                }
            }
        });
    }
}
