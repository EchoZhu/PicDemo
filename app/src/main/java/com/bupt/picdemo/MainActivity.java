package com.bupt.picdemo;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.SaveCallback;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private SimpleDraweeView my_image_view;
    private Button btn_takephoto, btn_chosepic;
    private static final int REQUEST_CODE_CAPTURE_CAMEIA = 0x01;
    private static final int REQUEST_CODE_PICK_IMAGE = 0x02;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        setContentView(R.layout.activity_main);
        AVOSCloud.initialize(this, "48X5CbKCMh8qdAutJpkFNTr7-gzGzoHsz", "kbhPFjfDTyXknMlD9MFuXsC0");
        my_image_view = (SimpleDraweeView) findViewById(R.id.my_image_view);
        btn_takephoto = (Button) findViewById(R.id.button);
        btn_chosepic = (Button) findViewById(R.id.button2);

        btn_takephoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImageFromCamera();
            }

            private void getImageFromCamera() {
                String state = Environment.getExternalStorageState();
                if (state.equals(Environment.MEDIA_MOUNTED)) {
                    Intent getImageByCamera = new Intent("android.media.action.IMAGE_CAPTURE");
                    startActivityForResult(getImageByCamera, REQUEST_CODE_CAPTURE_CAMEIA);
                } else {
                    Toast.makeText(getApplicationContext(), "请确认已经插入SD卡", Toast.LENGTH_LONG).show();

                }
            }
        });
        btn_chosepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImageFromAlbum();
            }

            private void getImageFromAlbum() {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");//相片类型
                startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CAPTURE_CAMEIA) {
            Uri uri = data.getData();
//            Log.d("uri", uri.toString());
            if (uri == null) {
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    Bitmap photo = (Bitmap) bundle.get("data"); //get bitmap
                    String path = saveImage(photo);

                    if (null != path) {
                        try {
                            String fileName = System.currentTimeMillis() + "jpg";
                            final AVFile file = AVFile.withAbsoluteLocalPath(fileName, path);
                            file.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(AVException e) {
                                    // 成功或失败处理...

                                    if (e == null) {
                                        String url = file.getUrl().toString();
                                        Uri uri = Uri.parse(url);
                                        my_image_view.setImageURI(uri);
                                        Toast.makeText(MainActivity.this, "上传成功", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(MainActivity.this, "上传失败", Toast.LENGTH_LONG).show();

                                    }

                                }
                            });

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                    }
                } else {
                    Toast.makeText(getApplicationContext(), "err****", Toast.LENGTH_LONG).show();
                }

            }
        } else if (requestCode == REQUEST_CODE_PICK_IMAGE) {
            Uri uri = data.getData();
//            Log.e("123",uri.getPath());
            ContentResolver resolver = getContentResolver();
            try {
                Bitmap bm = MediaStore.Images.Media.getBitmap(resolver,uri);
                //获取图片路径
                String[] proj = {MediaStore.Images.Media.DATA};
                Cursor cursor = managedQuery(uri,proj,null,null,null);
                int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                //根据索引值获取图片
                String path = cursor.getString(index);
                String fileName = System.currentTimeMillis() + "jpg";
                final AVFile file = AVFile.withAbsoluteLocalPath(fileName, path);
                file.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        // 成功或失败处理...

                        if (e == null) {
                            String url = file.getUrl().toString();
                            Uri uri = Uri.parse(url);
                            my_image_view.setImageURI(uri);
                            Toast.makeText(MainActivity.this, "上传成功", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this, "上传失败", Toast.LENGTH_LONG).show();

                        }

                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    private String saveImage(Bitmap photo) {

        String fileName = System.currentTimeMillis() + ".jpg";
        String path = Environment.getExternalStorageDirectory() + "/" + fileName;
        try {
            BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(path, false));
            photo.compress(Bitmap.CompressFormat.JPEG, 80, bos);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return path;
    }
}
