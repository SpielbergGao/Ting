package com.zjw.ting.bean;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class AudioHistorys implements Serializable {

    private LinkedHashMap<String, AudioHistory> map = new LinkedHashMap<>();

    public LinkedHashMap<String, AudioHistory> getMap() {
        return map;
    }

    public void setMap(LinkedHashMap<String, AudioHistory> map) {
        this.map = map;
    }

}
