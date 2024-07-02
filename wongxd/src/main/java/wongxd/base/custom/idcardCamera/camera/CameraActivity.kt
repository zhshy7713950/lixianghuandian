package wongxd.base.custom.idcardCamera.camera

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import com.wongxd.R
import wongxd.common.permission.PermissionType
import wongxd.common.permission.getPermissions
import wongxd.common.simpleForResult.SimpleOnActivityResult


/**
 * Created by wongxd on 2019/1/3.
 * https://github.com/wongxd
 * wxd1@live.com
 *
 *
 *
 *
 * 拍照界面
 */
class CameraActivity : Activity(), View.OnClickListener {

    private var customCameraPreview: CustomCameraPreview? = null
    private var containerView: View? = null
    private var cropView: ImageView? = null
    private var optionView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val type = intent.getIntExtra("type", 0)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        setContentView(R.layout.activity_camera)

        customCameraPreview = findViewById<View>(R.id.camera_surface) as CustomCameraPreview
        containerView = findViewById(R.id.camera_crop_container)
        cropView = findViewById<View>(R.id.camera_crop) as ImageView
        optionView = findViewById(R.id.camera_option)

        //获取屏幕最小边，设置为cameraPreview较窄的一边
        val screenMinSize =
            Math.min(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels).toFloat()
        //根据screenMinSize，计算出cameraPreview的较宽的一边，长宽比为标准的16:9
        val maxSize = screenMinSize / 9.0f * 16.0f
        val layoutParams: RelativeLayout.LayoutParams

        layoutParams = RelativeLayout.LayoutParams(maxSize.toInt(), screenMinSize.toInt())
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT)
        customCameraPreview!!.layoutParams = layoutParams

        val height = (screenMinSize * 0.75).toInt().toFloat()
        val width = (height * 75.0f / 47.0f).toInt().toFloat()
        val containerParams = LinearLayout.LayoutParams(width.toInt(), ViewGroup.LayoutParams.MATCH_PARENT)
        val cropParams = LinearLayout.LayoutParams(width.toInt(), height.toInt())
        containerView!!.layoutParams = containerParams
        cropView!!.layoutParams = cropParams
        when (type) {
            TYPE_ID_CARD_FRONT -> cropView!!.setImageResource(R.drawable.camera_front)
            TYPE_ID_CARD_BACK -> cropView!!.setImageResource(R.drawable.camera_back)
        }

        customCameraPreview!!.setOnClickListener(this)
        findViewById<View>(R.id.camera_close).setOnClickListener(this)
        findViewById<View>(R.id.camera_take).setOnClickListener(this)

    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.camera_surface) {
            customCameraPreview!!.focus()

        } else if (i == R.id.camera_close) {
            finish()

        } else if (i == R.id.camera_take) {
            takePhoto()
        }
    }

    private fun takePhoto() {
        optionView?.visibility = View.GONE
        customCameraPreview?.isEnabled = false
        customCameraPreview?.takePhoto { data, camera ->
            //子线程处理图片，防止ANR
            Thread(Runnable {
                var bitmap: Bitmap? = null
                if (data != null) {
                    bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                    camera.stopPreview()
                }
                if (bitmap != null) {
                    //计算裁剪位置
                    val left =
                        (containerView!!.left.toFloat() - customCameraPreview!!.left.toFloat()) / customCameraPreview!!.width.toFloat()
                    val top = cropView!!.top.toFloat() / customCameraPreview!!.height.toFloat()
                    val right = containerView!!.right.toFloat() / customCameraPreview!!.width.toFloat()
                    val bottom = cropView!!.bottom.toFloat() / customCameraPreview!!.height.toFloat()

                    //裁剪及保存到文件
                    val resBitmap = Bitmap.createBitmap(
                        bitmap,
                        (left * bitmap.width.toFloat()).toInt(),
                        (top * bitmap.height.toFloat()).toInt(),
                        ((right - left) * bitmap.width.toFloat()).toInt(),
                        ((bottom - top) * bitmap.height.toFloat()).toInt()
                    )

                    CustomCameraPreview.saveBitmap(resBitmap)

                    if (!bitmap.isRecycled) {
                        bitmap.recycle()
                    }
                    if (!resBitmap.isRecycled) {
                        resBitmap.recycle()
                    }

                    //拍照完成，返回对应图片路径
                    val intent = Intent()
                    intent.putExtra("result", CustomCameraPreview.getImgPath())
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }

                return@Runnable
            }).start()
        }
    }

    companion object {

        /**
         * 身份证正面
         */
        val TYPE_ID_CARD_FRONT = 1

        /**
         * 身份证反面
         */
        val TYPE_ID_CARD_BACK = 2

        /**
         * 跳转到拍照页面
         */
        fun navToCamera(fgt: Fragment, type: Int, callback: (String?) -> Unit) {

            getPermissions(
                listOf(
                    PermissionType.CAMERA,
                    PermissionType.WRITE_EXTERNAL_STORAGE,
                    PermissionType.READ_EXTERNAL_STORAGE
                )
            ) {


                val intent = Intent(fgt.activity, CameraActivity::class.java)
                intent.putExtra("type", type)

                SimpleOnActivityResult.SimpleForResult(fgt).startForResult(intent) { reqCoed, resultCode, data ->
                    if (data != null && resultCode == RESULT_OK) {
                        callback(data.getStringExtra("result"))
                    }
                }

            }
        }
    }
}
