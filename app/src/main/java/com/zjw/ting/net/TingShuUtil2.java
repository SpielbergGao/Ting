package com.zjw.ting.net;

import com.google.common.base.Splitter;
import com.zjw.ting.bean.AudioInfo;
import com.zjw.ting.util.JSEngine;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;

public class TingShuUtil2 {
    //https://m.mp3book.cn/
    public static String httpHost = "https://m.mp3book.cn/";
    private static String host = "https://m.mp3book.cn";
    private static String searchUrl = "https://m.mp3book.cn/search.php?keywords=";
    private static long countPage = -1;

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
        try {
            String keyParam = URLEncoder.encode(keyWord, "utf-8");
            String pageParam = page > 0 ? "&page=" + page : "1";
            final Connection connection = Jsoup.connect(searchUrl + keyParam + "&category=0&sort=add_time&order=DESC&intro=&detail=0&area=&lang=&page=" + pageParam);
            setCommonHeader(connection);
            ///search.php?keywords=%E6%88%91%E5%BD%93
            connection.header("path", "/search.php?keywords=" + keyParam + "&category=0&sort=add_time&order=DESC&intro=&detail=0&area=&lang=&page=" + pageParam);
            connection.header("referer", httpHost);
            final Document doc = connection.get();
            Element booksDiv = doc.select("div.book_slist").first();
            Elements bookInfosDiv = booksDiv.select("div.bookbox");
            for (int i = 0; i < bookInfosDiv.size(); i++) {
                Element element = bookInfosDiv.get(i);
                String bookUrl = httpHost + "show-" + element.attr("bookid") + ".html";
                //ownText 直属标签外层外层text  --- text 直属标签外层text+内嵌子标签的text
                String info = element.select("h4.bookname").first().ownText() + " / " + element.select("div.author").first().ownText() + " " + element.select("div.update").first().text();
                audioInfos.add(new AudioInfo(info, bookUrl));
            }
            //System.out.println(doc);
        } catch (Throwable throwable) {
            audioInfos = null;
            throwable.printStackTrace();
        }
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
        //url = https://m.mp3book.cn/show-21611.html
        final String bookId = url.split("show-")[1].replace(".html", "");
        //https://m.mp3book.cn/player.php?mov_id=1022&look_id=1&player=mp
        //https://m.mp3book.cn/player.php?mov_id=书籍ID&look_id=集数&player=mp
        url = String.format("https://m.mp3book.cn/player.php?mov_id=%s&look_id=1&player=mp", bookId);

        ArrayList<AudioInfo> audioInfos = new ArrayList<>();
        try {

            final Connection connection = Jsoup.connect(url);
            setCommonHeader(connection);
            final Document doc = connection.get();

            Element listDiv = doc.select("div.playlist").first();
            Elements urlListElements = listDiv.select("a[href]");
            for (Element urlListElement : urlListElements) {
                String info = String.format("[第%s集]", urlListElement.ownText());
                audioInfos.add(new AudioInfo(info, httpHost + urlListElement.attr("href").replace("&amp;", "&")));
            }
            countPage = audioInfos.size();
        } catch (Throwable throwable) {
            audioInfos = null;
            throwable.printStackTrace();
        }
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
            connection.header("path", url.replace(host, ""));

            final String format = "https://m.mp3book.cn/player.php?mov_id=%s&look_id=1&player=mp";
            final String mov_id = getParam(url, "mov_id");
            connection.header("referer", String.format(format, mov_id));
            final Document doc = connection.get();

            //获取上下集的html
            Element preUrlElment = doc.select("a:contains(上一章)").first();
            String preUrl = preUrlElment.attr("href").trim();
            if (preUrl.length() > 0) {
                audioInfo.setPreUrl(preUrl.replace("&amp;", "&"));
            }

            Element nextUrlElment = doc.select("a:contains(下一章)").first();
            String nextUrl = nextUrlElment.attr("href").trim();
            if (nextUrl.length() > 0) {
                audioInfo.setNextUrl(nextUrl.replace("&amp;", "&"));
            }

            //获取当前集数
            final String look_id = getParam(url, "look_id");
            audioInfo.setCurrentPosstion(look_id);

            //获取当前集数
            String currentUrl = "";
            String wrapUrl = "";
            //var media =  { mp3: "http://mp3.aikeu.com/1022/1.mp3"}
            //script 必须用containsData  ontains无效
            Element scrpitElement = doc.select("script:containsData(var media =  { mp3)").first();
            String[] split = scrpitElement.toString().split("\n");
            for (int i = 0; i < split.length; i++) {
                String trim = split[i].trim();
                if (trim.contains("var u=")) {
                    final String srcUrl = new JSEngine().runScript(GET_URL, "getUrl", new String[]{trim.split("\"")[1]});
                    currentUrl = srcUrl;
                }
                if (trim.contains("var media =  { mp3: ")) {
                    wrapUrl = trim.split("\"")[1];
                }
            }
            audioInfo.setUrl(currentUrl);
            audioInfo.setWrapUrl(wrapUrl);
            // System.out.println(currentUrl);
        } catch (Throwable throwable) {
            audioInfo = null;
            throwable.printStackTrace();
        }
        return audioInfo;
    }

    public static String getParam(String url, String name) {
        String params = url.substring(url.indexOf("?") + 1);
        Map<String, String> split = Splitter.on("&").withKeyValueSeparator("=").split(params);
        return split.get(name);
    }

    private static final String GET_URL = "function getUrl(p){\n" +
            "\tvar u=p;\n" +
            "\tvar uArr=u.split(\"*\");\n" +
            "\tvar n = uArr.length;\n" +
            "\tvar x = '';\n" +
            "\tfor(i=1;i<n-1;i++){\n" +
            "   \t\tx += String.fromCharCode(uArr[i]);\n" +
            "\t}\n" +
            "\treturn x\n" +
            "}";

    private static void setCommonHeader(Connection connect) {
        connect.header("authority", "m.mp3book.cn");
        connect.header("scheme", "https");
        connect.header("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Mobile Safari/537.36");
        connect.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        connect.header("accept-encoding", "gzip, deflate, br");
        connect.header("accept-language", "zh-CN,zh;q=0.9,en;q=0.8");
        connect.header("upgrade-insecure-requests", "1");
    }

}
