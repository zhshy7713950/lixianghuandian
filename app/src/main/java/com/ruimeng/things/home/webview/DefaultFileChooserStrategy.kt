package com.ruimeng.things.home.webview

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import wongxd.common.UriGrantCompat
import wongxd.common.permission.PermissionType
import wongxd.common.permission.getPermissions
import wongxd.common.simpleForResult.SimpleOnActivityResult
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 默认文件选择策略实现
 * 提供基本的图片和视频文件选择功能
 * 使用项目现有的权限框架和Activity结果处理框架
 */
class DefaultFileChooserStrategy(
    private val activity: AppCompatActivity,
    private val authority: String = "${activity.packageName}.fileprovider"
) : FileChooserStrategy {
    
    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1001
        private const val REQUEST_VIDEO_CAPTURE = 1002
        private const val REQUEST_PICK_IMAGE = 1003
        private const val REQUEST_PICK_VIDEO = 1004
    }
    
    // 文件选择回调
    private var fileChooserCallback: ((Array<Uri>) -> Unit)? = null
    
    // 临时文件URI
    private var tempImageUri: Uri? = null
    private var tempVideoUri: Uri? = null
    
    // 当前请求类型
    private var currentRequestType: Int = 0
    
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
                    currentRequestType = REQUEST_IMAGE_CAPTURE
                    checkCameraPermissionAndOpen()
                } else {
                    currentRequestType = REQUEST_PICK_IMAGE
                    openFilePicker()
                }
                true
            }
            acceptTypes?.any { it.startsWith("video/") } == true -> {
                if (isCaptureEnabled) {
                    currentRequestType = REQUEST_VIDEO_CAPTURE
                    checkCameraPermissionAndOpen()
                } else {
                    currentRequestType = REQUEST_PICK_VIDEO
                    openFilePicker()
                }
                true
            }
            else -> {
                // 默认打开图片选择器
                currentRequestType = REQUEST_PICK_IMAGE
                openFilePicker()
                true
            }
        }
    }
    
    /**
     * 检查相机权限并打开相机
     */
    private fun checkCameraPermissionAndOpen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity.checkSelfPermission(Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                // 使用项目现有的权限框架请求相机权限
                requestCameraPermission()
            }
        } else {
            openCamera()
        }
    }
    
    /**
     * 请求相机权限
     */
    private fun requestCameraPermission() {
        getPermissions(
            activity as androidx.fragment.app.FragmentActivity,
            PermissionType.CAMERA,
            result = { isAllGranted, perList ->
                if (isAllGranted) {
                    openCurrentRequest()
                } else {
                    // 权限被拒绝，通知用户取消选择
                    fileChooserCallback?.invoke(emptyArray())
                }
            },
            allGranted = {
                openCurrentRequest()
            }
        )
    }
    
    /**
     * 根据当前请求类型打开相应的功能
     */
    fun openCurrentRequest() {
        when (currentRequestType) {
            REQUEST_IMAGE_CAPTURE -> openCameraForImage()
            REQUEST_VIDEO_CAPTURE -> openCameraForVideo()
            REQUEST_PICK_IMAGE -> openImagePicker()
            REQUEST_PICK_VIDEO -> openVideoPicker()
        }
    }
    
    /**
     * 打开相机
     */
    private fun openCamera() {
        when (currentRequestType) {
            REQUEST_IMAGE_CAPTURE -> openCameraForImage()
            REQUEST_VIDEO_CAPTURE -> openCameraForVideo()
        }
    }
    
    /**
     * 打开文件选择器
     */
    private fun openFilePicker() {
        when (currentRequestType) {
            REQUEST_PICK_IMAGE -> openImagePicker()
            REQUEST_PICK_VIDEO -> openVideoPicker()
        }
    }

    override fun handleFileChooserResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ): Boolean {
        android.util.Log.d("DefaultFileChooserStrategy", "handleFileChooserResult: requestCode=$requestCode, resultCode=$resultCode, currentRequestType=$currentRequestType")
        
        if (resultCode != Activity.RESULT_OK) {
            android.util.Log.d("DefaultFileChooserStrategy", "Result not OK, canceling file selection")
            fileChooserCallback?.invoke(emptyArray())
            return false
        }
        
        // 根据当前请求类型和结果数据来确定返回的URI
        val uri = when (currentRequestType) {
            REQUEST_IMAGE_CAPTURE -> {
                android.util.Log.d("DefaultFileChooserStrategy", "Image capture result, tempImageUri: $tempImageUri")
                tempImageUri
            }
            REQUEST_VIDEO_CAPTURE -> {
                android.util.Log.d("DefaultFileChooserStrategy", "Video capture result, tempVideoUri: $tempVideoUri")
                tempVideoUri
            }
            REQUEST_PICK_IMAGE, REQUEST_PICK_VIDEO -> {
                android.util.Log.d("DefaultFileChooserStrategy", "File pick result, data?.data: ${data?.data}")
                data?.data
            }
            else -> {
                android.util.Log.d("DefaultFileChooserStrategy", "Unknown request type: $currentRequestType")
                null
            }
        }
        
        return if (uri != null) {
            android.util.Log.d("DefaultFileChooserStrategy", "Successfully got URI: $uri")
            fileChooserCallback?.invoke(arrayOf(uri))
            true
        } else {
            android.util.Log.d("DefaultFileChooserStrategy", "No URI available, canceling file selection")
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
     * 清理临时文件
     */
    override fun cleanup() {
        tempImageUri = null
        tempVideoUri = null
        fileChooserCallback = null
    }
    
    /**
     * 打开相机拍照
     */
    private fun openCameraForImage() {
        android.util.Log.d("DefaultFileChooserStrategy", "Opening camera for image capture")
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            val photoFile = createImageFile()
            tempImageUri = FileProvider.getUriForFile(activity, authority, photoFile)
            android.util.Log.d("DefaultFileChooserStrategy", "Created temp image URI: $tempImageUri")
            intent.putExtra(MediaStore.EXTRA_OUTPUT, tempImageUri)
            tempImageUri?.let { UriGrantCompat.grantReadWrite(activity, intent, it) }
            
            // 使用项目现有的SimpleOnActivityResult框架
            SimpleOnActivityResult.SimpleForResult(activity)
                .startForResult(intent) { requestCode, resultCode, data ->
                    android.util.Log.d("DefaultFileChooserStrategy", "Camera image result callback: requestCode=$requestCode, resultCode=$resultCode")
                    handleFileChooserResult(requestCode, resultCode, data)
                }
        } catch (e: IOException) {
            android.util.Log.e("DefaultFileChooserStrategy", "Error creating image file", e)
            e.printStackTrace()
        }
    }
    
    /**
     * 打开相机录制视频
     */
    private fun openCameraForVideo() {
        android.util.Log.d("DefaultFileChooserStrategy", "Opening camera for video capture")
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        try {
            val videoFile = createVideoFile()
            tempVideoUri = FileProvider.getUriForFile(activity, authority, videoFile)
            android.util.Log.d("DefaultFileChooserStrategy", "Created temp video URI: $tempVideoUri")
            intent.putExtra(MediaStore.EXTRA_OUTPUT, tempVideoUri)
            tempVideoUri?.let { UriGrantCompat.grantReadWrite(activity, intent, it) }
            
            // 使用项目现有的SimpleOnActivityResult框架
            SimpleOnActivityResult.SimpleForResult(activity)
                .startForResult(intent) { requestCode, resultCode, data ->
                    android.util.Log.d("DefaultFileChooserStrategy", "Camera video result callback: requestCode=$requestCode, resultCode=$resultCode")
                    handleFileChooserResult(requestCode, resultCode, data)
                }
        } catch (e: IOException) {
            android.util.Log.e("DefaultFileChooserStrategy", "Error creating video file", e)
            e.printStackTrace()
        }
    }
    
    /**
     * 打开图片选择器
     */
    private fun openImagePicker() {
        android.util.Log.d("DefaultFileChooserStrategy", "Opening image picker")
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        // 使用项目现有的SimpleOnActivityResult框架
        SimpleOnActivityResult.SimpleForResult(activity)
            .startForResult(intent) { requestCode, resultCode, data ->
                android.util.Log.d("DefaultFileChooserStrategy", "Image picker result callback: requestCode=$requestCode, resultCode=$resultCode")
                handleFileChooserResult(requestCode, resultCode, data)
            }
    }
    
    /**
     * 打开视频选择器
     */
    private fun openVideoPicker() {
        android.util.Log.d("DefaultFileChooserStrategy", "Opening video picker")
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "video/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        // 使用项目现有的SimpleOnActivityResult框架
        SimpleOnActivityResult.SimpleForResult(activity)
            .startForResult(intent) { requestCode, resultCode, data ->
                android.util.Log.d("DefaultFileChooserStrategy", "Video picker result callback: requestCode=$requestCode, resultCode=$resultCode")
                handleFileChooserResult(requestCode, resultCode, data)
            }
    }
    
    /**
     * 创建图片文件
     */
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "IMG_$timeStamp"
        val storageDir = File(activity.externalCacheDir ?: activity.cacheDir, "web_uploads")
        if (!storageDir.exists() && !storageDir.mkdirs()) {
            throw IOException("Unable to create web upload cache")
        }
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }
    
    /**
     * 创建视频文件
     */
    @Throws(IOException::class)
    private fun createVideoFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val videoFileName = "VID_$timeStamp"
        val storageDir = File(activity.externalCacheDir ?: activity.cacheDir, "web_uploads")
        if (!storageDir.exists() && !storageDir.mkdirs()) {
            throw IOException("Unable to create web upload cache")
        }
        return File.createTempFile(videoFileName, ".mp4", storageDir)
    }
}
