package com.ruimeng.things

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect

import androidx.appcompat.app.AppCompatActivity
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import wongxd.common.EasyToast
import wongxd.common.getSweetDialog
import wongxd.common.permission.PermissionType
import wongxd.common.permission.getPermissions
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * Created by wongxd on 2019/2/16.
 */


/**
 * @param maxSize 压缩图片的最大尺寸 **kb
 */
fun zipImg(
    appCompatActivity: AppCompatActivity,
    imgPath: String,
    maxSize: Int = 800,
    failed: () -> Unit = { EasyToast.DEFAULT.show("压缩图片失败") },
    success: (String) -> Unit
) {
    val dlgProgress: SweetAlertDialog =
        getSweetDialog(appCompatActivity, SweetAlertDialog.PROGRESS_TYPE, "压缩图片中")
    dlgProgress.setCancelable(true)
    dlgProgress.show()
    getPermissions(
        appCompatActivity,
        PermissionType.WRITE_EXTERNAL_STORAGE,
        PermissionType.READ_EXTERNAL_STORAGE,
        allGranted = {
            realZipImg(appCompatActivity, imgPath, {
                dlgProgress.dismissWithAnimation()
                failed()
            }, {
                dlgProgress.dismissWithAnimation()
                success(it)
            }, maxSize)
        })

}

/**
 * 传入原始图片地址，获得一张压缩后的图片地址
 */
private fun realZipImg(
    appCompatActivity: AppCompatActivity,
    imgPath: String,
    failed: () -> Unit,
    success: (String) -> Unit,
    maxSize: Int
) {


    val fileDirName =
        appCompatActivity.application?.externalCacheDir?.absolutePath + File.separator + "zip_images"//应用缓存地址

    val dirFile = File(fileDirName)
    if (!dirFile.exists()) {
        dirFile.mkdirs()
    }
    val scaleImgPath =
        File(dirFile, String.format("img_%d.jpg", System.currentTimeMillis())).absolutePath

    val outPutFile = File(scaleImgPath)
    if (!outPutFile.exists())
        outPutFile.mkdirs()
    if (outPutFile.exists()) outPutFile.delete()


    val width = Resources.getSystem().displayMetrics.widthPixels
    val height = Resources.getSystem().displayMetrics.heightPixels
    val tempWidth = Math.min(width, 1080)
    val tempHeight = Math.min(height, 1920)


    //压缩图片
    val bmp = decodeSampledBitmapFromFile(imgPath, tempWidth, tempHeight)


    bmp?.let {

        appCompatActivity.doAsync {
            compressBitmapToFile(bmp, File(scaleImgPath), maxSize)
            uiThread {
                bmp.recycle()
                success.invoke(scaleImgPath)
            }
        }
    } ?: failed.invoke()
}


/**
 * 计算本机展示的缩略尺寸
 */
private fun calculateInSampleSize(
    options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int
): Int {
    // Raw height and width of image
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        if (width > height) {
            inSampleSize = Math.round(height.toFloat() / reqHeight.toFloat())
        } else {
            inSampleSize = Math.round(width.toFloat() / reqWidth.toFloat())
        }
    }
    return inSampleSize
}


private fun decodeSampledBitmapFromFile(
    filePath: String,
    reqWidth: Int, reqHeight: Int
): Bitmap? {

    // First decode with inJustDecodeBounds=true to check dimensions
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(filePath, options)

    // Calculate inSampleSize
    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)


    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false
    return BitmapFactory.decodeFile(filePath, options)
}


/**
 * @param maxSize **kb
 */
private fun compressBitmapToFile(bmp: Bitmap, file: File, maxSize: Int) {

    val (newWidth, newHeight) = Pair(bmp.width, bmp.height)

    // 尺寸压缩倍数,值越大，图片尺寸越小
    // 压缩Bitmap到对应尺寸
    val result = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)
    val rect = Rect(0, 0, newWidth, newHeight)
    canvas.drawBitmap(bmp, null, rect, null)

    val baos = ByteArrayOutputStream()
    // 把压缩后的数据存放到baos中
    var options = 80 //个人喜欢从80开始,
    result.compress(Bitmap.CompressFormat.JPEG, options, baos)
    while (baos.size() / 1024 > maxSize) { //大于 maxSize kb 继续压缩
        baos.reset()
        options -= 10
        result.compress(Bitmap.CompressFormat.JPEG, options, baos)
    }
    try {
        val fos = FileOutputStream(file)
        fos.write(baos.toByteArray())
        fos.flush()
        fos.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
