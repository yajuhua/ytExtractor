package com.github.yajuhua.extractor.yt;

import com.github.yajuhua.extractor.yt.pojo.PageInfo;
import com.github.yajuhua.extractor.yt.pojo.VideoInfo;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 解析 playlist/channel页面
 */
public class Page {

    /**
     * playlist和channel
     */
    public enum UrlType{
        Playlist,
        Channel
    }

    /**
     * 支持playlist和频道主页
     * @param url
     * @return
     */
    public static UrlType getTypeByUrl(String url){
        if (url.contains("https://www.youtube.com/playlist?list=")){
            return UrlType.Playlist;
        }
        return UrlType.Channel;
    }

    /**
     * 获取playlist/channel页面信息
     * @param url
     * @return
     */
    public static PageInfo getInfo(String url) throws Exception {
        UrlType typeByUrl = getTypeByUrl(url);
        switch (typeByUrl) {
            case Playlist:
                return Playlist.getInfo(url);
            case Channel:
                return Channel.getInfo(url);
        }
        throw new Exception("PageInfo Not Found from " + url);
    }

    /**
     * 获取初始化视频列表
     * @param url
     * @return
     */
    public static List<VideoInfo> getVideoList(String url,Video.Sorted sorted) throws Exception {
        UrlType typeByUrl = getTypeByUrl(url);
        switch (typeByUrl) {
            case Channel:
                return Channel.getInit(url,sorted);
            case Playlist:
                List<VideoInfo> init = Playlist.getInit(url, Video.Sorted.Default);
                if (init != null && init.size() > 2){
                    String url1 = init.get(0).getUrl();
                    String url2 = init.get(init.size() - 1).getUrl();
                    Integer comparePublicTime = Video.comparePublicTime(url1, url2);
                    if (comparePublicTime != 1){
                        //第一个视频是最旧的
                        //获取全部视频
                        List<VideoInfo> moreVideoInfo =
                                Playlist.getMore(url, Video.Sorted.Default);
                        init.addAll(moreVideoInfo);
                        return Video.sorted(init, Video.Sorted.DESCByTime);
                    }else {
                        //第一个视频是最新的
                        return Playlist.getInit(url, Video.Sorted.Default);
                    }
                }else {
                    return Playlist.getInit(url,sorted);
                }
        }
        return new ArrayList<>();
    }

    /**
     * 获取视频列表
     * @param url
     * @param es 第一个数等于0返回最新一期视频；第一个数等于-1返回全部视频；第一个数大于0返回对应index视频
     * @return
     */
    public static List<VideoInfo> getVideoList(String url, List<Integer> es) throws Exception {
        if (es == null){
            es = Arrays.asList(0);
        }
        Integer sign = es.get(0);
        if (sign > 0){
           //自定义
            List<VideoInfo> videoList = getVideoList(url, Video.Sorted.Default);
            List<VideoInfo> customs = new ArrayList<>();
            for (int i = 0; i < es.size(); i++) {
                if (videoList.size() > es.get(i)){
                    customs.add(videoList.get(es.get(i) - 1));
                }
            }
            return customs;
        }else if (sign == 0){
            return Arrays.asList(getLatest(url));
        }else {
            //返回全部
            return getVideoList(url, Video.Sorted.Default);
        }
    }

    /**
     * 返回最近视频
     * @param url
     * @param count 几个
     * @return
     */
    public static List<VideoInfo> getRecent(String url,Integer count) throws Exception {
        List<VideoInfo> videoList = getVideoList(url, Video.Sorted.ACSByTime);
        Collections.reverse(videoList);
        if (count > videoList.size()){
            return videoList;
        }else {
            return videoList.subList(videoList.size() - count,videoList.size());
        }
    }

    /**
     * 返回最新一期视频
     * @param url
     * @return
     */
    public static VideoInfo getLatest(String url) throws Exception {
        List<VideoInfo> videoList = getVideoList(url, Video.Sorted.DESCByTime);
        if (videoList != null && !videoList.isEmpty()){
            return videoList.get(0);
        }
        throw new Exception("找不到最新视频");
    }

    /**
     * 获取https://www.youtube.com/playlist?list=XXX  or https://www.youtube.com/channel/xxx 页面json数据
     * @param htmlStr 页面字符串
     * @return
     */
    public static JsonObject getPageInitialData(String htmlStr) throws Exception {
        Document document = getPageDoc(htmlStr);
        Elements scripts = document.getElementsByTag("script");
        for (Element script : scripts) {
            if (script.data().startsWith("var ytInitialData")) {
                String data = script.data();
                String initialData = data.substring(data.indexOf('{'), data.length() - 1);
                return new Gson().fromJson(initialData, JsonObject.class);
            }
        }
        throw new Exception("ytInitialData Not Found");
    }

    /**
     * 获取https://www.youtube.com/playlist?list=XXX  or https://www.youtube.com/channel/xxx 页面Document
     * @param htmlStr 页面字符串
     * @return
     */
    public static Document getPageDoc(String htmlStr){
        return Jsoup.parse(htmlStr);
    }
}
