package com.ruimeng.things.shop;

import android.Manifest;
import android.app.Activity;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.ruimeng.things.R;
import com.tbruyelle.rxpermissions2.RxPermissions;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import com.ruimeng.things.shop.bean.GetShareBean;
import com.ruimeng.things.shop.view.CustomDialog;
import wongxd.base.BaseBackActivity;
import wongxd.common.AnyKt;
import wongxd.common.EasyToast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CreateShareActivity extends BaseBackActivity implements View.OnClickListener {

    private Activity context;
    private QMUITopBar topbar;


    private TextView text_check_nums;
    private LinearLayout linear_images;
    private EditText edit_share_msg;
    private TextView text_copy_msg;
    private LinearLayout linear_share_circle;
    private LinearLayout linear_share_weichat;
    private Context mContext;
    private String copy_msg;
    //保存被选中的图片
    private Map<Integer, Boolean> checkMap = new HashMap<>();
    private List<File> files = new ArrayList<>();
    private int checkItem = 0;
    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    private GetShareBean getShareBean;

    private CustomDialog promptDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_create_share);
        initView();
        setListener();
        initData();
    }

    private void initView() {
        topbar = findViewById(R.id.topbar);
        initTopbar(topbar, "创建分享");
        text_check_nums = findViewById(R.id.text_check_nums);
        linear_images = findViewById(R.id.linear_images);
        edit_share_msg = findViewById(R.id.edit_share_msg);
        text_copy_msg = findViewById(R.id.text_copy_msg);
        linear_share_circle = findViewById(R.id.linear_share_circle);
        linear_share_weichat = findViewById(R.id.linear_share_weichat);

        mContext = this;
    }

    private void setListener() {

        text_copy_msg.setOnClickListener(this);
        linear_share_circle.setOnClickListener(this);
        linear_share_weichat.setOnClickListener(this);
    }

    private void initData() {

        getShareBean = (GetShareBean) getIntent().getSerializableExtra("info");
//        Log.i("data===", "===getText===" + getShareBean.getText());
        Log.i("data===", "===getShare_png===" + getShareBean.getShare_png());

        edit_share_msg.setText(getShareBean.getText());

        copy_msg = getShareBean.getText();


        for (int i = 0; i < getShareBean.getPics().size(); i++) {

            View view = LayoutInflater.from(mContext).inflate(R.layout.check_share_img_item, null);
            ImageView share_img = (ImageView) view.findViewById(R.id.share_img);
            final ImageView iv_check = (ImageView) view.findViewById(R.id.iv_check);

            AnyKt.loadImg(share_img, getShareBean.getPics().get(i));

            if (i == 0) {
                checkMap.put(0, true);
                iv_check.setImageResource(R.drawable.iv_ischecked);
                checkItem++;
                text_check_nums.setText("已选择" + checkItem + "张");
            }

            final int finalI = i;
            iv_check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (checkMap.size() > 0) {

                        if (checkItem == 9) {
                            EasyToast.Companion.getDEFAULT().show("图片选择不能超过9张");
                        } else if (checkMap.containsKey(finalI)) {

                            if (checkMap.get(finalI)) {

                                iv_check.setImageResource(R.drawable.iv_notchecked);

                                checkMap.remove(finalI);

                                checkItem--;
                                text_check_nums.setText("已选择" + checkItem + "张");

                            } else {
                                checkMap.put(finalI, true);
                                iv_check.setImageResource(R.drawable.iv_ischecked);
                                checkItem++;
                                text_check_nums.setText("已选择" + checkItem + "张");
                            }

                        } else {
                            checkMap.put(finalI, true);
                            iv_check.setImageResource(R.drawable.iv_ischecked);
                            checkItem++;
                            text_check_nums.setText("已选择" + checkItem + "张");
                        }

                    } else {
                        checkMap.put(finalI, true);
                        iv_check.setImageResource(R.drawable.iv_ischecked);
                        checkItem++;
                        text_check_nums.setText("已选择" + checkItem + "张");
                    }

                }

            });

            linear_images.addView(view);
        }
    }

    @Override
    public void onClick(View view) {

        if (view == text_copy_msg) {
            ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            // 将文本内容放到系统剪贴板里。
            cm.setText(copy_msg);
//            Toast.makeText(mContext, "复制成功", Toast.LENGTH_SHORT).show();
            promptDialog();
        }

        if (view == linear_share_circle) {
            Toast.makeText(mContext, "请稍后", Toast.LENGTH_SHORT).show();


            RxPermissions rxPermissions = new RxPermissions(context);
            rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                    .subscribe(new Observer<Boolean>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(Boolean aBoolean) {
                            if (aBoolean) {
                                shareImage(1);
                            } else {
                                Toast.makeText(mContext, "请开启SD卡读写权限", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });


        }

        if (view == linear_share_weichat) {
            if (checkMap.size() > 1) {
                Toast.makeText(mContext, "只能分享一张图片到微信好友", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(mContext, "请稍后", Toast.LENGTH_SHORT).show();

            RxPermissions rxPermissions = new RxPermissions(context);
            rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                    .subscribe(new Observer<Boolean>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(Boolean aBoolean) {
                            if (aBoolean) {
                                shareImage(0);
                            } else {
                                Toast.makeText(mContext, "请开启SD卡读写权限", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });

        }

    }


    /**
     * @param flag 0 发送给微信好友  1发送到朋友圈
     */
    private void shareImage(final int flag) {

        Log.i("info", "====checkMap===" + checkMap.toString());


        if (checkMap.size() == 0) {
            Toast.makeText(context, "您还没有选择图片!", Toast.LENGTH_SHORT).show();
        } else {

            new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        //把分享过后的list清空
                        files.clear();

                        //如果是分享微信好友,默认是分享带有二维码的那张图片，所以把选中的图片清除掉
                        if (flag == 0) {
                            checkMap.clear();
                        }
                        //加入带有二维码的图片
                        checkMap.put(10, true);

                        for (Integer key : checkMap.keySet()) {
                            File file;
                            if (key == 10) {
                                file = Toolss.saveImageToSdCard(context, getShareBean.getShare_png());//"http://img.alicdn.com/bao/uploaded/i4/693192501/TB2JKFteamgSKJjSsphXXcy1VXa_!!693192501.jpg");//
                            } else {
                                file = Toolss.saveImageToSdCard(context, getShareBean.getPics().get(key));
                            }
                            files.add(file);
                        }
                        Intent intent = new Intent();
                        ComponentName comp;

                        if (flag == 0) {
                            comp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI");
                        } else {
                            comp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI");
                            intent.putExtra("Kdescription", copy_msg);
                        }
                        intent.setComponent(comp);
                        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                        intent.setType("image/*");

                        ArrayList<Uri> imageUris = new ArrayList<Uri>();
                        for (File f : files) {
                            imageUris.add(Uri.fromFile(f));
                        }

                        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
                        startActivityForResult(intent, 1001);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.i("info", "====e====" + e.toString());
                    }
                }
            }).start();
        }
    }

    public void upImageMap() {
        if (checkMap.containsKey(10)) {
            checkMap.remove(10);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            upImageMap();
        }
    }

    private void promptDialog() {
        promptDialog = new CustomDialog(context, R.layout.dialog_prompt);
        promptDialog.setGravity(Gravity.CENTER);
        promptDialog.show();
        promptDialog.setOnItemClickListener(R.id.prompt_cancel, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptDialog.dismiss();
            }
        });
        promptDialog.setOnItemClickListener(R.id.prompt_confirm, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptDialog.dismiss();
                try {
                    Intent intent = new Intent();
                    ComponentName cmp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI");
                    intent.setAction(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setComponent(cmp);
//                    startActivityForResult(intent, 0);
                    startActivity(intent);
                } catch (Exception e) {
                    //若无法正常跳转，在此进行错误处理
                    Toast.makeText(context, "无法跳转到微信，请检查您是否安装了微信！", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
}
