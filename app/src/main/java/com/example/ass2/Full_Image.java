package com.example.ass2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Full_Image extends AppCompatActivity {

    String address;
    ImageView image;

    private ExecutorService e1 = Executors.newSingleThreadScheduledExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);
        Intent intent = getIntent();
        address = intent.getStringExtra("address");
        image = findViewById(R.id.imageView2);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Full_Image.this.finish();
            }
        }
        );
        e1.submit(()->{
            final Bitmap bmp;
            BitmapFactory.Options options = new BitmapFactory.Options();
            bmp = BitmapFactory.decodeFile(address);
            image.post(()->image.setImageBitmap(bmp));
        });

    }
}