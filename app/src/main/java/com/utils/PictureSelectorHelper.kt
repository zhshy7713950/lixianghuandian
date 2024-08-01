package com.utils

import android.app.Activity
import com.luck.picture.lib.basic.PictureSelectionModel
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.language.LanguageConfig

object PictureSelectorHelper {
    fun openGallery(activity: Activity, maxSelectNum: Int = 1): PictureSelectionModel {
        return PictureSelector.create(activity)
            .openGallery(SelectMimeType.ofImage())
            .setLanguage(LanguageConfig.SYSTEM_LANGUAGE)
            .setImageEngine(GlideEngine.createGlideEngine())
            .setMaxSelectNum(maxSelectNum)
            .isDisplayCamera(true)
            .setSkipCropMimeType(
                PictureMimeType.ofGIF(), PictureMimeType.ofWEBP(),
                PictureMimeType.ofBMP(), PictureMimeType.ofWapBMP(), PictureMimeType.ofXmsBMP()
            )
            .isDisplayCamera(true)
    }
}