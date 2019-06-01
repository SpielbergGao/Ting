package com.zjw.ting.bean;

public class Event {
    public static class ServiceEvent {
        private String action = "";

        public ServiceEvent(String action) {
            this.action = action;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }
    }
}
