package com.github.yajuhua.extractor.yt.tab;

import com.github.yajuhua.extractor.yt.Page;
import com.github.yajuhua.extractor.yt.Video;
import com.github.yajuhua.extractor.yt.pojo.VideoInfo;
import com.github.yajuhua.extractor.yt.utils.Http;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 一般有videos和streams
 */
public class Tabs {

    /**
     * 获取tabs
     * @param url
     * @return
     */
    public static JsonArray getTabs(String url) throws Exception {
        String htmlStr = Http.getHtml(url);
        JsonObject pageInitialData = Page.getPageInitialData(htmlStr);
        return pageInitialData.getAsJsonObject("contents")
                .getAsJsonObject("twoColumnBrowseResultsRenderer")
                .getAsJsonArray("tabs");
    }

    /**
     * 获取tab的title
     * @param tab
     * @return
     */
    public static String getTabName(JsonObject tab) throws Exception {
        if (getTabRenderer(tab) != null && getTabRenderer(tab).has("title")){
            return getTabRenderer(tab).get("title").getAsString();
        }else {
            return null;
        }
    }

    /**
     * 获取tabs中的所有title
     * @param tabs
     * @return
     */
    public static List<String> getTabNames(JsonArray tabs) throws Exception {
        List<String> tabNames = new ArrayList<>();
        for (JsonElement tab : tabs) {
            String tabName = getTabName(tab.getAsJsonObject());
            if (tabName != null){
                tabNames.add(tabName);
            }
        }
        return tabNames;
    }

    /**
     * 获取tabRenderer
     * @param tab
     * @return
     */
    public static JsonObject getTabRenderer(JsonObject tab) throws Exception {
        if (tab.has("tabRenderer")){
            JsonObject tabRenderer = tab.get("tabRenderer").getAsJsonObject();
            return tabRenderer;
        }else {
            return null;
        }
    }

    /**
     * 获取richGridRenderer
     * @param tabRenderer
     * @return
     */
    public static JsonObject getRichGridRenderer(JsonObject tabRenderer){
        if (tabRenderer.has("content")){
            JsonObject richGridRenderer = tabRenderer.getAsJsonObject("content").
                    getAsJsonObject("richGridRenderer");
            return richGridRenderer;
        }else {
            return null;
        }
    }

    /**
     * 获取视频列表
     * @param richGridRenderer
     * @return
     */
    public static List<VideoInfo> getVideoList(JsonObject richGridRenderer){
        List<VideoInfo> videoInfoList = new ArrayList<>();
        JsonArray contents = richGridRenderer.getAsJsonArray("contents");
        for (JsonElement content : contents) {
            if (content.getAsJsonObject().has("richItemRenderer")){
                JsonObject videoRenderer = content.getAsJsonObject().getAsJsonObject("richItemRenderer")
                        .getAsJsonObject("content").
                        getAsJsonObject("videoRenderer");
                VideoInfo videoInfo = Video.getVideoInfo(videoRenderer);
                if (videoInfo != null){
                    videoInfoList.add(videoInfo);
                }
            }
        }
        return videoInfoList;
    }

    /**
     * 获取视频列表
     * @param url
     * @return
     * @throws Exception
     */
    public static List<VideoInfo> getVideoList(String url) throws Exception {
        List<VideoInfo> videoInfoList = new ArrayList<>();
        url = getChannelContentTypeToUrl(url);
        JsonArray tabs = Tabs.getTabs(url);
        for (JsonElement tab : tabs) {
            String tabName = Tabs.getTabName(tab.getAsJsonObject());
            String channelContentType = getChannelContentType(url);
            if (channelContentType.equalsIgnoreCase("streams")){
                channelContentType = "live";
            }
            if (tabName != null && tabName.equalsIgnoreCase(channelContentType)){
                JsonObject tabRenderer = tab.getAsJsonObject()
                        .getAsJsonObject("tabRenderer");
                JsonObject richGridRenderer = Tabs.getRichGridRenderer(tabRenderer);
                if (richGridRenderer == null){
                    continue;
                }
                videoInfoList = Tabs.getVideoList(richGridRenderer);
                return videoInfoList;
            }
        }
        return videoInfoList;
    }

    /**
     * 获取频道类型: videos / streams
     * @param url
     * @return
     */
    public static String getChannelContentType(String url){
        if (url.contains("/")){
            try {
                String substring = url.substring(url.lastIndexOf("/") + 1);
                if (substring.equalsIgnoreCase("streams") || substring.equalsIgnoreCase("videos")){
                    return substring;
                }else {
                    //默认
                    return "videos";
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }else {
            throw new RuntimeException("url不合法: " + url);
        }
    }

    /**
     * 获取频道类型对应的url
     * @param url
     * @return
     */
    public static String getChannelContentTypeToUrl(String url){
        if (url.contains("/")){
            try {
                String substring = url.substring(url.lastIndexOf("/") + 1);
                if (substring.equalsIgnoreCase("streams") || substring.equalsIgnoreCase("videos")){
                    return url;
                }else {
                    //默认
                    return url + "/videos";
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }else {
            throw new RuntimeException("不合法链接: " + url);
        }
    }
}
