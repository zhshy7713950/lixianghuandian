package wongxd.base.custom.idcardCamera.camera;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by gxj on 2018/2/17 01:46.
 * 相机预览封装
 */
public class CustomCameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static String TAG = CustomCameraPreview.class.getName();

    /**
     * 打开相机
     */
    private static Camera openCamera() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    /**
     * Android相机的预览尺寸都是4:3或者16:9，这里遍历所有支持的预览尺寸，得到16:9的最大尺寸，保证成像清晰度
     *
     * @param sizes
     * @return 最佳尺寸
     */
    private static Camera.Size getBestSize(List<Camera.Size> sizes) {
        Camera.Size bestSize = null;
        for (Camera.Size size : sizes) {
            if ((float) size.width / (float) size.height == 16.0f / 9.0f) {
                if (bestSize == null) {
                    bestSize = size;
                } else {
                    if (size.width > bestSize.width) {
                        bestSize = size;
                    }
                }
            }
        }
        return bestSize;
    }

    private Camera mCamera;


    private static final File parentPath = Environment.getExternalStorageDirectory();
    private static String storagePath = "";
    private static final String DST_FOLDER_NAME = "IdCard";
    private static String imgPath;

    /**
     * 初始化保存路径
     *
     * @return
     */
    private static String initPath() {
        if (storagePath.equals("")) {
            storagePath = parentPath.getAbsolutePath() + "/" + DST_FOLDER_NAME;
            File f = new File(storagePath);
            if (!f.exists()) {
                f.mkdir();
            }
        }
        return storagePath;
    }

    /**
     * 保存Bitmap到sdcard
     *
     * @param b 得到的图片
     */
    public static boolean saveBitmap(Bitmap b) {
        String path = initPath();
        long dataTake = System.currentTimeMillis();
        imgPath = path + "/" + dataTake + ".jpg";
        try {
            FileOutputStream fout = new FileOutputStream(imgPath);
            BufferedOutputStream bos = new BufferedOutputStream(fout);
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @return 保存到sd卡的图片路径
     */
    public static String getImgPath() {
        return imgPath;
    }


    public CustomCameraPreview(Context context) {
        super(context);
        init();
    }

    public CustomCameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomCameraPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setKeepScreenOn(true);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        mCamera = openCamera();
        if (mCamera != null) {
            startPreview(holder);
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        mCamera.stopPreview();
        startPreview(holder);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        //回收释放资源
        release();
    }

    /**
     * 预览相机
     */
    private void startPreview(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
            Camera.Parameters parameters = mCamera.getParameters();
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                //竖屏拍照时，需要设置旋转90度，否者看到的相机预览方向和界面方向不相同
                mCamera.setDisplayOrientation(90);
                parameters.setRotation(90);
            } else {
                mCamera.setDisplayOrientation(0);
                parameters.setRotation(0);
            }
            Camera.Size bestSize = getBestSize(parameters.getSupportedPreviewSizes());
            if (bestSize != null) {
                parameters.setPreviewSize(bestSize.width, bestSize.height);
                parameters.setPictureSize(bestSize.width, bestSize.height);
            } else {
                parameters.setPreviewSize(1920, 1080);
                parameters.setPictureSize(1920, 1080);
            }
            mCamera.setParameters(parameters);
            mCamera.startPreview();
            focus();
        } catch (Exception e) {
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    mCamera.setDisplayOrientation(90);
                    parameters.setRotation(90);
                } else {
                    mCamera.setDisplayOrientation(0);
                    parameters.setRotation(0);
                }
                mCamera.setParameters(parameters);
                mCamera.startPreview();
                focus();
            } catch (Exception e1) {
                e.printStackTrace();
                mCamera = null;
            }
        }
    }

    /**
     * 释放资源
     */
    private void release() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 对焦，在CameraActivity中触摸对焦
     */
    public void focus() {
        if (mCamera != null) {
            mCamera.autoFocus(null);
        }
    }

    /**
     * 拍摄照片
     *
     * @param pictureCallback 在pictureCallback处理拍照回调
     */
    public void takePhoto(Camera.PictureCallback pictureCallback) {
        if (mCamera != null) {
            mCamera.takePicture(null, null, pictureCallback);
        }
    }
}
