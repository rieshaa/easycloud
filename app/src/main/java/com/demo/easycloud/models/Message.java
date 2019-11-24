package com.demo.easycloud.models;

import androidx.annotation.NonNull;

public class Message {
    String message;
    String name;
    String key;
    String imageUrl;

    public Message() {}

    public Message(String message, String name) {
        this.message = message;
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public String toString() {
        return "User: ("+
                "Message: "+this.message+" "+
                "Name: "+this.name+" "+
                "Key: "+this.key+" "+
                "Image Url: "+this.imageUrl+")";
    }
}
