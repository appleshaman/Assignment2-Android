package com.example.ass2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Full_Image extends AppCompatActivity {
    private static final String TAG = "ImageScaleValue";

    String address;

    ImageView image;
    private ScaleGestureDetector scaleGestureDetector;
    private ExecutorService e1 = Executors.newSingleThreadScheduledExecutor();

    private int mode = 0;//0: initial state, 1: drag, 2: zoom
    private final int drag = 1;
    private final int zoom = 2;


    private Matrix matrix = new Matrix();
    private PointF start = new PointF();
    private Matrix currentMatrix = new Matrix();

    private long dragTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);
        Intent intent = getIntent();
        address = intent.getStringExtra("address");
        image = findViewById(R.id.imageView2);
        image.setScaleType(ImageView.ScaleType.CENTER);// can not set to matrix first, otherwise the picture will not appear at center
        matrix.set(image.getImageMatrix());
//        image.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Full_Image.this.finish();
//            }
//        });
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.OnScaleGestureListener() {

            @Override
            public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
                matrix.set(image.getImageMatrix());
                matrix.postScale(scaleGestureDetector.getScaleFactor(), scaleGestureDetector.getScaleFactor(),
                        scaleGestureDetector.getFocusX(), scaleGestureDetector.getFocusY());
                image.setScaleType(ImageView.ScaleType.MATRIX);
                image.setImageMatrix(matrix);
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {

            }
        });
        image.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                float dx = 0;
                float dy = 0;
                switch (motionEvent.getAction() & motionEvent.getActionMasked()){
                    case MotionEvent.ACTION_DOWN:
                        mode = drag;
                        Log.i(TAG, "drag");
                        currentMatrix.set(image.getImageMatrix());
                        start.set(motionEvent.getX(), motionEvent.getY());
                        dragTime = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        mode = zoom;// necessary because two finger move will also make the ACTION_DOWN happens
                        Log.i(TAG, "zoom");
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if(mode == drag){
                            Log.i(TAG, "move");
                            dx = motionEvent.getX() - start.x;
                            dy = motionEvent.getY() - start.y;
                            image.setScaleType(ImageView.ScaleType.MATRIX);
                            matrix.set(currentMatrix);
                            matrix.postTranslate(dx, dy);
                            image.setImageMatrix(matrix);
                        }
                        break;
                }
                if((mode == drag)&&(motionEvent.getAction() == MotionEvent.ACTION_UP)){
                    if((System.currentTimeMillis() - dragTime) < 150){// if the touch time less than 150ms, consider it is is a single click
                        Full_Image.this.finish();
                    }

                }
                scaleGestureDetector.onTouchEvent(motionEvent);
                return true;
            }
        });
        e1.submit(() -> {
            final Bitmap bmp;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            bmp = BitmapFactory.decodeFile(address, options);
            image.post(() -> image.setImageBitmap(bmp));



        });

    }
    public int calculateInSampleSize(BitmapFactory.Options options,int reqWidth,int reqHeight)
    {
        int inSampleSize = 1;
        int rawWidth = options.outWidth;
        int rawHeight = options.outHeight;
        while ((rawWidth / inSampleSize >= reqWidth) && (rawHeight / inSampleSize >= reqHeight))
        {
            inSampleSize = inSampleSize *2;
        }

        return inSampleSize;
    }

}