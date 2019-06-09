package com.zjw.ting.bean;

import java.io.Serializable;
import java.util.Objects;

public class AudioHistory implements Serializable {
    private String info;
    private String episodesUrl;
    private String bookUrl;
    private long currentPosition;
    private int position;
    private String sourceHost;

    public AudioHistory(String info, long currentPosition, String bookUrl, String episodesUrl, int position, String sourceHost) {
        this.info = info;
        this.episodesUrl = episodesUrl;
        this.bookUrl = bookUrl;
        this.currentPosition = currentPosition;
        this.position = position;
        this.sourceHost = sourceHost;
    }


    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public long getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(long currentPosition) {
        this.currentPosition = currentPosition;
    }

    public String getEpisodesUrl() {
        return episodesUrl;
    }

    public void setEpisodesUrl(String episodesUrl) {
        this.episodesUrl = episodesUrl;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getBookUrl() {
        return bookUrl;
    }

    public void setBookUrl(String bookUrl) {
        this.bookUrl = bookUrl;
    }

    public String getSourceHost() {
        return sourceHost;
    }

    public void setSourceHost(String sourceHost) {
        this.sourceHost = sourceHost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AudioHistory)) return false;
        AudioHistory that = (AudioHistory) o;
        return Objects.equals(getBookUrl(), that.getBookUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getInfo(), getEpisodesUrl(), getBookUrl(), getCurrentPosition(), getPosition(), getSourceHost());
    }
}