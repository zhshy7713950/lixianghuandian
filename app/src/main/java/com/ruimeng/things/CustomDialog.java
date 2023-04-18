package com.ruimeng.things;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;



/**
 * 自定义Didalog
 */
public class CustomDialog extends Dialog {

    private Context context;
    private int resouceLayoutId;
    private View dialogView;
    private int gravity = Gravity.CENTER;
    private float height;

    public CustomDialog(Context context, int resouceLayoutId) {
        super(context, R.style.CustomProgressDialog);
        this.context = context;
        this.resouceLayoutId = resouceLayoutId;
    }

    /**
     * @param context
     * @param themeId         主题（样式）ID
     * @param resouceLayoutId 布局资源文件ID
     */
    public CustomDialog(Context context, int themeId, int resouceLayoutId) {
        super(context, themeId);
        this.context = context;
        this.resouceLayoutId = resouceLayoutId;
    }

    /**
     * @param context
     * @param themeId         主题（样式）ID
     * @param resouceLayoutId 布局资源文件ID
     */
    public CustomDialog(Context context, int themeId, int resouceLayoutId, int gravity) {
        super(context, themeId);
        this.context = context;
        this.resouceLayoutId = resouceLayoutId;
        this.gravity = gravity;
    }

    public int getGravity() {
        return gravity;
    }

    public void setGravity(int gravity) {
        this.gravity = gravity;
        init();
    }

    public void setGravity(int gravity, float height) {
        this.gravity = gravity;
        this.height = height;
        init();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        init();
    }

    /**
     * 给控件设置值
     *
     * @param viewId
     * @param text
     */
    public void setText(int viewId, String text) {
        if (dialogView != null && viewId != 0) {
            View view = dialogView.findViewById(viewId);
            if (view instanceof TextView) {
                ((TextView) view).setText(text);
            } else if (view instanceof Button) {
                ((Button) view).setText(text);
            } else if (view instanceof EditText) {
                ((EditText) view).setText(text);
            }

        }
    }

    /**
     * 输入样式
     *
     * @param viewId
     * @param inputType
     */
    public void setInputType(int viewId, int inputType) {
        if (viewId != 0 && inputType != 0) {
            View view = getView(viewId);
            if (view instanceof EditText) {
                ((EditText) view).setInputType(inputType);
            } else if (view instanceof TextView) {
                ((TextView) view).setInputType(inputType);
            } else if (view instanceof Button) {
                ((Button) view).setInputType(inputType);
            }
        }

    }


    /**
     * 给控件设置值
     *
     * @param viewId
     * @param text
     */
    public void setHintText(int viewId, String text) {
        if (dialogView != null && viewId != 0) {
            View view = dialogView.findViewById(viewId);
            if (view instanceof TextView) {
                ((TextView) view).setHint(text);
            } else if (view instanceof Button) {
                ((Button) view).setHint(text);
            } else if (view instanceof EditText) {
                ((EditText) view).setHint(text);
            }

        }
    }

    /**
     * 控件是否隐藏
     *
     * @param viewId
     */
    public void setVisible(int viewId, int visible) {
        if (dialogView != null && viewId != 0) {
            View view = dialogView.findViewById(viewId);
            view.setVisibility(visible);
        }
    }

    /**
     * 获取控件文本值
     *
     * @param viewId
     * @return
     */
    public String getText(int viewId) {
        if (dialogView != null && viewId != 0) {
            View view = dialogView.findViewById(viewId);
            if (view instanceof TextView) {
                return ((TextView) view).getText().toString();
            } else if (view instanceof Button) {
                return ((Button) view).getText().toString();
            } else if (view instanceof EditText) {
                return ((EditText) view).getText().toString();
            }

        }
        return "";
    }

    /**
     * 跟某个View设置监听
     *
     * @param viewId
     * @param mOnclickListener
     */
    public void setOnItemClickListener(int viewId, View.OnClickListener mOnclickListener) {
        if (dialogView != null && viewId != 0) {
            dialogView.findViewById(viewId).setOnClickListener(mOnclickListener);
        }

    }

    public View getView(int viewId) {
        return dialogView.findViewById(viewId);
    }


    public void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (resouceLayoutId != 0) {
            dialogView = inflater.inflate(resouceLayoutId, null);
            setContentView(dialogView);
        }
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics(); // 获取屏幕宽、高用
        if (this.gravity == Gravity.BOTTOM) {
            lp.width = d.widthPixels;
            if (this.height > 0) {
                lp.height = (int) (d.heightPixels * this.height); // 高度设置为屏幕的0.6
            }
        } else {
            lp.width = (int) (d.widthPixels * 0.8); // 高度设置为屏幕的0.6
        }
        lp.gravity = this.gravity;
        dialogWindow.setAttributes(lp);
    }

    public void setAdapter(BaseAdapter adapter, int viewId) {
        if (dialogView != null && viewId != 0) {
            View view = dialogView.findViewById(viewId);
            if (view instanceof ListView) {
                ((ListView) view).setAdapter(adapter);
            } else if (view instanceof GridView) {
                ((GridView) view).setAdapter(adapter);
            }

        }
    }


}