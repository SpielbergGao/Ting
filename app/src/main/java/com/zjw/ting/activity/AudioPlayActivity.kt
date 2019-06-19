package com.zjw.ting.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.blankj.rxbus.RxBus
import com.google.common.net.UrlEscapers
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.trello.rxlifecycle3.android.lifecycle.kotlin.bindUntilEvent
import com.zjw.ting.R
import com.zjw.ting.bean.AudioHistory
import com.zjw.ting.bean.AudioHistorys
import com.zjw.ting.bean.AudioInfo
import com.zjw.ting.bean.Event
import com.zjw.ting.net.TingShuUtil
import com.zjw.ting.notification.*
import com.zjw.ting.notification.NotificationGenerator.Companion.NOTIFY_NEXT
import com.zjw.ting.notification.NotificationGenerator.Companion.NOTIFY_PLAY
import com.zjw.ting.notification.NotificationGenerator.Companion.NOTIFY_PREVIOUS
import com.zjw.ting.notification.NotificationGenerator.Companion.NOTIFY_STOP
import com.zjw.ting.notification.NotificationGenerator.Companion.START_SERVICE
import com.zjw.ting.util.ACache
import es.dmoral.toasty.Toasty
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_audio_play.*
import top.defaults.drawabletoolbox.DrawableBuilder


class AudioPlayActivity : AppCompatActivity(), LifecycleOwner {

    @Volatile
    private var mAudioInfo: AudioInfo? = null
    private var position: Int = 1
    private var mCurrentUrl: String = ""
    private var canChangeUrl = true
    private var episodesUrl: String = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        savedInstanceState?.apply {
            val bundleIntent = Intent()
            bundleIntent.putExtra("url", savedInstanceState.getString("url"))
            bundleIntent.putExtra("position", savedInstanceState.getInt("position"))
            bundleIntent.putExtra("currentPosition", savedInstanceState.getLong("currentPosition"))
            bundleIntent.putExtra("bookUrl", savedInstanceState.getString("bookUrl"))
            bundleIntent.putExtra("info", savedInstanceState.getString("info"))
            intent = bundleIntent
        }

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
            playPre()
        }


        val nextBtDrawable = DrawableBuilder()
            .rectangle()
            .rounded()
            .solidColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .solidColorPressed(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            .build()

        nextBt.background = nextBtDrawable
        nextBt.setOnClickListener {
            playNext()
        }

        // 注册 String 类型事件
        RxBus.getDefault().subscribeSticky(this, object : RxBus.Callback<Event.ServiceEvent>() {
            override fun onEvent(event: Event.ServiceEvent) {
                when (event.action) {
                    NOTIFY_PREVIOUS -> {
                        playPre()
                    }
                    NOTIFY_PLAY -> {
                        videoPlayer.gsyVideoManager?.let {
                            if (videoPlayer.gsyVideoManager.isPlaying) {
                                onVideoPause()
                            } else {
                                onVideoResume()
                            }
                        }

                    }
                    NOTIFY_NEXT -> {
                        playNext()
                    }
                    NOTIFY_STOP -> {
                    }
                }
            }
        })
    }

    private fun playPre() {
        if (position <= 1) {
            Toasty.info(this, "没有上一集哟~").show()
            return
        }
        if (!canChangeUrl) {
            Toasty.info(this@AudioPlayActivity, "重试获取资源中，无法切换集数~").show()
            return
        }
        mAudioInfo?.preUrl?.let { preUrl ->
            loadData(preUrl, onSuccess = {
                episodesUrl = preUrl
                setTitleAndPlay(it, false)
            }, onError = {
                it.message?.let { msg -> Toasty.error(this@AudioPlayActivity, msg).show() }
            })
        }
    }

    @SuppressLint("SetTextI18n")
    private fun playNext() {
        if (!canChangeUrl) {
            Toasty.info(this@AudioPlayActivity, "重试获取资源中，无法切换集数~").show()
            return
        }

        mAudioInfo?.nextUrl?.let { nextUrl ->
            loadData(nextUrl, onSuccess = {
                episodesUrl = nextUrl
                setTitleAndPlay(it, false)
            }, onError = {
                it.message?.let { msg -> Toasty.error(this@AudioPlayActivity, msg).show() }
            })
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setTitleAndPlay(it: AudioInfo, needSeekTo: Boolean, onSuccess: () -> Unit = {}) {
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
            position = it.currentPosstion.toInt()

            val serviceIntent = Intent(applicationContext, NotificationService::class.java)
            serviceIntent.action = START_SERVICE
            serviceIntent.putExtra("title", getTitleStr())
            startService(serviceIntent)
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
            setAudioHistory()
        }
    }

    private fun getTitleStr(): String = intent.getStringExtra("info") + " ===【第 $position 集】"

    //因为播放器不接受url中带有中文，因此需要做处理
    private fun handleUrl(url: String): String {
        return UrlEscapers.urlFragmentEscaper().escape(url)
    }


    fun onVideoPause() {
        videoPlayer.onVideoPause()
    }

    fun onVideoResume() {
        videoPlayer.onVideoResume()
    }

    private fun setAudioHistory(outState: Bundle? = null) {

        var history = ACache[this].getAsObject("history")

        if (history == null) {
            history = AudioHistorys()
        }

        history as AudioHistorys
        history.map.remove(intent.getStringExtra("bookUrl"))
        history.map[intent.getStringExtra("bookUrl")] =
            AudioHistory(
                getTitleStr(),
                videoPlayer.gsyVideoManager.currentPosition,
                intent.getStringExtra("bookUrl"),
                episodesUrl,
                position,
                TingShuUtil.sourceHost
            )

        ACache[this].put("history", history)
        outState?.let {
            it.putString("url", episodesUrl)
            it.putLong("currentPosition", videoPlayer.gsyVideoManager.currentPosition)
            it.putInt("position", position)
            it.putString("bookUrl", intent.getStringExtra("bookUrl"))
            it.putString("info", getTitleStr())
        }
    }

    @SuppressLint("CheckResult")
    private fun loadData(episodesUrl: String, onSuccess: (url: AudioInfo) -> Unit, onError: (e: Throwable) -> Unit) {
        Observable.create(ObservableOnSubscribe<AudioInfo> {
            try {
                if (!this.isFinishing && !this.isDestroyed) {
                    mAudioInfo = TingShuUtil.getAudioUrl(episodesUrl)
                    mAudioInfo?.let { info ->
                        it.onNext(info)
                    }
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
            .bindUntilEvent(this@AudioPlayActivity, Lifecycle.Event.ON_DESTROY)
            .subscribe({
                onSuccess(it)
            }, {
                onError(it)
            })
    }


    override fun onStop() {
        //记录当前播放进度
        setAudioHistory()
        // 发送 String 类型事件
        RxBus.getDefault().post(intent.getStringExtra("bookUrl"))
        super.onStop()
    }

    override fun onDestroy() {
        GSYVideoManager.releaseAllVideos()
        RxBus.getDefault().unregister(this)
        val serviceIntent = Intent(applicationContext, NotificationService::class.java)
        serviceIntent.action = NOTIFY_STOP
        startService(serviceIntent)
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {

        //记录当前播放进度
        setAudioHistory(outState)

        // 发送 String 类型事件
        RxBus.getDefault().post(intent.getStringExtra("bookUrl"))

        super.onSaveInstanceState(outState)
    }

}
