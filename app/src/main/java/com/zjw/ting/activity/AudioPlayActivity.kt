package com.zjw.ting.activity

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.lzx.starrysky.model.SongInfo
import com.zjw.ting.R
import com.zjw.ting.net.TingShuUtil
import es.dmoral.toasty.Toasty
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_audio_play.*

class AudioPlayActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_play)

        val s1 = SongInfo()
        s1.songId = "111"
        s1.songUrl = ""

        loadData(onSuccess = {
            Log.e("tag", "" + it)
            Toasty.success(this@AudioPlayActivity, "get url $it").show()
            s1.songUrl = it
        }, onError = {
            it.message?.let { msg -> Toasty.error(this@AudioPlayActivity, msg).show() }
        })

        //s1.songCover = "https://www.qqkw.com/d/file/p/2018/04-21/c24fd86006670f964e63cb8f9c129fc6.jpg"
        //s1.songName = "心雨"
        //s1.artist = "贤哥"

        val songInfos = ArrayList<SongInfo>()
        songInfos.add(s1)

        playTv.setOnClickListener { v ->
            // MusicManager.getInstance().playMusic(songInfos, 0)
            // playAudio("http://180l.ysts8.com:8000/恐怖小说/我当算命先生那些年/014.mp3?1231710044742x1558968690x1231716175402-f002e814b9d51c55addf150d702074fc?3")
            playAudio("http://fs.w.kugou.com/201905272309/3c16fd91c7c8f3953c42ad9832de7ba7/G018/M06/04/10/Ug0DAFVe7COANQyAAD8RTmmcMlM249.mp3")
        }
    }

    @Throws(Exception::class)
    private fun playAudio(url: String) {
        killMediaPlayer()

        mediaPlayer = MediaPlayer()
        //mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mediaPlayer?.setDataSource(url)
        mediaPlayer?.prepareAsync()
        mediaPlayer?.start()
    }

    private fun killMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer?.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        killMediaPlayer()
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
