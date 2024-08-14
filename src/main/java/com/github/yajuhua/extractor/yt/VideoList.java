package com.github.yajuhua.extractor.yt;

import com.github.yajuhua.extractor.yt.pojo.VideoInfo;
import com.github.yajuhua.extractor.yt.utils.Http;
import com.github.yajuhua.extractor.yt.utils.ParseTime;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * videos / streams / playlist 视频列表
 */
public class VideoList {

    /**
     * 获取视频列表
     * @param url
     * @return
     */
    public static List<VideoInfo> get(String url) throws Exception {
        switch (getTypeByUrl(url)) {
            case Channel:
                return getVideosOrStreamsList(url);
            case Playlist:
                return getPlaylist(url);
            default:
                throw new Exception("链接不合法: " + url);
        }
    }

    /**
     * playlist和channel
     */
    enum UrlType{
        Playlist,
        Channel
    }

    /**
     * 支持playlist和频道主页
     * @param url
     * @return
     */
    private static UrlType getTypeByUrl(String url){
        if (url.contains("https://www.youtube.com/playlist?list=")){
            return UrlType.Playlist;
        }
        return UrlType.Channel;
    }

    /**
     * 获取频道类型: videos / streams
     * @param url
     * @return
     */
    private static String getChannelContentType(String url){
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
            throw new RuntimeException("不合法链接: " + url);
        }
    }

    /**
     * 获取频道类型对应的url
     * @param url
     * @return
     */
    private static String getChannelContentTypeToUrl(String url){
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

    /**
     * 获取tab中视频的videoRenderer对象集合
     * @param tabRenderer
     * @return
     */
    private static List<JsonObject> getVideoRenderer(JsonObject tabRenderer){
        List<JsonObject> videoIdList = new ArrayList<>();
        if (!tabRenderer.has("content")){
            return videoIdList;
        }
        JsonArray asJsonArray = tabRenderer.get("content").getAsJsonObject().get("richGridRenderer").getAsJsonObject()
                .get("contents").getAsJsonArray();
        for (JsonElement element : asJsonArray) {
            if (!element.getAsJsonObject().has("richItemRenderer")){
                continue;
            }
            JsonObject videoRendererJsonObject = element.getAsJsonObject().get("richItemRenderer").getAsJsonObject().get("content").getAsJsonObject()
                    .get("videoRenderer").getAsJsonObject();
            videoIdList.add(videoRendererJsonObject);
        }
        return videoIdList;
    }

    /**
     * 获取Videos/streams集合
     *
     * @return
     */
    public static List<VideoInfo> getVideosOrStreamsList(String url) throws Exception {
        //获取链接与类型
        String type = getChannelContentType(url);
        if (type.equalsIgnoreCase("streams")){
            type = "live";
        }
        type = type.substring(0,1).toUpperCase() + type.substring(1);
        url = getChannelContentTypeToUrl(url);

        //开始提取
        String htmlStr = Http.get(url, null, null);
        Map<String, JsonObject> tabRendererMap = ChannelInfo.getTabRenderer(htmlStr);
        List<VideoInfo> videoInfoList = new ArrayList<>();
        if (tabRendererMap.containsKey(type)){
            List<JsonObject> videoRendererList = getVideoRenderer(tabRendererMap.get(type));
            for (JsonObject object : videoRendererList) {
                VideoInfo videoInfo = getVideoInfo(object);
                videoInfoList.add(videoInfo);
            }
            return videoInfoList;
        }else {
            throw new RuntimeException("该频道没有" + type);
        }
    }

    /**
     * 获取plaulist视频集合信息
     * @param url
     * @return
     * @throws Exception
     */
    public static List<VideoInfo> getPlaylist(String url) throws Exception{
        String htmlStr = Http.get(url, null, null);
        List<VideoInfo> videoInfoList = new ArrayList<>();
        List<JsonObject> playlistVideoRenderer = getPlaylistVideoRenderer(htmlStr);
        for (JsonObject object : playlistVideoRenderer) {
            VideoInfo videoInfo = getVideoInfo(object);
            videoInfoList.add(videoInfo);
        }
        return videoInfoList;
    }

    /**
     * 获取playlistVideoRenderer集合
     * @return
     * @throws Exception
     */
    private static List<JsonObject> getPlaylistVideoRenderer(String htmlStr)throws Exception{
        List<JsonObject> playlistVideoRendererList = new ArrayList<>();
        Document document = Jsoup.parse(htmlStr);
        Elements scripts = document.getElementsByTag("script");
        Gson gson = new Gson();
        for (Element script : scripts) {
            if (script.data().startsWith("var ytInitialData")){
                String data = script.data();
                String json = data.substring(data.indexOf('{'),data.length() -1);
                JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
                JsonArray tabs = jsonObject.get("contents").getAsJsonObject().get("twoColumnBrowseResultsRenderer")
                        .getAsJsonObject().get("tabs").getAsJsonArray();

                for (JsonElement tab : tabs) {
                    JsonArray contents = tab.getAsJsonObject().get("tabRenderer").getAsJsonObject().get("content").getAsJsonObject().get("sectionListRenderer")
                            .getAsJsonObject().get("contents").getAsJsonArray();

                    for (JsonElement content : contents) {
                        if (!content.getAsJsonObject().has("itemSectionRenderer")){
                            continue;
                        }
                        JsonArray contents2 = content.getAsJsonObject().get("itemSectionRenderer").getAsJsonObject()
                                .get("contents").getAsJsonArray();

                        for (JsonElement element : contents2) {
                            if (!element.getAsJsonObject().has("playlistVideoListRenderer")){
                                continue;
                            }
                            JsonArray contents3 = element.getAsJsonObject().get("playlistVideoListRenderer")
                                    .getAsJsonObject().get("contents").getAsJsonArray();

                            for (JsonElement c3 : contents3) {
                                if (!c3.getAsJsonObject().has("playlistVideoRenderer")){
                                    continue;
                                }
                                JsonObject playlistVideoRenderer = c3.getAsJsonObject().get("playlistVideoRenderer")
                                        .getAsJsonObject();
                                playlistVideoRendererList.add(playlistVideoRenderer);
                            }
                        }
                    }
                }
            }
        }
        return playlistVideoRendererList;
    }

    /**
     * 解析单个videoRenderer
     * @param videoRenderer
     * @return
     */
    private static VideoInfo getVideoInfo(JsonObject videoRenderer){
        String videoId = videoRenderer.get("videoId").getAsString();

        JsonArray thumbnails = videoRenderer.get("thumbnail").getAsJsonObject().get("thumbnails").getAsJsonArray();
        String image = null;
        if (!thumbnails.isEmpty()){
            image = thumbnails.get(thumbnails.size() - 1).getAsJsonObject().get("url").getAsString();
        }

        String title = null;
        JsonArray titles = videoRenderer.get("title").getAsJsonObject().get("runs").getAsJsonArray();
        if (!titles.isEmpty()){
            title = titles.get(titles.size() - 1).getAsJsonObject().get("text").getAsString();
        }

        String url = "https://www.youtube.com/watch?v=" + videoId;

        //描述
        String description = null;
        if (videoRenderer.has("descriptionSnippet")) {
            JsonArray descriptions = videoRenderer.get("descriptionSnippet").getAsJsonObject().get("runs").getAsJsonArray();
            if (!descriptions.isEmpty()){
                description = descriptions.get(titles.size() - 1).getAsJsonObject().get("text").getAsString();
            }
        }

        //时长
        String lengthText = videoRenderer.get("lengthText").getAsJsonObject().get("simpleText").getAsString();
        Integer duration = ParseTime.toSecond(lengthText);

        //封装
        return VideoInfo.builder()
                .description(description)
                .duration(duration)
                .image(image)
                .title(title)
                .id(videoId)
                .url(url)
                .build();
    }
}
