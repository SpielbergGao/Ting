package com.zjw.ting.net;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;

public class TingShuUtil {
    //有声听书吧
   public static String httpHost = "https://www.ysts8.com";
   public static String host = "www.ysts8.com";
   public static String searchUrl = "/Ys_so.asp?stype=1&keyword=";
   public static long countPage = -1;


    public static void main(String[] args) {
        try {
            String keyWord = "我当算命先生那几年";
            ArrayList<String> searchUrls = getSearchUrls(keyWord, countPage);
            ArrayList<String> episodesUrls = getEpisodesUrls(searchUrls.get(0));
            String audioUrl = getAudioUrl(episodesUrls.get(0));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static ArrayList<String> getSearchUrls(String keyWord) throws IOException {
        return  getSearchUrls(keyWord,-1);
    }
    /**
     * 获取搜索到的作品列表信息
     *
     * @param keyWord
     * @param page
     * @return
     * @throws IOException
     */
    public static ArrayList<String> getSearchUrls(String keyWord, long page) throws IOException {
        ArrayList<String> searchUrls = new ArrayList<>();
        ArrayList<String> searchInfos = new ArrayList<>();
        String keyParam = URLEncoder.encode(keyWord, "GB2312");
        String pageParam = page >= 0 ? "&page=" + page : "";
        final Connection connection = Jsoup.connect(httpHost + searchUrl + keyParam + pageParam);
        setCommonHeader(connection);
        connection.header("path", searchUrl + keyParam + pageParam);
        connection.header("referer", httpHost + "/");
        final Document doc = connection.get();

        //获取总页数
        Element countPageElement = doc.select("b:contains(共)").first();
        String countPageStr = countPageElement.childNode(0).toString().trim().split("/")[1].replace("页", "");
        countPage = Long.parseLong(countPageStr);
        //System.out.println("总页数 " + countPage);
        //获取当前页播放列表url集合
        //jsoup select用法参考https://www.cnblogs.com/yueshutong/p/9381530.html
        Element listDiv = doc.select("div.pingshu_ysts8").first();
        Elements urlListElements = listDiv.select("a[href]");
        for (Element urlListElement : urlListElements) {
            //名称 状态
            //urlListElement.text() 代表该节点的内容文本以及其嵌套的子节点的内容文本
            searchInfos.add(urlListElement.text().trim());
            searchUrls.add(httpHost + urlListElement.attr("href"));
        }
        //System.out.println(searchUrls);
        return searchUrls;
    }


    /**
     * 获取某一作品url的所有集数url
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static ArrayList<String> getEpisodesUrls(String url) throws IOException {
        ArrayList<String> urls = new ArrayList<>();
        ArrayList<String> episodes = new ArrayList<>();

        final Connection connection = Jsoup.connect(url);
        setCommonHeader(connection);
        // connection.header("path", searchUrl + keyParam + pageParam);
        //  connection.header("referer", httpHost + "/");
        final Document doc = connection.get();

        Element listDiv = doc.select("div.ny_l").first();
        Elements urlListElements = listDiv.select("a[href]");
        for (Element urlListElement : urlListElements) {
            //<a href="/play_16702_55_1_1.html" title="001.mp3">[第001集]</a>
            episodes.add(urlListElement.text().trim());
            urls.add(httpHost + urlListElement.attr("href"));
        }
        //System.out.println(urls);
        return urls;
    }

    /**
     * 获取某一作品的某一集对应的真实音频资源地址
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static String getAudioUrl(String url) throws IOException {
        final Connection connection = Jsoup.connect(url);
        setCommonHeader(connection);
        final Document doc = connection.get();

        Element frame = doc.select("iframe[src*=play]").first();
        String src = frame.attr("src");
        String relHref = httpHost + src;

        Connection connect = Jsoup.connect(relHref);
        setCommonHeader(connect);
        connect.header("path", src);
        connect.header("referer", url);
        Document iframeDoc = connect.get();
        Element scriptElement = iframeDoc.select("script:containsData(var )").first();
        String text = scriptElement.toString();

        StringBuffer audioStr = getAudioUrlFromText(text);
        return audioStr.toString();
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
                String[] strUrl = trim.replace("mp3:", "").split("\\+");
                String part1 = strUrl[0].replace("'", "").trim();
                //System.out.println("part1 ==== " + part1);
                audioStr.append(part1);
                for (int j = 0; j < list.size(); j++) {
                    if (list.get(j).trim().split(" = ")[0].equals(strUrl[1])) {
                        String part2 = list.get(j).trim().split(" = ")[1].replace("'", "").replace(";", "");
                        //特殊情况 url2598185 = ''+murl2598185+'126597953824x1558907797x126671352392-76152f3a765eb206245595d269214c51';
                        for (int k = 0; k < list.size(); k++) {
                            if (part2.contains(list.get(k).trim().split(" = ")[0])) {
                                part2 = part2.replace("+", "").replace(list.get(k).trim().split(" = ")[0], list.get(k).trim().split(" = ")[1].replace("'", "").replace(";", ""));
                            }
                        }
                        audioStr.append(part2);
                        //System.out.println("part2 ==== " + part2);
                    }
                }
                String part3 = strUrl[2].replace("'", "").trim();
                //System.out.println("part3 ==== " + part3);
                audioStr.append(part3);
            }
        }
        //System.out.println(text);
        //System.out.println("audioStr is " + audioStr);
        return audioStr;
    }

    private static void setCommonHeader(Connection connect) {
        connect.header("authority", host);
        connect.header("scheme", "https");
        connect.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36");
        connect.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        connect.header("accept-encoding", "gzip, deflate, br");
        connect.header("accept-language", "zh-CN,zh;q=0.9,en;q=0.8");
        connect.header("upgrade-insecure-requests", "1");
    }
}
