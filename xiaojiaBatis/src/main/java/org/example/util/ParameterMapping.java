package org.example.util;

public class ParameterMapping {
    //#{}里面解析的值
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ParameterMapping(String content) {
        this.content = content;
    }
}
