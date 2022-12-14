package com.example.ass2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "ScaleFactorValue";
    private GridView imageList;
    private ArrayList<String> imageAddress;// address of photo stored inside
    private final ExecutorService e1 = Executors.newSingleThreadScheduledExecutor();
    private ScaleGestureDetector scaleGestureDetector;
    private final int imageNum = 4;

    private ArrayList<String> getImageAddress() {

        ArrayList<String> temp = new ArrayList<String>();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        try (Cursor cursor = getContentResolver().query(uri, null, null, null,
                MediaStore.Images.Media.DATE_ADDED +" DESC")) {// follow the date to sort the image, just as what
            if (cursor == null || cursor.getCount() <= 0) return null; // found not image
            while (cursor.moveToNext())// found image
            {
                int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                String path = cursor.getString(index); // get file address
                temp.add(path);
            }
        }
        return temp;
    }

    public class TileAdapter extends BaseAdapter{
        class ViewHolder {
            int position;
            ImageView image;
        }
        @Override
        public int getCount() {
            return imageAddress.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }


        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder vh;
            if (view == null){
                view = getLayoutInflater().inflate(R.layout.image, viewGroup, false);
                vh = new ViewHolder();
                vh.image = view.findViewById(R.id.imageView);
                view.setTag(vh);
            }else{
                vh = (ViewHolder) view.getTag();
            }

            vh.position = i;
            vh.image.setImageBitmap(null);

            e1.submit(()->{
                if(vh.position != i){
                    return;
                }
                final Bitmap bmp;
                bmp = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(imageAddress.get(vh.position)),400, 400);

                if(vh.position == i){
                    vh.image.post(()->vh.image.setImageBitmap(bmp));
                }
            });
            return view;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
        };
        requestPermissions(permissions, 200);
        imageList = (GridView)findViewById(R.id.images);
        imageAddress = getImageAddress();
        TileAdapter tileAdapter = new TileAdapter();
        imageList.setNumColumns(imageNum);
        imageList.setAdapter(tileAdapter);
        imageList.setOnItemClickListener((parent, view, position, id)->{//actually we only need position
            Intent intent = new Intent(this, Full_Image.class);
            intent.putExtra("address", imageAddress.get(position));//send address to fullImage activity
            this.startActivity(intent);
        });
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.OnScaleGestureListener() {
            float temp = imageNum;
            @Override
            public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
                try{
                    temp = temp / scaleGestureDetector.getScaleFactor();//always return the x times of scale factor
                }catch (ArithmeticException e){
                    Log.w(TAG, "Zero:" + scaleGestureDetector.getScaleFactor());
                }

                if(temp < 1){
                    temp = 1;// at least one image per column
                }else if(temp > 8){
                    temp = 8;// at most 8 images per column
                }
                imageList.setNumColumns(((int)temp));
                Log.i(TAG, "scale = " + scaleGestureDetector.getScaleFactor());
                //Log.i(TAG, "num = " + temp);
                return true;//was false, set to true to reset the scale factor
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {

                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {

            }
        });
        imageList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                scaleGestureDetector.onTouchEvent(motionEvent);
                return false;
            }
        });
    }

//    @Override
//    protected void onSaveInstanceState(Bundle savedInstanceState) {
//        super.onSaveInstanceState(savedInstanceState);
//        //savedInstanceState.putString("PHOTO_DETAIL_PATH", mPath);
//    }
}