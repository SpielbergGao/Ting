package com.zjw.ting.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.blankj.rxbus.RxBus
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.trello.rxlifecycle3.android.lifecycle.kotlin.bindUntilEvent
import com.zjw.ting.R
import com.zjw.ting.bean.AudioHistory
import com.zjw.ting.bean.AudioHistorys
import com.zjw.ting.net.TingShuUtil
import com.zjw.ting.util.ACache
import es.dmoral.toasty.Toasty
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_audio_play.*
import top.defaults.drawabletoolbox.DrawableBuilder
import java.net.URLEncoder
import java.util.regex.Pattern


class AudioPlayActivity : AppCompatActivity(), LifecycleOwner {

    private var position: Int = 1

    @Volatile
    private var mAudioInfo: TingShuUtil.AudioInfo? = null

    private var mCurrentUrl: String = ""

    private var canChangeUrl = true

    private var episodesUrl: String = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_play)

        // videoPlayer.setUp("http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4", true, "测试视频");
        // var url = "http://180l.ysts8.com:8000/恐怖小说/我当算命先生那些年/014.mp3?1231710044742x1558968690x1231716175402-f002e814b9d51c55addf150d702074fc?3"
        position = intent.getIntExtra("position", 1)
        episodesUrl = intent.getStringExtra("url")
        loadData(episodesUrl, onSuccess = {
            setTitleAndPlay(it, true)
            videoPlayer.setVideoAllCallBack(object : GSYSampleCallBack() {
                override fun onAutoComplete(url: String?, vararg objects: Any?) {
                    playNext()
                }
            })
        }, onError = {
            it.message?.let { msg -> Toasty.error(this@AudioPlayActivity, msg).show() }
        })

        val preBtDrawable = DrawableBuilder()
            .rectangle()
            .rounded()
            .solidColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .solidColorPressed(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            .build()
        preBt.background = preBtDrawable
        preBt.setOnClickListener {
            if (position <= 1) {
                Toasty.info(this, "没有上一集哟~").show()
                return@setOnClickListener
            }
            if (!canChangeUrl) {
                Toasty.info(this@AudioPlayActivity, "重试获取资源中，无法切换集数~").show()
                return@setOnClickListener
            }
            mAudioInfo?.preUrl?.let { preUrl ->
                loadData(preUrl, onSuccess = {
                    setTitleAndPlay(it, false) {
                        episodesUrl = preUrl
                    }
                }, onError = {
                    it.message?.let { msg -> Toasty.error(this@AudioPlayActivity, msg).show() }
                })
            }
        }


        val nextBtDrawable = DrawableBuilder()
            .rectangle()
            .rounded()
            .solidColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .solidColorPressed(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            .build()
        nextBt.background = nextBtDrawable
        nextBt.setOnClickListener {
            if (!canChangeUrl) {
                Toasty.info(this@AudioPlayActivity, "重试获取资源中，无法切换集数~").show()
                return@setOnClickListener
            }
            /*if (TingShuUtil.countPage > 0 && (position >= TingShuUtil.countPage)) {
                Toasty.info(this, "没有下一集哟~").show()
                return@setOnClickListener
            }*/
            playNext()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun playNext() {
        mAudioInfo?.nextUrl?.let { nextUrl ->
            loadData(nextUrl, onSuccess = {
                setTitleAndPlay(it, false) {
                    episodesUrl = nextUrl
                }
            }, onError = {
                it.message?.let { msg -> Toasty.error(this@AudioPlayActivity, msg).show() }
            })
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setTitleAndPlay(it: TingShuUtil.AudioInfo, needSeekTo: Boolean, onSuccess: () -> Unit = {}) {
        GSYVideoManager.releaseAllVideos()
        if (this.isFinishing || this.isDestroyed) {
            return
        }

        titleTv.text = getTitleStr()
        Log.e("tag", it.url)


        mCurrentUrl = handleUrl(it.url)

        //无效播放源需要自动重试
        if (it.url.contains("180k.5txs.com")) {
            canChangeUrl = false
            Toasty.warning(this@AudioPlayActivity, "url ${it.url}").show()
            //原集数html地址
            mAudioInfo?.episodesUrl?.let { episodesUrl ->
                loadData(episodesUrl, onSuccess = {
                    setTitleAndPlay(it, needSeekTo)
                }, onError = {
                    it.message?.let { msg -> Toasty.error(this@AudioPlayActivity, msg).show() }
                })
            }

        } else {
            canChangeUrl = true
            var p = Pattern.compile("\\d+\\.mp3")
            var m = p.matcher(it.url)
            if (m.find()) {
                position = m.group(0).replace(".mp3", "").toInt()
            }
            onSuccess()
            titleTv.text = getTitleStr()
            Toasty.success(this@AudioPlayActivity, "url ${it.url}").show()
            videoPlayer.setUp(mCurrentUrl, true, "")
            if (needSeekTo) {
                videoPlayer.seekOnStart = intent.getLongExtra("currentPosition", 0)
            } else {
                videoPlayer.seekOnStart = 0
            }
            videoPlayer.startPlayLogic()
        }
    }

    private fun getTitleStr(): String = intent.getStringExtra("info") + " ===【第 $position 集】"

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
        GSYVideoManager.releaseAllVideos()
        super.onDestroy()
    }

    private fun setAudioHistory() {
        var history = ACache.get(this).getAsObject("history")
        if (history == null) {
            history = AudioHistorys()
        }
        history as AudioHistorys
        history.map[intent.getStringExtra("bookUrl")] =
            AudioHistory(
                getTitleStr(),
                videoPlayer.gsyVideoManager.currentPosition,
                intent.getStringExtra("bookUrl"),
                episodesUrl,
                position
            )
        ACache.get(this).put("history", history)
    }

    @SuppressLint("CheckResult")
    private fun loadData(episodesUrl: String, onSuccess: (url: TingShuUtil.AudioInfo) -> Unit, onError: (e: Throwable) -> Unit) {
        Observable.create(ObservableOnSubscribe<TingShuUtil.AudioInfo> {
            try {
                if (!this.isFinishing && !this.isDestroyed) {
                    mAudioInfo = TingShuUtil.getAudioUrl(episodesUrl)
                    it.onNext(mAudioInfo!!)
                    it.onComplete()
                }
            } catch (e: Throwable) {
                try {
                    it.onError(e)
                } catch (e: Throwable) {
                }
            }
        }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .bindUntilEvent(this@AudioPlayActivity, Lifecycle.Event.ON_STOP)
            .subscribe({
                onSuccess(it)
            }, {
                onError(it)
            })
    }

    override fun finish() {
        super.finish()
    }

    override fun onStop() {
        //记录当前播放进度
        setAudioHistory()
        // 发送 String 类型事件
        RxBus.getDefault().post(intent.getStringExtra("bookUrl"))
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        //记录当前播放进度
        setAudioHistory()
        // 发送 String 类型事件
        RxBus.getDefault().post(intent.getStringExtra("bookUrl"))
        super.onSaveInstanceState(outState)
    }
}
