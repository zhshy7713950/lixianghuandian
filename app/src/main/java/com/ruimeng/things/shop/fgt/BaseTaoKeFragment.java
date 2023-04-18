package com.ruimeng.things.shop.fgt;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.ruimeng.things.R;
import com.ruimeng.things.shop.view.CustomDialog;

import java.lang.reflect.Field;
import java.util.Map;

public abstract class BaseTaoKeFragment extends Fragment {

    private View view;
    public Context mContext;
    public Bundle savedInstanceState;//用于恢复界面数据

    private CustomDialog customDialog;

    /**
     * fragment 返回值
     *
     * @param data
     */
    public abstract void onActivityFragmentResult(String data);

    /**
     * 更新键盘状态
     *
     * @param isupkeyboard
     */
    public void updateKeyBoradState(boolean isupkeyboard) {

    }

    /**
     * 针对mqtt 刷新数据或fragment的跳转
     *
     * @param type -1 接口刷新 0 为find.view.carousel 1为find.view.notification 2 为find.view.buttons 3 为find.view.leaderboard
     * @param msg
     */
    public void onRefresh(int type, String msg) {
    }




    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int resId = getResouceLayoutId();
        if (resId != 0) {
            view = LayoutInflater.from(mContext).inflate(resId, container, false);
        }
        this.savedInstanceState = savedInstanceState;
        return view != null ? view : super.onCreateView(inflater, container, savedInstanceState);
    }

    public void updateDataRefresh() {
    }

    public void transmitData(Map map){

    }

    public  void getArgumentsParam (){

    }

    public void dataTransmission(Object object) {

    }

    @Override
    public void onDetach() {
        super.onDetach();

        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);


        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = getActivity();
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getArgumentsParam ();
        transmitData(null);
        initView();
        initData();
    }



    /**
     * 获取资源布局ID
     *
     * @return
     */
    public abstract int getResouceLayoutId();

    /**
     * 实例化View
     */
    public abstract void initView();

    /**
     * 初始化数据
     */
    public abstract void initData();

    /**
     * 获取View
     *
     * @param id
     * @param <V>
     * @return
     */
    protected <V> V findView(int id) {
        if (view != null)
            return (V) view.findViewById(id);
        return null;
    }

    /**
     * 获取View
     *
     * @param id
     * @param <V>
     * @return
     */
    protected <V> V findView(View headView, int id) {
        if (headView != null)
            return (V) headView.findViewById(id);
        return null;
    }

    public void showDialog(String msg) {
        if (customDialog != null && customDialog.isShowing()) {
            return;
        }
        customDialog = new CustomDialog(mContext, R.layout.dialog_custom);
        customDialog.setGravity(Gravity.CENTER);
        customDialog.setCancelable(true);
        customDialog.setText(R.id.custom_msg, msg);
        customDialog.show();
    }

    public void dismissDialog() {
        if (customDialog != null && customDialog.isShowing()) {
            customDialog.dismiss();
        }
    }

}
