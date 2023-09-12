package com.utils;

import android.content.Context;
import android.graphics.Color;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;

import java.util.List;

public class OptionPickerUtil {
    public static void showSingleOptionPicker(Context context, List<String> data, OnSinglePickerSelectListener listener) {
        OptionsPickerView view = new OptionsPickerBuilder(context, (options1, options2, options3, v) -> listener.selectStr(data.get(options1)))
                .setTitleText("")
                .setCancelText("取消")//取消按钮文字
                .setSubmitText("确定")//确认按钮文字
                .setContentTextSize(18)//设置滚轮文字大小
                .setSelectOptions(0, 1)//默认选中项
                .setBgColor(Color.parseColor("#404E59"))
                .setTitleBgColor(Color.parseColor("#404E59"))
                .setSubmitColor(Color.parseColor("#29EBB6"))//确定按钮文字颜色
                .setCancelColor(Color.WHITE)//取消按钮文字颜色
                .setTextColorCenter(Color.WHITE)
                .isRestoreItem(true)//切换时是否还原，设置默认选中第一项。
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .build();
        view.setPicker(data);
        view.show();

    }
    public static void showOptionPicker(Context context, List<String> data, OnOptionsSelectListener listener) {
        OptionsPickerView view = new OptionsPickerBuilder(context, listener)
                .setTitleText("")
                .setCancelText("取消")//取消按钮文字
                .setSubmitText("确定")//确认按钮文字
                .setContentTextSize(18)//设置滚轮文字大小
                .setSelectOptions(0, 1)//默认选中项
                .setBgColor(Color.parseColor("#404E59"))
                .setTitleBgColor(Color.parseColor("#404E59"))
                .setSubmitColor(Color.parseColor("#29EBB6"))//确定按钮文字颜色
                .setCancelColor(Color.WHITE)//取消按钮文字颜色
                .setTextColorCenter(Color.WHITE)
                .isRestoreItem(true)//切换时是否还原，设置默认选中第一项。
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .build();
        view.setPicker(data);
        view.show();

    }
}
