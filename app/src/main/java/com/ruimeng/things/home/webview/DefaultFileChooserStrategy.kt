package com.ruimeng.things.home.webview

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 默认文件选择策略实现
 * 提供基本的图片和视频文件选择功能
 */
class DefaultFileChooserStrategy(
    private val activity: Activity,
    private val authority: String = "${activity.packageName}.fileprovider"
) : FileChooserStrategy {
    
    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1002
        private const val REQUEST_VIDEO_CAPTURE = 1003
        private const val REQUEST_PICK_IMAGE = 1004
        private const val REQUEST_PICK_VIDEO = 1005
    }
    
    // 文件选择回调
    private var fileChooserCallback: ((Array<Uri>) -> Unit)? = null
    
    // 临时文件URI
    private var tempImageUri: Uri? = null
    private var tempVideoUri: Uri? = null
    
    override fun handleFileChooser(
        webView: WebView?,
        fileChooserParams: WebChromeClient.FileChooserParams?,
        requestCode: Int
    ): Boolean {
        val acceptTypes = fileChooserParams?.acceptTypes
        val isCaptureEnabled = fileChooserParams?.isCaptureEnabled ?: false
        
        return when {
            acceptTypes?.any { it.startsWith("image/") } == true -> {
                if (isCaptureEnabled) {
                    openCameraForImage()
                } else {
                    openImagePicker()
                }
                true
            }
            acceptTypes?.any { it.startsWith("video/") } == true -> {
                if (isCaptureEnabled) {
                    openCameraForVideo()
                } else {
                    openVideoPicker()
                }
                true
            }
            else -> {
                // 默认打开图片选择器
                openImagePicker()
                true
            }
        }
    }
    
    override fun handleFileChooserResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ): Boolean {
        if (resultCode != Activity.RESULT_OK) {
            fileChooserCallback?.invoke(emptyArray())
            return false
        }
        
        val uri = when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> tempImageUri
            REQUEST_VIDEO_CAPTURE -> tempVideoUri
            REQUEST_PICK_IMAGE, REQUEST_PICK_VIDEO -> data?.data
            else -> null
        }
        
        return if (uri != null) {
            fileChooserCallback?.invoke(arrayOf(uri))
            true
        } else {
            fileChooserCallback?.invoke(emptyArray())
            false
        }
    }
    
    override fun getSupportedFileTypes(): List<String> {
        return listOf(
            "image/*",
            "video/*",
            "image/jpeg",
            "image/png",
            "image/gif",
            "video/mp4",
            "video/avi",
            "video/mov"
        )
    }
    
    override fun isFileTypeSupported(mimeType: String): Boolean {
        return getSupportedFileTypes().any { 
            it == mimeType || it.endsWith("/*") && mimeType.startsWith(it.substring(0, it.length - 1))
        }
    }
    
    /**
     * 设置文件选择回调
     * @param callback 回调函数
     */
    fun setFileChooserCallback(callback: (Array<Uri>) -> Unit) {
        this.fileChooserCallback = callback
    }
    
    /**
     * 打开相机拍照
     */
    private fun openCameraForImage() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            val photoFile = createImageFile()
            tempImageUri = FileProvider.getUriForFile(activity, authority, photoFile)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, tempImageUri)
            activity.startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    
    /**
     * 打开相机录制视频
     */
    private fun openCameraForVideo() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        try {
            val videoFile = createVideoFile()
            tempVideoUri = FileProvider.getUriForFile(activity, authority, videoFile)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, tempVideoUri)
            activity.startActivityForResult(intent, REQUEST_VIDEO_CAPTURE)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    
    /**
     * 打开图片选择器
     */
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        activity.startActivityForResult(intent, REQUEST_PICK_IMAGE)
    }
    
    /**
     * 打开视频选择器
     */
    private fun openVideoPicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        intent.type = "video/*"
        activity.startActivityForResult(intent, REQUEST_PICK_VIDEO)
    }
    
    /**
     * 创建图片文件
     */
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "IMG_$timeStamp"
        val storageDir = activity.getExternalFilesDir("Images")
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }
    
    /**
     * 创建视频文件
     */
    @Throws(IOException::class)
    private fun createVideoFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val videoFileName = "VID_$timeStamp"
        val storageDir = activity.getExternalFilesDir("Videos")
        return File.createTempFile(videoFileName, ".mp4", storageDir)
    }
    
    /**
     * 清理临时文件
     */
    override fun cleanup() {
        tempImageUri = null
        tempVideoUri = null
        fileChooserCallback = null
    }
}
