package com.zjw.ting.net;

import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class TingShuUtil {
    //有声听书吧
    public static String httpHost = "https://m.ysts8.com";
    public static String host = "m.ysts8.com";
    public static String searchUrl = "/so.asp?keyword=";
    public static long countPage = -1;

    /**
     * 获取搜索到的作品列表信息
     *
     * @param keyWord
     * @param page
     * @return
     * @throws IOException
     */
    public static ArrayList<AudioInfo> getSearchUrls(String keyWord, long page) {
        ArrayList<AudioInfo> audioInfos = new ArrayList<>();
        String keyParam = null;
        try {
            keyParam = URLEncoder.encode(keyWord, "GB2312");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String pageParam = page > 0 ? "&page=" + page : "";
        try {
            final Connection connection = Jsoup.connect(httpHost + searchUrl + keyParam + pageParam);
            setCommonHeader(connection);
            connection.header("path", searchUrl + keyParam + pageParam);
            connection.header("referer", httpHost + "/");
            final Document doc = connection.get();

            //获取总页数
            Element countPageElement = doc.select("a:contains(页次)").first();
            String countPageStr = countPageElement.childNode(0).toString().trim().split("/")[1];
            countPage = Long.parseLong(countPageStr);
            //System.out.println("总页数 " + countPage);
            //获取当前页播放列表url集合
            //jsoup select用法参考https://www.cnblogs.com/yueshutong/p/9381530.html
            Element listDiv = doc.select("div.top_list").first();
            Elements urlListElements = listDiv.select("a[href]");
            for (Element urlListElement : urlListElements) {
                //名称 状态
                //urlListElement.text() 代表该节点的内容文本以及其嵌套的子节点的内容文本
                audioInfos.add(new AudioInfo(urlListElement.childNodes().get(1).outerHtml() + " " + urlListElement.childNodes().get(2).childNode(0).outerHtml(), httpHost + urlListElement.attr("href")));
            }
        } catch (Throwable throwable) {
            audioInfos = null;
            throwable.printStackTrace();
        }
        //System.out.println(audioInfos);
        return audioInfos;
    }


    /**
     * 获取某一作品url的所有集数url
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static ArrayList<AudioInfo> getEpisodesUrls(String url) {
        ArrayList<AudioInfo> audioInfos = new ArrayList<>();
        try {
            final Connection connection = Jsoup.connect(url);
            setCommonHeader(connection);
            // connection.header("path", searchUrl + keyParam + pageParam);
            //  connection.header("referer", httpHost + "/");
            final Document doc = connection.get();

            Element listDiv = doc.select("div.compress").first();
            Elements urlListElements = listDiv.select("a[href]");
            for (Element urlListElement : urlListElements) {
                //<a href="/play_16702_55_1_1.html" title="001.mp3">[第001集]</a>
                if (urlListElement.outerHtml().contains("第")) {
                    audioInfos.add(new AudioInfo(urlListElement.text().trim(), httpHost + urlListElement.attr("href")));
                }
            }
        } catch (Throwable throwable) {
            audioInfos = null;
            throwable.printStackTrace();
        }
        //System.out.println(audioInfos);
        return audioInfos;
    }

    /**
     * 获取某一作品的某一集对应的真实音频资源地址
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static AudioInfo getAudioUrl(String url) {
        AudioInfo audioInfo = new AudioInfo(url);
        try {
            final Connection connection = Jsoup.connect(url);
            setCommonHeader(connection);
            final Document doc = connection.get();

            //获取上下集的html
            Element preUrlElment = doc.select("a:contains(上一集)").first();
            audioInfo.setPreUrl(httpHost + "/play_m/" + preUrlElment.attr("href"));
            Element nextUrlElment = doc.select("a:contains(下一集)").first();
            audioInfo.setNextUrl(httpHost + "/play_m/" + nextUrlElment.attr("href"));

            //获取当前集数
            Element currentUrlElment = doc.select("h3.sub_tit").first();
            final Node node = currentUrlElment.childNodes().get(1);
            audioInfo.setCurrentPosstion(((TextNode) node).toString().split("回")[0].replace("第", ""));

            Element frame = doc.select("iframe[src*=play]").first();
            String src = frame.attr("src");
            String relHref = httpHost + src;
            Log.e("tag", "relHref " + relHref);

            Connection connect = Jsoup.connect(relHref);
            setCommonHeader(connect);
            connect.header("path", src);
            connect.header("referer", url);
            Document iframeDoc = connect.get();
            Element scriptElement = iframeDoc.select("script:containsData(var )").first();
            String text = scriptElement.toString();

            StringBuffer audioStr = getAudioUrlFromText(text);
            audioInfo.setUrl(audioStr.toString());
        } catch (Throwable throwable) {
            audioInfo = null;
            throwable.printStackTrace();
        }
        return audioInfo;
    }

    private static StringBuffer getAudioUrlFromText(String text) {
        String[] split = text.split("\n");
        ArrayList<String> list = new ArrayList<String>();
        StringBuffer audioStr = new StringBuffer();
        for (int i = 0; i < split.length; i++) {
            String trim = split[i].trim();
            if (trim.contains(" = '")) {
                list.add(trim);
            }
            if (trim.contains("mp3:")) {
                //mp3:'http://psf.tt56w.com:8000/刘兰芳/努尔哈赤/刘兰芳_努尔哈赤_05.mp3?126598878994x1559832967x126605009654-c4c16a404860c1a040cc0b8b76376'
                String strUrl = trim.replace("mp3:", "").replace("'", "");
                audioStr.append(strUrl);
            }
        }
        //System.out.println(text);
        //System.out.println("audioStr is " + audioStr);
        return audioStr;
    }

    private static void setCommonHeader(Connection connect) {
        /*Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        final String format = dateFormat.format(date.getTime());
        connect.header("if-modified-since", format);*/
        connect.header("cache-control", "max-age=0");
        connect.header("authority", host);
        connect.header("scheme", "https");
        //connect.header("cookie", "ASPSESSIONIDAWCQBSBB=PMAAPIHCAHGKNCGENGPIEPAK; startime=1");
        //connect.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36");
        connect.header("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Mobile Safari/537.36");
        connect.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        connect.header("accept-encoding", "gzip, deflate, br");
        connect.header("accept-language", "zh-CN,zh;q=0.9,en;q=0.8");
        connect.header("upgrade-insecure-requests", "1");
        //connect.header("Referrer Policy", "no-referrer-when-downgrade");
    }

    public static class AudioInfo implements Serializable {
        private String info;
        private String url;
        private String episodesUrl;
        private String preUrl;
        private String nextUrl;
        //当前播放的集数
        private String currentPosstion;

        public AudioInfo(String episodesUrl) {
            this.episodesUrl = episodesUrl;
        }

        public AudioInfo(String info, String url) {
            this.info = info;
            this.url = url;
        }

        public AudioInfo(String info, String url, String preUrl, String nextUrl) {
            this.info = info;
            this.url = url;
            this.preUrl = preUrl;
            this.nextUrl = nextUrl;
        }

        public String getPreUrl() {
            return preUrl;
        }

        public void setPreUrl(String preUrl) {
            this.preUrl = preUrl;
        }

        public String getNextUrl() {
            return nextUrl;
        }

        public void setNextUrl(String nextUrl) {
            this.nextUrl = nextUrl;
        }

        public String getInfo() {
            return info;
        }

        public void setInfo(String info) {
            this.info = info;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getEpisodesUrl() {
            return episodesUrl;
        }

        public void setEpisodesUrl(String episodesUrl) {
            this.episodesUrl = episodesUrl;
        }

        public String getCurrentPosstion() {
            return currentPosstion;
        }

        public void setCurrentPosstion(String currentPosstion) {
            this.currentPosstion = currentPosstion;
        }

        @Override
        public String toString() {
            return "AudioInfo{" +
                    "info='" + info + '\'' +
                    ", url='" + url + '\'' +
                    ", episodesUrl='" + episodesUrl + '\'' +
                    ", preUrl='" + preUrl + '\'' +
                    ", nextUrl='" + nextUrl + '\'' +
                    ", currentPosstion='" + currentPosstion + '\'' +
                    '}';
        }
    }
}
