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
    private var initialized = false
    private val playbackState = VoicePlaybackState()
    private val foregroundListener = AppForegroundTracker.Listener { foreground ->
        playbackState.setForeground(foreground)
        if (!foreground) stopCurrentPlay()
    }
    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                playbackState.setFocusState(VoicePlaybackState.FocusState.GRANTED)
                mediaPlayer?.setVolume(1f, 1f)
                if (playbackState.canStart()) resumePlay()
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                playbackState.setFocusState(VoicePlaybackState.FocusState.LOST)
                stopCurrentPlay()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                playbackState.setFocusState(VoicePlaybackState.FocusState.DELAYED)
                pausePlay()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                mediaPlayer?.setVolume(0.3f, 0.3f)
            }
        }
    }

    @Synchronized
    fun initialize() {
        if (initialized) return
        initialized = true
        AppForegroundTracker.addListener(foregroundListener)
        playbackState.setForeground(AppForegroundTracker.isForeground)
    }

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

        if (!AppForegroundTracker.isForeground) {
            onPlayError?.invoke("应用位于后台，已取消语音提示")
            return
        }

        // 停止当前播放
        stopCurrentPlay()
        playbackState.reset(true)

        // 构建完整URL
        val voiceUrl = buildVoiceUrl(voiceId)
        
        try {
            // 初始化MediaPlayer
            mediaPlayer = MediaPlayer().apply {
                setAudioStreamType(AudioManager.STREAM_MUSIC)
                setDataSource(voiceUrl)
                prepareAsync()
                
                setOnPreparedListener { mp ->
                    if (!AppForegroundTracker.isForeground || mediaPlayer !== mp) {
                        stopCurrentPlay()
                        onPlayError?.invoke("应用已进入后台，语音提示已取消")
                        return@setOnPreparedListener
                    }

                    playbackState.onPrepared()
                    currentVoiceId = voiceId
                    when (requestAudioFocus(context.applicationContext)) {
                        AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> {
                            playbackState.setFocusState(VoicePlaybackState.FocusState.GRANTED)
                            startPreparedPlayer(mp, voiceId, onPlayError)
                        }
                        AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> {
                            playbackState.setFocusState(VoicePlaybackState.FocusState.DELAYED)
                        }
                        else -> {
                            playbackState.setFocusState(VoicePlaybackState.FocusState.LOST)
                            stopCurrentPlay()
                            onPlayError?.invoke("无法获取音频焦点")
                        }
                    }
                }
                
                setOnCompletionListener { mp ->
                    // 播放完成
                    if (mediaPlayer === mp) {
                        mp.release()
                        mediaPlayer = null
                        mIsPlaying = false
                        currentVoiceId = null
                        playbackState.reset(AppForegroundTracker.isForeground)
                        releaseAudioFocus()
                    }
                    onPlayComplete?.invoke()
                }
                
                setOnErrorListener { mp, what, extra ->
                    // 播放错误
                    if (mediaPlayer === mp) {
                        mp.release()
                        mediaPlayer = null
                        mIsPlaying = false
                        currentVoiceId = null
                        playbackState.reset(AppForegroundTracker.isForeground)
                        releaseAudioFocus()
                    }
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
            try {
                if (mp.isPlaying) mp.stop()
            } catch (_: IllegalStateException) {
                // The player may still be preparing; release is sufficient.
            }
            mp.release()
        }
        mediaPlayer = null
        mIsPlaying = false
        currentVoiceId = null
        playbackState.reset(AppForegroundTracker.isForeground)
        releaseAudioFocus()
    }

    /**
     * 暂停播放
     */
    fun pausePlay() {
        mediaPlayer?.let { mp ->
            try {
                if (mp.isPlaying) {
                    mp.pause()
                    mIsPlaying = false
                }
            } catch (_: IllegalStateException) {
            }
        }
    }

    /**
     * 恢复播放
     */
    fun resumePlay() {
        if (!playbackState.canStart()) return
        mediaPlayer?.let { mp ->
            try {
                if (!mp.isPlaying) {
                    mp.start()
                    mIsPlaying = true
                }
            } catch (_: IllegalStateException) {
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
    private fun requestAudioFocus(context: Context): Int {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(audioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .build()
            
            return audioManager?.requestAudioFocus(audioFocusRequest!!)
                ?: AudioManager.AUDIOFOCUS_REQUEST_FAILED
        } else {
            @Suppress("DEPRECATION")
            return audioManager?.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            ) ?: AudioManager.AUDIOFOCUS_REQUEST_FAILED
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
            audioManager?.abandonAudioFocus(audioFocusChangeListener)
        }
        audioFocusRequest = null
        audioManager = null
    }

    /**
     * 释放资源
     */
    fun release() {
        stopCurrentPlay()
        if (initialized) {
            AppForegroundTracker.removeListener(foregroundListener)
            initialized = false
        }
    }

    private fun startPreparedPlayer(
        player: MediaPlayer,
        voiceId: String,
        onPlayError: ((String) -> Unit)?
    ) {
        if (!playbackState.canStart()) return
        try {
            player.setVolume(1f, 1f)
            player.start()
            mIsPlaying = true
            currentVoiceId = voiceId
        } catch (error: IllegalStateException) {
            stopCurrentPlay()
            onPlayError?.invoke("语音播放器状态异常: ${error.message}")
        }
    }
}
