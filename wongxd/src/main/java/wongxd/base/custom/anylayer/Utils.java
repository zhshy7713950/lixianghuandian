package wongxd.base.custom.anylayer;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import androidx.annotation.Nullable;
import android.view.View;

/**
 * 描述：
 *
 * @author Cuizhen
 * @date 2018/10/25
 */
class Utils {

    /**
     * 从当前上下文获取Activity
     */
    @Nullable
    static Activity getActivity(Context context) {
        if (context == null) {
            return null;
        }
        if (context instanceof Activity) {
            return (Activity) context;
        }
        if (context instanceof ContextWrapper) {
            Context baseContext = ((ContextWrapper) context).getBaseContext();
            if (baseContext instanceof Activity) {
                return (Activity) baseContext;
            }
        }
        return null;
    }

    static Bitmap snapshot(View view){
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache(true);
        view.destroyDrawingCache();
        return view.getDrawingCache();
    }
}
