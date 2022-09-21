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
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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

    TileAdapter tileAdapter;
    ListView imageList;
    ArrayList<String> imageAddress;// address of photo stored inside


    private ArrayList<String> getImageAddress() {
        ArrayList<String> temp = new ArrayList<String>();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
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


    private ExecutorService e1 = Executors.newSingleThreadScheduledExecutor();
    public class TileAdapter extends BaseAdapter{
        class ViewHolder {
            int position;
            ImageView image;
        }
        @Override
        public int getCount() {
            return 100;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @SuppressLint({"StaticFieldLeak", "SetTextI18n"})
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
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 10;
                bmp = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(imageAddress.get(vh.position), options),400, 400);

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
        imageList = findViewById(R.id.images);
        imageAddress = getImageAddress();
        tileAdapter = new TileAdapter();

        imageList.setAdapter(tileAdapter);
        imageList.setOnItemClickListener((parent, view, position, id)->{//actually we only need position
            Intent intent = new Intent(this, Full_Image.class);
            intent.putExtra("address", imageAddress.get(position));//send address to fullImage activity
            this.startActivity(intent);
        });
    }


}