package com.ruimeng.things.shop;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import com.bumptech.glide.Glide;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.ruimeng.things.R;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.internal.entity.CaptureStrategy;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import com.ruimeng.things.shop.TKHttpKt;
import com.ruimeng.things.shop.TkPath;
import com.ruimeng.things.shop.bean.UploadFileBean;
import wongxd.base.BaseBackActivity;
import wongxd.common.AnyKt;
import wongxd.common.EasyToast;
import wongxd.common.net.netDSL.RequestWrapper;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FeedBackActivity extends BaseBackActivity implements View.OnClickListener {


    private Activity context;
    private TextView tag_one, tag_two, tag_three, tag_four, tag_five;
    private String getItemId = "", getImg = "", getTag = "其他";

    private EditText feed_back_input;
    private ImageView feed_back_image;
    private Button feed_back_btn;

    private static final int REQUEST_CODE_CHOOSE = 23;
    private List<String> mPaths = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.aty_feedback);
        initView();
        setListener();
        initData();
    }

    private void initView() {
        QMUITopBar topBar = findViewById(R.id.topbar);
        initTopbar(topBar, "商品问题反馈");


        tag_one = findViewById(R.id.tag_one);
        tag_two = findViewById(R.id.tag_two);
        tag_three = findViewById(R.id.tag_three);
        tag_four = findViewById(R.id.tag_four);
        tag_five = findViewById(R.id.tag_five);
        feed_back_input = findViewById(R.id.feed_back_input);
        feed_back_image = findViewById(R.id.feed_back_image);
        feed_back_btn = findViewById(R.id.feed_back_btn);
    }

    private void setListener() {
        tag_one.setOnClickListener(this);
        tag_two.setOnClickListener(this);
        tag_three.setOnClickListener(this);
        tag_four.setOnClickListener(this);
        tag_five.setOnClickListener(this);
        feed_back_image.setOnClickListener(this);
        feed_back_btn.setOnClickListener(this);
    }

    private void initData() {
        getItemId = getIntent().getStringExtra("item_id");
    }

    @Override
    public void onClick(View view) {
        if (view == tag_one) {
            getTag = tag_one.getText().toString();
            tag_one.setTextColor(ContextCompat.getColor(context, R.color.white));
            tag_two.setTextColor(ContextCompat.getColor(context, R.color.gray_B6));
            tag_three.setTextColor(ContextCompat.getColor(context, R.color.gray_B6));
            tag_four.setTextColor(ContextCompat.getColor(context, R.color.gray_B6));
            tag_five.setTextColor(ContextCompat.getColor(context, R.color.gray_B6));
            tag_one.setBackgroundResource(R.drawable.rectangle_bg_main_color);
            tag_two.setBackgroundResource(R.drawable.rectangle_bg_gray_e);
            tag_three.setBackgroundResource(R.drawable.rectangle_bg_gray_e);
            tag_four.setBackgroundResource(R.drawable.rectangle_bg_gray_e);
            tag_five.setBackgroundResource(R.drawable.rectangle_bg_gray_e);
        }
        if (view == tag_two) {
            getTag = tag_two.getText().toString();
            tag_one.setTextColor(ContextCompat.getColor(context, R.color.gray_B6));
            tag_two.setTextColor(ContextCompat.getColor(context, R.color.white));
            tag_three.setTextColor(ContextCompat.getColor(context, R.color.gray_B6));
            tag_four.setTextColor(ContextCompat.getColor(context, R.color.gray_B6));
            tag_five.setTextColor(ContextCompat.getColor(context, R.color.gray_B6));
            tag_one.setBackgroundResource(R.drawable.rectangle_bg_gray_e);
            tag_two.setBackgroundResource(R.drawable.rectangle_bg_main_color);
            tag_three.setBackgroundResource(R.drawable.rectangle_bg_gray_e);
            tag_four.setBackgroundResource(R.drawable.rectangle_bg_gray_e);
            tag_five.setBackgroundResource(R.drawable.rectangle_bg_gray_e);
        }
        if (view == tag_three) {
            getTag = tag_three.getText().toString();
            tag_one.setTextColor(ContextCompat.getColor(context, R.color.gray_B6));
            tag_two.setTextColor(ContextCompat.getColor(context, R.color.gray_B6));
            tag_three.setTextColor(ContextCompat.getColor(context, R.color.white));
            tag_four.setTextColor(ContextCompat.getColor(context, R.color.gray_B6));
            tag_five.setTextColor(ContextCompat.getColor(context, R.color.gray_B6));
            tag_one.setBackgroundResource(R.drawable.rectangle_bg_gray_e);
            tag_two.setBackgroundResource(R.drawable.rectangle_bg_gray_e);
            tag_three.setBackgroundResource(R.drawable.rectangle_bg_main_color);
            tag_four.setBackgroundResource(R.drawable.rectangle_bg_gray_e);
            tag_five.setBackgroundResource(R.drawable.rectangle_bg_gray_e);
        }
        if (view == tag_four) {
            getTag = tag_four.getText().toString();
            tag_one.setTextColor(ContextCompat.getColor(context, R.color.gray_B6));
            tag_two.setTextColor(ContextCompat.getColor(context, R.color.gray_B6));
            tag_three.setTextColor(ContextCompat.getColor(context, R.color.gray_B6));
            tag_four.setTextColor(ContextCompat.getColor(context, R.color.white));
            tag_five.setTextColor(ContextCompat.getColor(context, R.color.gray_B6));
            tag_one.setBackgroundResource(R.drawable.rectangle_bg_gray_e);
            tag_two.setBackgroundResource(R.drawable.rectangle_bg_gray_e);
            tag_three.setBackgroundResource(R.drawable.rectangle_bg_gray_e);
            tag_four.setBackgroundResource(R.drawable.rectangle_bg_main_color);
            tag_five.setBackgroundResource(R.drawable.rectangle_bg_gray_e);
        }
        if (view == tag_five) {
            getTag = tag_five.getText().toString();
            tag_one.setTextColor(ContextCompat.getColor(context, R.color.gray_B6));
            tag_two.setTextColor(ContextCompat.getColor(context, R.color.gray_B6));
            tag_three.setTextColor(ContextCompat.getColor(context, R.color.gray_B6));
            tag_four.setTextColor(ContextCompat.getColor(context, R.color.gray_B6));
            tag_five.setTextColor(ContextCompat.getColor(context, R.color.white));
            tag_one.setBackgroundResource(R.drawable.rectangle_bg_gray_e);
            tag_two.setBackgroundResource(R.drawable.rectangle_bg_gray_e);
            tag_three.setBackgroundResource(R.drawable.rectangle_bg_gray_e);
            tag_four.setBackgroundResource(R.drawable.rectangle_bg_gray_e);
            tag_five.setBackgroundResource(R.drawable.rectangle_bg_main_color);
        }
        if (view == feed_back_image) {
            chooseImage();
        }
        if (view == feed_back_btn) {
            if (TextUtils.isEmpty(feed_back_input.getText())) {
                Toast.makeText(context, "请输入您的意见", Toast.LENGTH_SHORT).show();
                return;
            }
            requestFeedBack(getImg, getTag, feed_back_input.getText().toString(), getItemId);
        }
    }

    private void requestFeedBack(String img, String tag, String msg, String item_id) {
        final HashMap<String, String> map = new HashMap();
        map.put("img", img);
        map.put("tag", tag);
        map.put("msg", msg);
        map.put("item_id", item_id);

        TKHttpKt.tkHttp(new Function1<RequestWrapper, Unit>() {
            @Override
            public Unit invoke(RequestWrapper requestWrapper) {

                requestWrapper.setUrl(TkPath.INSTANCE.getFEED_BACK());
                requestWrapper.setParams(map);

                requestWrapper.onSuccessWithMsg(new Function2<String, String, Unit>() {
                    @Override
                    public Unit invoke(String s, String msg) {
                        EasyToast.Companion.getDEFAULT().show(msg);
                        return null;
                    }
                });

                requestWrapper.onFail(new Function2<Integer, String, Unit>() {
                    @Override
                    public Unit invoke(Integer i, String s) {
                        EasyToast.Companion.getDEFAULT().show(s);
                        return null;
                    }
                });


                requestWrapper.onFinish(new Function0<Unit>() {
                    @Override
                    public Unit invoke() {

                        return null;
                    }
                });

                return null;
            }
        });


    }

    private void chooseImage() {
        RxPermissions rxPermissions = new RxPermissions(context);
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
                            try {

                                Matisse.from(context)
                                        .choose(MimeType.ofAll())
                                        .capture(true)
                                        .captureStrategy(
                                                new CaptureStrategy(true,
                                                        "fileprovider"))
                                        .countable(true)
                                        .maxSelectable(1)
                                        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                                        .thumbnailScale(0.85f)
                                        .imageEngine(new PostGlideEngine())
                                        .forResult(REQUEST_CODE_CHOOSE);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            EasyToast.Companion.getDEFAULT().show("未能获取到访问存储的权限");
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


    //计算图片的缩放值
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    // 根据路径获得图片并压缩，返回bitmap用于显示
    public static Bitmap getSmallBitmap(String filePath, int width, int height) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, width, height);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap image = BitmapFactory.decodeFile(filePath, options);
        //质量压缩
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        //质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int option = 100;
        //循环判断如果压缩后图片是否大于200kb,大于继续压缩
        while (baos.toByteArray().length / 1024 > 200) {
            //重置baos即清空baos
            baos.reset();
            option -= 10;//每次都减少10
            image.compress(Bitmap.CompressFormat.JPEG, option, baos);
        }
        //把压缩后的数据baos存放到ByteArrayInputStream中
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        //把ByteArrayInputStream数据生成图片
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (out != null) bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        return bitmap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == Activity.RESULT_OK && data != null) {
            mPaths.clear();
            List<Uri> uris = Matisse.obtainResult(data);

            for (Uri uri : uris) {
                mPaths.add(PostGlideEngine.getAbsoluteImagePath(context, uri).replace("/my_images/",
                        "/storage/emulated/0/"));
            }



            String filePaths = mPaths.get(0).toString();
            Bitmap sBitmap = getSmallBitmap(filePaths, 200, 200);
            File file = new File(filePaths);//将要保存图片的路径
            try {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                sBitmap.compress(Bitmap.CompressFormat.JPEG, 70, bos);
                bos.flush();
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            requestUploadFile(filePaths);
        }
    }

    /**
     * 文件上传接口
     *
     * @param filePaths
     */
    public void requestUploadFile(final String filePaths) {


        TKHttpKt.tkHttp(new Function1<RequestWrapper, Unit>() {
            @Override
            public Unit invoke(RequestWrapper requestWrapper) {

                Map<String, File> imgs = new HashMap<>();
                imgs.put("file", new File(filePaths));

                requestWrapper.setUrl(TkPath.INSTANCE.getFILE());
                requestWrapper.setImgs(imgs);

                requestWrapper.onSuccess(new Function1<String, Unit>() {
                    @Override
                    public Unit invoke(String s) {

                        try {
                            ;
                            UploadFileBean uploadFileBean = AnyKt.getGson().fromJson(s, UploadFileBean.class);
                            if (null != uploadFileBean) {

                                if (200 == uploadFileBean.getErrcode()) {
                                    getImg = uploadFileBean.getData().getString();
                                    Glide.with(context).load(getImg)
                                            .into(feed_back_image);
                                }

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        return null;
                    }
                });


                requestWrapper.onFail(new Function2<Integer, String, Unit>() {
                    @Override
                    public Unit invoke(Integer i, String s) {
                        EasyToast.Companion.getDEFAULT().show(s);
                        return null;
                    }
                });

                return null;
            }
        });


    }
}
