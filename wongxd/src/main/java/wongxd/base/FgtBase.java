package wongxd.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qmuiteam.qmui.widget.QMUITopBar;

import me.yokeyword.fragmentation.ISupportFragment;
import me.yokeyword.fragmentation.SupportFragment;
import com.wongxd.R;


/**
 * 结合 fragmentation ,IContextable 的 BaseMvvmFragment
 * <p>
 * Created by Wongxd on 2018/04/23.
 */
public abstract class FgtBase extends SupportFragment {

    protected View rootView;

    protected QMUITopBar topbar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(getLayoutRes(), container, false);
        try {
            topbar = rootView.findViewById(R.id.topbar);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rootView;
    }

    @Override
    public void onDestroyView() {
        rootView = null;
        super.onDestroyView();
    }


    @Override
    public void start(ISupportFragment toFragment) {
        if (null != getParentFragment()) {
            ((SupportFragment) getParentFragment()).start(toFragment);
        } else {
            super.start(toFragment);
        }
    }

    protected abstract int getLayoutRes();

    protected void initTopbar(QMUITopBar topBar, String title) {
        initTopbar(topBar, title, true);
    }

    protected void initTopbar(QMUITopBar topBar, String title, boolean isShowBackBtn) {
        if (isShowBackBtn) {
            topBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pop();
                }
            });
        }
        topBar.setTitle(title);
        topBar.setBackgroundColor(getResources().getColor(R.color.app_top_color));
    }

    protected void initTopbar1(QMUITopBar topBar, String title, boolean isShowBackBtn) {
        if (isShowBackBtn) {
            topBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pop();
                }
            });
        }

        topBar.setTitle(title);

        topBar.setBackgroundColor(getResources().getColor(R.color.app_top_color));
    }
}
