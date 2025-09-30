package com.entity.local

/**
 * 语音开关请求参数
 */
data class VoiceOpenLocal(
    val userId: String,
    val isVoiceActived: String // 1-启用，0-关闭
)
