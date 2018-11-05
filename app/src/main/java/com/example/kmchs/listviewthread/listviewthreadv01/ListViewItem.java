package com.example.kmchs.listviewthread.listviewthreadv01;

public class ListViewItem {
    private String icon;
    private String text1;
    private String text2;
    private String url;

    public void setIcon(String icon) {
        this.icon=icon;
    }
    public void setText1(String text) {
        text1=text;
    }
    public void setText2(String text) {
        text2=text;
    }
    public void setUrl(String url){this.url=url;}
    public String getIcon() {
        return this.icon;
    }
    public String getText1() {
        return this.text1;
    }
    public String getText2() {
        return this.text2;
    }
    public String getUrl(){return this.url;}

}
