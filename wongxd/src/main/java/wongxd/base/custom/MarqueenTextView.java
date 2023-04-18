package wongxd.base.custom;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import android.text.TextUtils;
import android.util.AttributeSet;

/**
 * Created by wongxd on 2018/11/5.
 */
public class MarqueenTextView extends AppCompatTextView {
    public MarqueenTextView(Context context) {
        this(context,null);
    }

    public MarqueenTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MarqueenTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setSingleLine();
        setEllipsize(TextUtils.TruncateAt.MARQUEE);
        setMarqueeRepeatLimit(-1);
    }


    @Override
    public boolean isEnabled() {
        return true;
    }

}
