package com.ruimeng.things.shop;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by Administrator on 2017/11/15.
 */
public class Toolss {

    public static String IMAGE_NAME = "iv_share_";
    public static int  i =0;

    //根据网络图片url路径保存到本地
    public static final File saveImageToSdCard(Context context, String image) {
        boolean success = false;
        File file = null;
        try {
            file = createStableImageFile(context);

            Bitmap bitmap = null;
            URL url = new URL(image);
            HttpURLConnection conn = null;
            conn = (HttpURLConnection) url.openConnection();
            InputStream is = null;
            is = conn.getInputStream();
            bitmap =  BitmapFactory.decodeStream(is);

            FileOutputStream outStream;

            outStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (success) {
            return file;
        } else {
            return null;
        }
    }

    //创建本地保存路径
    public static File createStableImageFile(Context context) throws IOException {
        i++;
        String imageFileName =IMAGE_NAME + i+ ".jpg";
        File storageDir = context.getExternalCacheDir();
        File image = new File(storageDir, imageFileName);
        return image;
    }

    //判断是否安装了微信
    public static boolean isWeixinAvilible(Context context) {
        final PackageManager packageManager = context.getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals("com.tencent.mm")) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 权限 6.0 检测权限
     * @param context
     * @param permissions
     * @return
     */
    public static boolean checkoutPermissions(final Context context, String[] permissions) {
        // 进行权限请求
        boolean result = true;
        int count = 0;
        for (int i = 0; i < permissions.length; i++) {
            final String permission = permissions[i];
            if (ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                count = count + 1;
            }
        }
        if (count == permissions.length) {
            result = true;
        } else {
            result = false;
        }
        return result;
    }
}
