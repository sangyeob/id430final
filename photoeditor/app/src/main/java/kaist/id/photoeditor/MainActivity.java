package kaist.id.photoeditor;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private PhotoView pvMain;
    private Button btnLoadImage;

    private static final int PICK_IMAGE = 1;

    private void loadImageForPhotoView() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent.createChooser(intent, "Select Picture to Edit"), PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
            pvMain.setImage(new ExtendedImage(inputStream));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pvMain = (PhotoView)findViewById(R.id.pvMain);
        btnLoadImage = (Button)findViewById(R.id.btnLoadImage);

        btnLoadImage.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadImageForPhotoView();
            }
        });
    }
}
