package com.example.nguyen.hybrid_aes_des.model;

public class Keys {
    private String key, child;

    public Keys(){}

    public Keys(String key, String child) {
        this.key = key;
        this.child = child;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getChild() {
        return child;
    }

    public void setChild(String child) {
        this.child = child;
    }
}
