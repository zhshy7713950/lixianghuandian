package wongxd.common.image

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import java.io.*



//
//<provider
//android:name="android.support.v4.content.FileProvider"
//android:authorities="${applicationId}.fileprovider"
//android:exported="false"
//android:grantUriPermissions="true">
//<!--元数据-->
//<meta-data
//android:name="android.support.FILE_PROVIDER_PATHS"
//android:resource="@xml/file_paths" />
//<!--配置中的authorities按照江湖规矩一般加上包名,${applicationId}是获取当前项目的包名，-->
//<!--前提是defaultConfig｛｝闭包中要有applicationId属性。-->
//<!--defaultConfig {-->
//    <!--applicationId "com.xx.xx"-->
//    <!--}-->
//</provider>



//<?xml version="1.0" encoding="utf-8"?>
//<resources xmlns:tools="http://schemas.android.com/tools"
//tools:ignore="MissingDefaultResource">
//<paths>
//<external-path
//name="my_images"
//path="" />
//<!--files-path：          该方式提供在应用的内部存储区的文件/子目录的文件。-->
//<!--它对应Context.getFilesDir返回的路径：eg:”/data/data/com.***.***/files”。-->
//
//<!--cache-path：          该方式提供在应用的内部存储区的缓存子目录的文件。-->
//<!--它对应Context.getCacheDir返回的路:eg:“/data/data/com.***.***/cache”；-->
//
//<!--external-path：       该方式提供在外部存储区域根目录下的文件。-->
//<!--它对应Environment.getExternalStorageDirectory返回的路径-->
//
//<!--external-files-path:  Context.getExternalFilesDir(null)-->
//
//<!--external-cache-path： Context.getExternalCacheDir(String)-->
//<!--上述代码中path=”“，是有特殊意义的，它代码根目录，也就是说你可以向其它的应用共享根目录及其子目录下任何一个文件了，
//如果你将path设为path=”pictures”， 那么它代表着根目录下的pictures目录(eg:/storage/emulated/0/pictures)，
//如果你向其它应用分享pictures目录范围之外的文件是不行的。-->
//</paths>
//</resources>


/**
 * 图片裁剪
 *
 * Created by wongxd on 2018/12/25.
 * https://github.com/wongxd
 * wxd1@live.com
 */

class DealImg(val ctx: Context) {


    companion object {

        fun getOne(activity: AppCompatActivity): DealImg {
            val one = DealImg(activity.applicationContext)
            one.mDealImgFgt = one.getOnResultFragment(activity.supportFragmentManager)
            return one
        }

        fun getOne(fragment: Fragment): DealImg {
            val one = DealImg(fragment.activity?.applicationContext!!)
            one.mDealImgFgt = one.getOnResultFragment(fragment.childFragmentManager)
            return one
        }
    }

    private var takePhotoImgPath = ""

    private var cropImgPath = ""


    private val REQ_ZOOM = 102
    private val REQ_TAKE_PHOTO = 100
    private val REQ_ALBUM = 101


    //裁剪
    private var scale: Boolean = false

    private var outputX: Float = 480f
    private var outputY: Float = 800f

    private var aspectX: Float = 0.1f
    private var aspectY: Float = 0.1f


    val dirPath: String = Environment.getExternalStorageDirectory().absolutePath + File.separator +
            ctx.applicationContext.packageName

    fun setCropScale(scale: Boolean): DealImg {
        this.scale = scale
        return this
    }


    fun setCropSize(cropX: Float, cropY: Float): DealImg {
        this.outputX = cropX
        this.outputY = cropY

        return this
    }


    fun setCropAspect(aspectX: Float, aspectY: Float): DealImg {
        this.aspectX = aspectX
        this.aspectY = aspectY

        return this
    }

    fun takePhoto(
        isCrop: Boolean = true,
        callback: (String) -> Unit
    ) {


        val dir = File(dirPath)
        if (!dir.exists()) {
            dir.mkdirs()
        }


        val file = File(dir, "take.jpg")

        takePhotoImgPath = file.absolutePath

        if (!file.exists()) {
            file.createNewFile()
        } else file.delete()


        // 指定调用相机拍照后照片的储存路径
        val imgFile = File(takePhotoImgPath)
        var imgUri: Uri? = null
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (Build.VERSION.SDK_INT >= 24) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            //如果是7.0或以上，使用getUriForFile()获取文件的Uri
            imgUri = FileProvider.getUriForFile(
                ctx,
                ctx.applicationContext.packageName + ".fileprovider",
                imgFile
            )
        } else {
            imgUri = Uri.fromFile(imgFile)
        }

        intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri)
        mDealImgFgt?.req(REQ_TAKE_PHOTO, intent) { requestCode, resultCode, data ->
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "takePhoto:$takePhotoImgPath")
                if (isCrop) {
                    cropPhoto(takePhotoImgPath, scale, aspectX, aspectY, outputX, outputY, callback)
                } else {
                    callback.invoke(takePhotoImgPath)
                }
            }
        }

    }

    fun pickPhoto(isCrop: Boolean = true, callback: (String) -> Unit) {
        val intent = Intent(Intent.ACTION_PICK, null)
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        mDealImgFgt?.req(REQ_ALBUM, intent) { requestCode, resultCode, data ->
            if (resultCode == Activity.RESULT_OK) {

                if (data != null) {
                    val sourceUri = data.data
                    val img_url = getFilePathFromUri(ctx, sourceUri)//这是本机的图片路径
                    Log.d(TAG, "pickPhoto:$img_url")
                    img_url?.let {
                        if (isCrop) {
                            cropPhoto(img_url, scale, aspectX, aspectY, outputX, outputY, callback)
                        } else {
                            callback.invoke(img_url)
                        }
                    }
                }

            }
        }
    }


    /**
     * 发起剪裁图片的请求 兼容7.0
     *
     */
    fun cropPhoto(
        srcImgPath: String,
        scale: Boolean = false,
        aspectX: Float = 0.1F,
        aspectY: Float = 0.1F,
        outputX: Float = 480f,
        outputY: Float = 800f,
        callback: (String) -> Unit
    ) {

        val srcFile = File(srcImgPath)
        val srcFileName = srcFile.name

        val cropFileName = "crop_$srcFileName"
        val dir = File(dirPath)
        val cropFile = File(dir, cropFileName)
        cropImgPath = cropFile.absolutePath

        if (!dir.exists()) {
            dir.mkdirs()
        }

        if (cropFile.exists())
            cropFile.delete()
        else
            cropFile.createNewFile()


        val intent = Intent("com.android.camera.action.CROP")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) //添加这一句表示对目标应用临时授权该Uri所代表的文件
        }

        intent.setDataAndType(getUriFromFile(ctx, srcFile), "image/*")

        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra("crop", "true")

        intent.putExtra("scale", scale)


        // aspectX aspectY 是宽高的比例
        if (Build.MANUFACTURER.equals("HUAWEI")) {
            //华为特殊处理 不然会显示圆
            intent.putExtra("aspectX", 9998)
            intent.putExtra("aspectY", 9999)
        } else {
            intent.putExtra("aspectX", aspectX)
            intent.putExtra("aspectY", aspectY)
        }

        // outputX,outputY 是剪裁图片的宽高
        intent.putExtra("outputX", outputX)
        intent.putExtra("outputY", outputY)

        intent.putExtra("return-data", false)// true:不返回uri，false：返回uri
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cropFile)) //这里不能使用  FileProvider.getUriForFile
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())



        mDealImgFgt?.req(REQ_ZOOM, intent) { reqCode, resultCode, data ->
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    Log.d(TAG, "cropPhoto:$cropImgPath")
                    //bm可以用于显示在对应的ImageView中，scaleImgPath是剪裁并压缩后的图片的路径，可以用于上传操作
                    //实现自己的业务逻辑
                    callback.invoke(cropImgPath)

                } else {
                    Log.e(TAG, "选择图片发生错误，图片可能已经移位或删除")
                }
            }
        }
    }


    fun getUriFromFile(ctx: Context, file: File): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Intent.FLAG_GRANT_READ_URI_PERMISSION
            FileProvider.getUriForFile(ctx, ctx.applicationContext.packageName + ".fileprovider", file)
        } else {
            Uri.fromFile(file)
        }
    }


    fun getFilePathFromUri(context: Context, uri: Uri?): String? {
        if (null == uri)
            return null

        val scheme = uri.scheme
        var data: String? = null

        if (scheme == null)
            data = uri.path
        else if (ContentResolver.SCHEME_FILE == scheme) {
            data = uri.path
        } else if (ContentResolver.SCHEME_CONTENT == scheme) {
            val cursor = context.contentResolver.query(
                uri,
                arrayOf(MediaStore.Images.ImageColumns.DATA),
                null,
                null,
                null
            )

            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                    if (index > -1) {
                        data = cursor.getString(index)
                    }
                }
                cursor.close()
            }
        }

        return data;
    }


    /**
     * 安卓7.0 根据文件路径获取uri
     *
     * @param context
     * @param imageFile 原文件的File
     * @return 文件uri
     */
    fun getImageContentUri(context: Context, imageFile: File): Uri? {
        val filePath = imageFile.absolutePath;
        val cursor = context.getContentResolver().query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media._ID),
            MediaStore.Images.Media.DATA + "=? ",
            arrayOf(filePath), null
        )

        if (cursor != null && cursor.moveToFirst()) {
            val id = cursor.getInt(
                cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID)
            )
            val baseUri = Uri.parse("content://media/external/images/media")
            return Uri.withAppendedPath(baseUri, "" + id)
        } else {
            if (imageFile.exists()) {
                val values = ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath)
                return context.getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                )
            } else {
                return null
            }
        }
    }


    fun decodeUriAsBitmap(context: Context, uri: Uri?): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            // 先通过getContentResolver方法获得一个ContentResolver实例，
            // 调用openInputStream(Uri)方法获得uri关联的数据流stream
            // 把上一步获得的数据流解析成为bitmap
            bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri!!))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return null
        }
        return bitmap
    }

    /**
     * 按质量压缩bm
     * 是对图片进行压缩，第一个参数传入的是图片的Bitmap对象
     * ，第二个参数是压缩的保留率，比如使用的是80
     * ，即压缩后为原来的80%，则是对其压缩了20%
     *
     * @param bm
     * @param quality 压缩保存率
     * @return
     */
    fun saveBitmapByQuality(bm: Bitmap, outputPath: String, quality: Int): String {

        try {
            val f = File(outputPath)

            if (f.exists()) {
                f.delete()
            }
            val out = FileOutputStream(f)
            bm.compress(Bitmap.CompressFormat.JPEG, quality, out)
            out.flush()
            out.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return outputPath
    }

    /**
     * 保存图片
     *
     * @param src     源图片
     * @param file    要保存到的文件
     * @param format  格式
     * @param recycle 是否回收
     * @return `true`: 成功<br></br>`false`: 失败
     */
    fun saveBitmapAsJpg(
        src: Bitmap,
        savePath: String,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
        recycle: Boolean = true
    ): Boolean {
        var os: OutputStream? = null
        var ret = false
        try {
            os = BufferedOutputStream(FileOutputStream(File(savePath)))
            ret = src.compress(format, 100, os)
            if (recycle && !src.isRecycled) src.recycle()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            os?.close()
        }
        return ret
    }


    private val TAG = "DealImg"
    private var mDealImgFgt: DealImgFgt? = null


    private fun getOnResultFragment(fragmentManager: FragmentManager): DealImgFgt {
        var dealImgFgt: DealImgFgt? = finddealImgFgt(fragmentManager)
        if (dealImgFgt == null) {
            dealImgFgt = DealImgFgt()
            fragmentManager
                .beginTransaction()
                .add(dealImgFgt, TAG)
                .commitAllowingStateLoss()
            fragmentManager.executePendingTransactions()
        }
        return dealImgFgt
    }

    private fun finddealImgFgt(fragmentManager: FragmentManager): DealImgFgt? {
        return fragmentManager.findFragmentByTag(TAG) as DealImgFgt?
    }


    class DealImgFgt : Fragment() {

        private val mCallbacks: MutableMap<Int, DealImgActivityResultCallback> = mutableMapOf()


        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            retainInstance = true
        }

        fun req(requestCode: Int, intent: Intent, callback: DealImgActivityResultCallback) {
            startActivityForResult(intent, requestCode)
            mCallbacks[requestCode] = callback
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            val callback = mCallbacks.remove(requestCode)
            callback?.invoke(requestCode, resultCode, data)
        }


    }

}

/**
 *
 * requestCode: Int, resultCode: Int, data: Intent?
 *
 */
private typealias  DealImgActivityResultCallback = (Int, Int, Intent?) -> Unit





