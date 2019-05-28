package com.zjw.ting.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.zjw.ting.R
import com.zjw.ting.net.TingShuUtil
import es.dmoral.toasty.Toasty
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_audio_play.*
import java.net.URLEncoder


class AudioPlayActivity : AppCompatActivity() {

    private var position: Int = 1
    private var mAudioInfo: TingShuUtil.AudioInfo? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_play)

        // videoPlayer.setUp("http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4", true, "测试视频");
        // var url = "http://180l.ysts8.com:8000/恐怖小说/我当算命先生那些年/014.mp3?1231710044742x1558968690x1231716175402-f002e814b9d51c55addf150d702074fc?3"
        position = intent.getIntExtra("position", 1)
        loadData(intent.getStringExtra("url"), onSuccess = {
            setTitleAndPlay(it)
            videoPlayer.setVideoAllCallBack(object : GSYSampleCallBack() {
                override fun onAutoComplete(url: String?, vararg objects: Any?) {
                    playNext()
                }
            })
        }, onError = {
            it.message?.let { msg -> Toasty.error(this@AudioPlayActivity, msg).show() }
        })

        preBt.setOnClickListener {
            if (position <= 1) {
                return@setOnClickListener
            }
            mAudioInfo?.preUrl?.let { preUrl ->
                loadData(preUrl, onSuccess = {
                    GSYVideoManager.releaseAllVideos()
                    position--
                    setTitleAndPlay(it)
                }, onError = {
                    it.message?.let { msg -> Toasty.error(this@AudioPlayActivity, msg).show() }
                })
            }
        }

        nextBt.setOnClickListener {
            playNext()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun playNext() {
        mAudioInfo?.nextUrl?.let { nextUrl ->
            loadData(nextUrl, onSuccess = {
                GSYVideoManager.releaseAllVideos()
                position++
                setTitleAndPlay(it)
            }, onError = {
                it.message?.let { msg -> Toasty.error(this@AudioPlayActivity, msg).show() }
            })
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setTitleAndPlay(it: TingShuUtil.AudioInfo) {
        Toasty.success(this@AudioPlayActivity, "url ${it.url}").show()
        var url = handleUrl(it.url)
        titleTv.text = intent.getStringExtra("info") + " ===【第 $position 集】"
        videoPlayer.setUp(url, true, "")
        videoPlayer.startPlayLogic()
    }

    //因为播放器不接受url中带有中文，因此需要做处理
    private fun handleUrl(url: String): String {
        var realUrl = url
        val toCharArray = realUrl.toCharArray()
        toCharArray.forEachIndexed { _, c ->
            if (isChineseChar(c)) {
                realUrl = realUrl.replace(c.toString(), URLEncoder.encode(c.toString(), "utf-8"))
            }
        }
        return realUrl
    }

    private fun isChineseChar(c: Char): Boolean {
        return c.toString().matches("[\u4e00-\u9fa5]".toRegex())
    }

    override fun onPause() {
        super.onPause()
        // videoPlayer.onVideoPause()
    }

    override fun onResume() {
        super.onResume()
        // videoPlayer.onVideoResume()
    }


    override fun onDestroy() {
        super.onDestroy()
        GSYVideoManager.releaseAllVideos()
    }

    @SuppressLint("CheckResult")
    private fun loadData(episodesUrl: String, onSuccess: (url: TingShuUtil.AudioInfo) -> Unit, onError: (e: Exception) -> Unit) {
        Observable.create(ObservableOnSubscribe<TingShuUtil.AudioInfo> {
            try {
                mAudioInfo = TingShuUtil.getAudioUrl(episodesUrl)
                it.onNext(mAudioInfo!!)
                it.onComplete()
            } catch (e: Exception) {
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
