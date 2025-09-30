package com.ruimeng.things.voice

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import com.ruimeng.things.UserInfoLiveData
import wongxd.common.EasyToast
import java.io.IOException

/**
 * 语音播放管理器单例
 * 负责管理全局语音播放，支持网络URL播放和语音开关控制
 */
class VoicePlayerManager private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: VoicePlayerManager? = null

        fun getInstance(): VoicePlayerManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: VoicePlayerManager().also { INSTANCE = it }
            }
        }

        // 语音URL基础路径
        private const val VOICE_BASE_URL = "https://downxll.oss-cn-beijing.aliyuncs.com/wxmin/voice/"
        private const val VOICE_EXTENSION = ".mp3"
    }

    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var mIsPlaying: Boolean = false
    private var currentVoiceId: String? = null

    /**
     * 播放语音
     * @param context 上下文
     * @param voiceId 语音ID（如：fail-1）
     * @param onPlayComplete 播放完成回调（可选）
     * @param onPlayError 播放错误回调（可选）
     */
    fun playVoice(
        context: Context,
        voiceId: String,
        onPlayComplete: (() -> Unit)? = null,
        onPlayError: ((String) -> Unit)? = null
    ) {
        // 检查语音开关状态
        if (!isVoiceActivated()) {
            onPlayError?.invoke("语音提示已关闭")
            return
        }

        // 停止当前播放
        stopCurrentPlay()

        // 构建完整URL
        val voiceUrl = buildVoiceUrl(voiceId)
        
        try {
            // 初始化MediaPlayer
            mediaPlayer = MediaPlayer().apply {
                setAudioStreamType(AudioManager.STREAM_MUSIC)
                setDataSource(voiceUrl)
                prepareAsync()
                
                setOnPreparedListener { mp ->
                    // 请求音频焦点
                    requestAudioFocus(context)
                    // 开始播放
                    mp.start()
                    mIsPlaying = true
                    currentVoiceId = voiceId
                }
                
                setOnCompletionListener { mp ->
                    // 播放完成
                    mIsPlaying = false
                    currentVoiceId = null
                    releaseAudioFocus()
                    onPlayComplete?.invoke()
                }
                
                setOnErrorListener { mp, what, extra ->
                    // 播放错误
                    mIsPlaying = false
                    currentVoiceId = null
                    releaseAudioFocus()
                    val errorMsg = "语音播放失败: $what"
                    onPlayError?.invoke(errorMsg)
                    true
                }
            }
        } catch (e: IOException) {
            onPlayError?.invoke("语音URL无效: ${e.message}")
        } catch (e: Exception) {
            onPlayError?.invoke("播放异常: ${e.message}")
        }
    }

    /**
     * 停止当前播放
     */
    fun stopCurrentPlay() {
        mediaPlayer?.let { mp ->
            if (mp.isPlaying) {
                mp.stop()
            }
            mp.release()
        }
        mediaPlayer = null
        mIsPlaying = false
        currentVoiceId = null
        releaseAudioFocus()
    }

    /**
     * 暂停播放
     */
    fun pausePlay() {
        mediaPlayer?.let { mp ->
            if (mp.isPlaying) {
                mp.pause()
                mIsPlaying = false
            }
        }
    }

    /**
     * 恢复播放
     */
    fun resumePlay() {
        mediaPlayer?.let { mp ->
            if (!mp.isPlaying) {
                mp.start()
                mIsPlaying = true
            }
        }
    }

    /**
     * 检查是否正在播放
     */
    fun isPlaying(): Boolean = mIsPlaying

    /**
     * 获取当前播放的语音ID
     */
    fun getCurrentVoiceId(): String? = currentVoiceId

    /**
     * 检查语音开关是否激活
     */
    private fun isVoiceActivated(): Boolean {
        return UserInfoLiveData.getCurrentVoiceSwitchStatus()
    }

    /**
     * 构建完整的语音URL
     */
    private fun buildVoiceUrl(voiceId: String): String {
        return "$VOICE_BASE_URL$voiceId$VOICE_EXTENSION"
    }

    /**
     * 请求音频焦点
     */
    private fun requestAudioFocus(context: Context) {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener { focusChange ->
                    when (focusChange) {
                        AudioManager.AUDIOFOCUS_GAIN -> resumePlay()
                        AudioManager.AUDIOFOCUS_LOSS -> pausePlay()
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pausePlay()
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                            // 降低音量而不是暂停
                            mediaPlayer?.setVolume(0.3f, 0.3f)
                        }
                    }
                }
                .build()
            
            audioManager?.requestAudioFocus(audioFocusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }

    /**
     * 释放音频焦点
     */
    private fun releaseAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { request ->
                audioManager?.abandonAudioFocusRequest(request)
            }
        } else {
            @Suppress("DEPRECATION")
            audioManager?.abandonAudioFocus(null)
        }
        audioFocusRequest = null
        audioManager = null
    }

    /**
     * 释放资源
     */
    fun release() {
        stopCurrentPlay()
        releaseAudioFocus()
    }
}
