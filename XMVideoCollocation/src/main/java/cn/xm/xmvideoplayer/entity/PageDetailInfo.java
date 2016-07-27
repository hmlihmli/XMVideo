package cn.xm.xmvideoplayer.entity;

import org.jsoup.select.Elements;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by WANG on 2016/7/27.
 * 页面详情
 */
public class PageDetailInfo implements Serializable {
    //标题
    String title;
    //封面链接
    String cover;
    //剧情介绍
    String smalltext;
    String alltext;
    //主演,更新时间
    String actor;
    //下载地址
    List<List> downlist;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getSmalltext() {
        return smalltext;
    }

    public void setSmalltext(String smalltext) {
        this.smalltext = smalltext;
    }

    public String getAlltext() {
        return alltext;
    }

    public void setAlltext(String alltext) {
        this.alltext = alltext;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public List<List> getDownlist() {
        return downlist;
    }

    public void setDownlist(List<List> downlist) {
        this.downlist = downlist;
    }

    @Override
    public String toString() {
        return "PageDetailInfo{" +
                "title='" + title + '\'' +
                ", cover='" + cover + '\'' +
                ", smalltext='" + smalltext + '\'' +
                ", alltext='" + alltext + '\'' +
                ", actor='" + actor + '\'' +
                ", downlist=" + downlist +
                '}';
    }

    public PageDetailInfo(String title, String cover, String smalltext, String alltext, String actor, List<List> downlist) {
        this.title = title;
        this.cover = cover;
        this.smalltext = smalltext;
        this.alltext = alltext;
        this.actor = actor;
        this.downlist = downlist;
    }

    public PageDetailInfo() {
    }
}
