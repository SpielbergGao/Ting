package com.zjw.ting.bean;

import java.io.Serializable;

public class AudioInfo implements Serializable {
    private String info;
    private String url;
    private String wrapUrl;
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

    public String getWrapUrl() {
        return wrapUrl;
    }

    public void setWrapUrl(String wrapUrl) {
        this.wrapUrl = wrapUrl;
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