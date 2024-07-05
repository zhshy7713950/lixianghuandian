package wongxd.utils;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;


public class OpenFileThing {
    public enum FileType {
        img, text, audio, video, chm, apk, excel, word, pdf, ppt, none
    }

    public static void openAssignFolder(AppCompatActivity ctx, Uri uri) {

        if (null == uri) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uri, "file/*");
        try {
            ctx.startActivity(intent);
            ctx.startActivity(Intent.createChooser(intent, "选择浏览工具"));
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }


    public static void openAssignFolder(FragmentActivity ctx, Uri uri, FileType fileType) {
        if (null == uri) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        switch (fileType) {
            case img:
//                打开图片文件
                intent.setDataAndType(uri, "image/*");
                break;
            case pdf:
//                打开PDF文件
                intent.setDataAndType(uri, "application/pdf");
                break;
            case text:
//                打开文本文件
                intent.setDataAndType(uri, "text/plain");
                break;
            case audio:
//                打开音频文件
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("oneshot", 0);
                intent.putExtra("configchange", 0);
                intent.setDataAndType(uri, "audio/*");
                break;
            case video:
//                打开视频文件
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("oneshot", 0);
                intent.putExtra("configchange", 0);
                intent.setDataAndType(uri, "video/*");
                break;
            case chm:
//                打开CHM文件
                intent.setDataAndType(uri, "application/x-chm");
                break;
            case apk:
//                打开apk文件
                intent.setDataAndType(uri, "application/vnd.android.package-archive");
                break;

            case ppt:
//                打开PPT文件
                intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
                break;
            case excel:
//                打开Excel文件
                intent.setDataAndType(uri, "application/vnd.ms-excel");
                break;
            case word:
//                打开Word文件
                intent.setDataAndType(uri, "application/msword");
                break;
        }
        try {
            ctx.startActivity(intent);
            ctx.startActivity(Intent.createChooser(intent, "选择浏览工具"));
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

}