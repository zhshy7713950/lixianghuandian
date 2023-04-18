package com.ruimeng.things.shop.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import androidx.annotation.NonNull;
import android.text.SpannableString;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.*;
import com.bumptech.glide.Glide;
import wongxd.common.AnyKt;


/**
 * 容器View管理器
 *
 * @author Karision.Gou
 */
public class ViewHolder {
    private final SparseArray<View> mViews;
    private int mPosition;
    private View mConvertView;


    private ViewHolder(Context context, ViewGroup parent, int layoutId,
                       int position) {
        this.mPosition = position;
        this.mViews = new SparseArray<View>();
        mConvertView = LayoutInflater.from(context).inflate(layoutId, parent,
                false);
        // setTag
        mConvertView.setTag(this);
    }

//    /**
//     * 设置圆环
//     *
//     * @param itemId
//     * @param percents
//     * @param isHaveAnim
//     * @return
//     */
//    public ViewHolder setAnimationView(int itemId, ArrayList percents, boolean isHaveAnim, int colors[]) {
//        View view = getView(itemId);
//        if (view instanceof CircleAnimationView) {
//            CircleAnimationView circleAnimationView = getView(itemId);
//            circleAnimationView.setIsHaveAnim(isHaveAnim);
//            circleAnimationView.setColors(colors);
//            circleAnimationView.setPercents(percents);
//        }
//        return this;
//    }


    /**
     * 拿到一个ViewHolder对象
     *
     * @param context
     * @param convertView
     * @param parent
     * @param layoutId
     * @param position
     * @return
     */
    public static ViewHolder get(Context context, View convertView,
                                 ViewGroup parent, int layoutId, int position) {
        if (convertView == null) {
            return new ViewHolder(context, parent, layoutId, position);
        }
        return (ViewHolder) convertView.getTag();
    }

    public ViewHolder setNetImage(Context context,int viewId, @NonNull String pathUrl){
       View view = getView(viewId);
        if (view instanceof ImageView){
           try{
               AnyKt.loadImg((ImageView) view,pathUrl);
           }catch (Exception e){
               e.printStackTrace();
           }
        }
        return this;
    }


    public View getConvertView() {
        return mConvertView;
    }


    /**
     * 通过控件的Id获取对于的控件，如果没有则加入views
     *
     * @param viewId
     * @return
     */
    public <T extends View> T getView(int viewId) {
        View view = mViews.get(viewId);
        if (view == null) {
            view = mConvertView.findViewById(viewId);
            view.setVisibility(View.VISIBLE);
            mViews.put(viewId, view);
        }
        return (T) view;
    }


    /**
     * 为TextView设置字符串
     *
     * @param viewId
     * @param text
     * @return
     */
    public ViewHolder setText(int viewId, String text) {
        TextView view = getView(viewId);
        view.setText(text);
        return this;
    }

    public ViewHolder setTexts(int viewId, SpannableString text) {
        TextView view = getView(viewId);
        view.setText(text);
        return this;
    }

    /**
     * 为TextView设置字符串
     *
     * @param viewId
     * @param text
     * @param typeface 字体
     * @return
     */
    public ViewHolder setText(int viewId, String text, Typeface typeface) {
        TextView view = getView(viewId);
        view.setText(text);
        view.setTypeface(typeface);
        return this;
    }

    /**
     * 为TextView设置字符串
     *
     * @param viewId
     * @param colors
     * @return
     */
    public ViewHolder setTextColor(int viewId, int colors) {
        TextView view = getView(viewId);
        view.setTextColor(colors);
        return this;
    }

    /**
     * 为TextView设置字符串
     *
     * @param viewId
     * @return
     */
    public String getText(int viewId) {
        TextView view = getView(viewId);

        return view.getText().toString();
    }

//    /**
//     * 为TextView设置字符串
//     *
//     * @param viewId
//     * @param firstStyleItem
//     * @return
//     */
//    public ViewHolder setText(int viewId, List<TextStyleItem> firstStyleItem) {
//        TextView view = getView(viewId);
//        StyleBuilder styleBuilder = new StyleBuilder();
//        if (firstStyleItem != null) {
//            for (TextStyleItem textSyleItem : firstStyleItem
//                    ) {
//                styleBuilder.addStyleItem(textSyleItem);
//            }
//        }
//        styleBuilder.show(view);
//        return this;
//    }

    /**
     * 为TextView设置字符串
     *
     * @param viewId
     * @return
     */
    public ViewHolder setTextMinWidth(int viewId, int width) {
        TextView view = getView(viewId);
        view.setMinWidth(width);
        return this;
    }

    /**
     * 为TextView设置字符串
     *
     * @param viewId
     * @param ischeck
     * @return
     */
    public ViewHolder setChecked(int viewId, boolean ischeck) {
        View view = getView(viewId);
        if (view instanceof CheckBox) {
            ((CheckBox) view).setChecked(ischeck);
        } else if (view instanceof RadioButton) {
            ((RadioButton) view).setChecked(ischeck);
        }
        return this;
    }

    /**
     * 获取check的值
     *
     * @param viewId
     * @return
     */
    public boolean getCheck(int viewId) {
        View view = getView(viewId);
        if (view instanceof CheckBox) {
            return ((CheckBox) view).isChecked();
        } else if (view instanceof RadioButton) {
            return ((RadioButton) view).isChecked();
        }
        return false;
    }


    /**
     * 为TextView设置tag
     *
     * @param viewId
     * @param text
     * @return
     */
    public ViewHolder setTag(int viewId, Object text) {
        TextView view = getView(viewId);
        view.setTag(text);
        return this;
    }

    public ViewHolder hideView(int viewId) {
        getView(viewId).setVisibility(View.GONE);
        return this;
    }


    public ViewHolder showView(int viewId) {
        getView(viewId).setVisibility(View.VISIBLE);
        return this;
    }


    /**
     * 设置动画
     *
     * @param viewId
     * @param animation
     * @return
     */
    public ViewHolder setAnim(int viewId, Animation animation) {
        View view = getView(viewId);
        view.startAnimation(animation);
        return this;
    }


    /**
     * 停止动画
     *
     * @param viewId
     * @param animation
     */
    public ViewHolder stopAnim(int viewId, Animation animation) {
        // TODO Auto-generated method stub
        View view = getView(viewId);
        view.clearAnimation();
        return this;
    }


    /**
     * 为View
     *
     * @param viewId
     * @param isVisible
     * @return
     */
    public ViewHolder setVisible(int viewId, boolean isVisible) {
        View view = getView(viewId);
        if (isVisible) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }

        return this;
    }


    /**
     * 为TextView设置字符串,带颜色
     *
     * @param viewId
     * @param text
     * @return
     */
    public ViewHolder setText(int viewId, String text, int color) {
        TextView view = getView(viewId);
        view.setText(text);
        view.setTextColor(color);
        return this;
    }


    /**
     * 为ImageView设置图片
     *
     * @param viewId
     * @param drawableId
     * @return
     */
    public ViewHolder setImageResource(int viewId, int drawableId) {
        if (viewId != 0 && drawableId != 0) {
            View view = getView(viewId);
            if (view instanceof ImageView) {
                ((ImageView) view).setImageResource(drawableId);
            } else if (view instanceof ImageButton) {
                ((ImageButton) view).setImageResource(drawableId);
            }
        }
        return this;
    }

    /**
     * 为ImageView设置图片
     *
     * @param viewId
     * @param drawableId
     * @return
     */
    public ViewHolder setImageResource(int viewId, String drawableId) {
        if (viewId != 0 && drawableId != null) {
            //得到可用的图片
            View view = getView(viewId);
            if (view instanceof ImageView) {
                AnyKt.loadImg((ImageView) view,drawableId);
            }
        }
        return this;
    }

    /**
     * 为ImageView设置图片
     *
     * @param viewId
     * @return
     */
    public ViewHolder setViewHeight(int viewId, int height) {
        if (viewId != 0 && height != 0) {
            //得到可用的图片
            View view = getView(viewId);
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = height;
            view.setLayoutParams(layoutParams);
        }
        return this;
    }


    public ViewHolder setViewAlpha(int viewId, int alpha) {
        if (viewId != 0) {
            //得到可用的图片
            View view = getView(viewId);
            view.getBackground().setAlpha(alpha);
        }
        return this;
    }





    public ViewHolder setImageResource(Context context, int viewId, String drawableId) {
        if (viewId != 0 && drawableId != null) {
            //得到可用的图片
            View view = getView(viewId);
            if (view instanceof ImageView) {
//                RequestOptions options = new RequestOptions()
//                        .centerCrop()
//                        .placeholder(R.drawable.icon_normal)
//                        .error(R.drawable.icon_normal)
//                        .priority(Priority.HIGH)
//                        .diskCacheStrategy(DiskCacheStrategy.NONE);

                Glide.with(context).load(drawableId)
//                        .apply(options)
//                        .placeholder(R.drawable.icon_normal)
                        .into((ImageView) view);

            } else if (view instanceof ImageButton) {
//                RequestOptions options = new RequestOptions()
//                        .centerCrop()
//                        .placeholder(R.drawable.icon_normal)
//                        .error(R.drawable.icon_normal)
//                        .priority(Priority.HIGH)
//                        .diskCacheStrategy(DiskCacheStrategy.NONE);
                Glide.with(context).load(drawableId)
//                        .apply(options)
                        .into((ImageButton) view);
            }
        }
        return this;
    }

    /**
     * 为View设置图片
     *
     * @param viewId
     * @param drawableId
     * @return
     */
    public ViewHolder setBackgroundBg(int viewId, int drawableId) {
        if (viewId != 0 && drawableId != 0) {
            View view = getView(viewId);
            if (view == null) {
                return this;
            }
            view.setBackgroundResource(drawableId);
        }
        return this;
    }

    /**
     * 为View设置是否可点击
     *
     * @param viewId
     * @param isClicked
     * @return
     */
    public ViewHolder setOnClicked(int viewId, boolean isClicked) {
        if (viewId != 0) {
            View view = getView(viewId);
            if (view == null) {
                return this;
            }
            view.setClickable(isClicked);
        }
        return this;
    }


    /**
     * 为ImageView设置图片
     *
     * @param viewId
     * @param bm
     * @return
     */
    public ViewHolder setImageBitmap(int viewId, Bitmap bm) {
        ImageView view = getView(viewId);
        view.setImageBitmap(bm);
        return this;
    }

    /**
     * 为View设置点击事件
     *
     * @param viewId
     * @param onClickListener
     * @return
     */
    public ViewHolder setViewOnclick(int viewId, OnClickListener onClickListener) {
        View view = getView(viewId);
        view.setOnClickListener(onClickListener);
        return this;
    }


    /**
     * 为View check
     *
     * @param viewId
     * @return
     */
    public ViewHolder setViewOnCheckChangeListener(int viewId, CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
        View view = getView(viewId);
        if (view instanceof RadioButton) {
            ((RadioButton) view).setOnCheckedChangeListener(onCheckedChangeListener);
        } else if (view instanceof CheckBox) {
            ((CheckBox) view).setOnCheckedChangeListener(onCheckedChangeListener);
        }

        return this;
    }

    /**
     * 为View设置长按点击事件
     *
     * @param viewId
     * @param longClickListener
     * @return
     */
    public ViewHolder setViewOnclick(int viewId, OnLongClickListener longClickListener) {
        View view = getView(viewId);
        view.setOnLongClickListener(longClickListener);
        return this;
    }


    public int getPosition() {
        return mPosition;
    }

}