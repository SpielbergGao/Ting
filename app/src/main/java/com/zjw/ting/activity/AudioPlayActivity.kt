package com.zjw.ting.activity

import android.annotation.SuppressLint
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.zjw.ting.R
import com.zjw.ting.net.TingShuUtil
import es.dmoral.toasty.Toasty
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_audio_play.*
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.net.URLEncoder


class AudioPlayActivity : AppCompatActivity() {

    private var mMediaPlayer: IjkMediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_play)

        // videoPlayer.setUp("http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4", true, "测试视频");
        // var url = "http://180l.ysts8.com:8000/恐怖小说/我当算命先生那些年/014.mp3?1231710044742x1558968690x1231716175402-f002e814b9d51c55addf150d702074fc?3"
        loadData(onSuccess = {
            var url = handleUrl(it)
            Toasty.success(this@AudioPlayActivity, "url $it").show()
            videoPlayer.setUp(url, true, intent.getStringExtra("info"))
            videoPlayer.startPlayLogic()
        }, onError = {
            it.message?.let { msg -> Toasty.error(this@AudioPlayActivity, msg).show() }
        })
    }

    private fun handleUrl(url: String): String {
        var url1 = url
        val toCharArray = url1.toCharArray()
        toCharArray.forEachIndexed { _, c ->
            if (isChineseChar(c)) {
                url1 = url1.replace(c.toString(), URLEncoder.encode(c.toString(), "utf-8"))
            }
        }
        return url1
    }

    fun isChineseChar(c: Char): Boolean {
        return c.toString().matches("[\u4e00-\u9fa5]".toRegex())
    }

    override fun onPause() {
        super.onPause()
        videoPlayer.onVideoPause()
    }

    override fun onResume() {
        super.onResume()
        videoPlayer.onVideoResume()
    }


    @Throws(Exception::class)
    private fun playAudio(url: String) {
        if (mMediaPlayer != null) {
            releasePlayer()
        }

        mMediaPlayer = IjkMediaPlayer()
        mMediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mMediaPlayer?.dataSource = url
        mMediaPlayer?.prepareAsync()

        mMediaPlayer?.setOnSeekCompleteListener {

        }
        mMediaPlayer?.setOnCompletionListener {
            releasePlayer()
        }

        mMediaPlayer?.setOnPreparedListener {
            mMediaPlayer?.start()
        }
    }

    /**
     * 释放播放器
     */
    private fun releasePlayer() {
        mMediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
            mMediaPlayer = null
        }
        System.gc()


    }

    override fun onDestroy() {
        super.onDestroy()
        GSYVideoManager.releaseAllVideos()
    }

    @SuppressLint("CheckResult")
    private fun loadData(onSuccess: (url: String) -> Unit, onError: (e: Error) -> Unit) {
        Observable.create(ObservableOnSubscribe<String> {
            try {
                val url = TingShuUtil.getAudioUrl(intent.getStringExtra("url"))
                it.onNext(url)
                it.onComplete()
            } catch (e: Error) {
                it.onError(e)
                onError(e)
            }
        }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                onSuccess(it)
            }
    }
}
