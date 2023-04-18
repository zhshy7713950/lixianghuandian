package wongxd.common.autoInstall;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wongxd on 2020/1/3.
 */
public class QueryFileUtil {


    /**
     * @param ctx
     * @param like 后缀名， 如 ：apk
     * @return
     */
    public static List<QueryFileBean> queryFilesLike(Context ctx, String like) {
        List<QueryFileBean> files = new ArrayList<>();

        String[] projection = new String[]{MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.SIZE
        };
        Cursor cursor = ctx.getContentResolver().query(
                Uri.parse("content://media/external/file"),
                projection,
                MediaStore.Files.FileColumns.DATA + " like ?",
                new String[]{"%." + like},
                null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {

                int idindex = cursor
                        .getColumnIndex(MediaStore.Files.FileColumns._ID);
                int dataindex = cursor
                        .getColumnIndex(MediaStore.Files.FileColumns.DATA);
                int sizeindex = cursor
                        .getColumnIndex(MediaStore.Files.FileColumns.SIZE);
                do {
                    String id = cursor.getString(idindex);
                    String path = cursor.getString(dataindex);
                    String size = cursor.getString(sizeindex);

                    int dot = path.lastIndexOf("/");
                    String name = path.substring(dot + 1);
                    Log.i("QueryFileUtil", name + " path:" + path + " size:" + size);

                    files.add(new QueryFileBean(path, name, size));

                } while (cursor.moveToNext());
            }
        }
        if (null != cursor) {
            cursor.close();
        }

        return files;
    }


    public static class QueryFileBean {
        public String filePath;
        public String fileName;
        public String fileSize;

        public QueryFileBean(String filePath, String fileName, String fileSize) {
            this.filePath = filePath;
            this.fileName = fileName;
            this.fileSize = fileSize;
        }
    }
}
