package com.ruimeng.things.shop;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

import androidx.loader.content.CursorLoader;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.zhihu.matisse.engine.ImageEngine;

public class PostGlideEngine implements ImageEngine {

    @Override
    public void loadThumbnail(Context context, int resize, Drawable placeholder, ImageView imageView, Uri uri) {
        RequestOptions options = new RequestOptions()
                .placeholder(placeholder)
                .override(resize, resize)
                .centerCrop();
        Glide.with(context)
                .asBitmap()
                .load(uri)
                .apply(options)
                .into(imageView);
    }

    @Override
    public void loadAnimatedGifThumbnail(Context context, int resize, Drawable placeholder, ImageView imageView,
                                         Uri uri) {

        RequestOptions options = new RequestOptions()
                .placeholder(placeholder)
                .override(resize, resize)
                .centerCrop();
        Glide.with(context)
                .asBitmap()
                .load(uri)
                .apply(options)
                .into(imageView);
    }

    @Override
    public void loadImage(Context context, int resizeX, int resizeY, ImageView imageView, Uri uri) {
        RequestOptions options = new RequestOptions()
                .override(resizeX, resizeY)
                .priority(Priority.HIGH);
        Glide.with(context)
                .asBitmap()
                .load(uri)
                .apply(options)
                .into(imageView);
    }

    @Override
    public void loadAnimatedGifImage(Context context, int resizeX, int resizeY, ImageView imageView, Uri uri) {
        RequestOptions options = new RequestOptions()
                .override(resizeX, resizeY)
                .priority(Priority.HIGH);
        Glide.with(context)
                .asGif()
                .load(uri)
                .apply(options)
                .into(imageView);
    }

    @Override
    public boolean supportAnimatedGif() {
        return true;
    }

    /**
     * 从URI获取本地路径
     *
     * @return
     */
    public static String getAbsoluteImagePath(Context ctx, Uri contentUri) {

        //如果是对媒体文件，在android开机的时候回去扫描，然后把路径添加到数据库中。
        //由打印的contentUri可以看到：2种结构。正常的是：content://那么这种就要去数据库读取path。
        //另外一种是Uri是 file:///那么这种是 Uri.fromFile(File file);得到的
        System.out.println(contentUri);

        String[] projection = {MediaStore.Images.Media.DATA};
        String urlpath;
        CursorLoader loader = new CursorLoader(ctx, contentUri, projection, null, null, null);
        Cursor cursor = loader.loadInBackground();
        try {
            int column_index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            urlpath = cursor.getString(column_index);
            //如果是正常的查询到数据库。然后返回结构
            System.out.println(urlpath);
            return urlpath;
        } catch (Exception e) {

            e.printStackTrace();
            // TODO: handle exception
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        //如果是文件。Uri.fromFile(File file)生成的uri。那么下面这个方法可以得到结果
        urlpath = contentUri.getPath();
        System.out.println(urlpath);
        return urlpath;
    }
}