package wongxd.common;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/** Saves an image through MediaStore without broadcasting a file:// URI. */
public final class GallerySaver {

    private GallerySaver() {
    }

    public static Uri savePng(Context context, Bitmap bitmap, String displayName, String album)
            throws IOException {
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, displayName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + File.separator + album
            );
            values.put(MediaStore.Images.Media.IS_PENDING, 1);
        } else {
            File directory = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    album
            );
            if (!directory.exists() && !directory.mkdirs()) {
                throw new IOException("Unable to create gallery directory");
            }
            values.put(MediaStore.Images.Media.DATA, new File(directory, displayName).getAbsolutePath());
        }

        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri == null) {
            throw new IOException("Unable to create MediaStore item");
        }

        try {
            OutputStream output = resolver.openOutputStream(uri);
            if (output == null) {
                throw new IOException("Unable to open MediaStore output");
            }
            try {
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                    throw new IOException("Unable to encode PNG");
                }
            } finally {
                output.close();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues ready = new ContentValues();
                ready.put(MediaStore.Images.Media.IS_PENDING, 0);
                resolver.update(uri, ready, null, null);
            }
            return uri;
        } catch (IOException error) {
            resolver.delete(uri, null, null);
            throw error;
        }
    }
}
