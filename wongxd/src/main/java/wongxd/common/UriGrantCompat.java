package wongxd.common;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.util.Collections;
import java.util.List;

/**
 * Adds explicit URI grants in both the intent flags and ClipData.
 *
 * Some receiving apps only inspect ClipData when propagating a content URI to
 * another component, so setting both is intentional.
 */
public final class UriGrantCompat {

    private UriGrantCompat() {
    }

    public static void grantRead(Context context, Intent intent, Uri uri) {
        grant(context, intent, Collections.singletonList(uri), false);
    }

    public static void grantReadWrite(Context context, Intent intent, Uri uri) {
        grant(context, intent, Collections.singletonList(uri), true);
    }

    public static void grantRead(Context context, Intent intent, List<? extends Uri> uris) {
        grant(context, intent, uris, false);
    }

    public static void grantReadWrite(Context context, Intent intent, List<? extends Uri> uris) {
        grant(context, intent, uris, true);
    }

    private static void grant(
            Context context,
            Intent intent,
            List<? extends Uri> uris,
            boolean writable
    ) {
        if (context == null || intent == null || uris == null || uris.isEmpty()) {
            return;
        }

        int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
        if (writable) {
            flags |= Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
        }
        intent.addFlags(flags);

        ClipData clipData = null;
        for (Uri uri : uris) {
            if (uri == null) {
                continue;
            }
            if (clipData == null) {
                clipData = ClipData.newUri(context.getContentResolver(), "shared_uri", uri);
            } else {
                clipData.addItem(new ClipData.Item(uri));
            }
        }
        if (clipData != null) {
            intent.setClipData(clipData);
        }
    }
}
