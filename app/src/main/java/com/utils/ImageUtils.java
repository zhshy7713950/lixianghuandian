package com.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class ImageUtils {
    public static Bitmap compressByQuality(Bitmap bitmap,int maxSize){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,50,baos);
        boolean isCompressed = false;
        while (baos.toByteArray().length/1024>maxSize){

        }
        return bitmap;
    }

    /**
     * 压缩图片
     *
     * @param bitmap
     *          被压缩的图片
     * @param sizeLimit
     *          大小限制
     * @return
     *          压缩后的图片
     */
    public static Bitmap compressBitmap(Bitmap bitmap, long sizeLimit) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int quality = 100;
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);

        // 循环判断压缩后图片是否超过限制大小
        while(baos.toByteArray().length / 1024 > sizeLimit) {
            // 清空baos
            baos.reset();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            quality -= 10;
        }

        Bitmap newBitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(baos.toByteArray()), null, null);

        return newBitmap;
    }
    public static void saveImage(Context context,Bitmap bitmap){
        String filePath = context.getApplicationContext().getFilesDir().getAbsolutePath()+"/image.jpg";
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Log.i("TAG", "saveImage: "+filePath);
        bitmap.compress(Bitmap.CompressFormat.JPEG,100, fileOutputStream);

    }
}
