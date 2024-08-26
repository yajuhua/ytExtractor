package com.github.yajuhua.extractor.yt;

import com.github.yajuhua.extractor.yt.pojo.VideoInfo;
import com.github.yajuhua.extractor.yt.utils.Http;
import com.github.yajuhua.extractor.yt.utils.ParseTime;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * 关于视频
 */
public class Video {

    /**
     * 升序（Ascending Order）: ASC 根据发布时间升序
     * 降序（Descending Order）: DESC 根据发布时间降序
     */
   public enum Sorted{
        /**
         * 根据发布时间升序
         */
        ACSByTime,
        /**
         * 根据发布时间降序
         */
        DESCByTime,
        /**
         * 默认
         */
        Default//默认
    }

    /**
     * 比较两个视频发布时间,如果第一个大返回1
     * @param url1
     * @param url2
     * @return
     */
    public static Integer comparePublicTime(String url1,String url2){
        String htmlStr1 = Http.get(url1, null, null);
        String htmlStr2 = Http.get(url2, null, null);

        LocalDate publicTime1 = getPublicTime(htmlStr1);
        LocalDate publicTime2 = getPublicTime(htmlStr2);

        return publicTime1.isAfter(publicTime2) ?1:-1;
    }

    /**
     * 排序视频列表
     * @param videoInfoList
     * @param sorted
     * @return
     */
    public static List<VideoInfo> sorted(List<VideoInfo> videoInfoList,Sorted sorted){
        if (sorted.equals(Video.Sorted.Default)){
            return videoInfoList;
        } else {
            VideoInfo info1 = videoInfoList.get(0);
            VideoInfo info2 = videoInfoList.get(videoInfoList.size() - 1);
            //比较时间
            Integer comparePublicTime = Video.comparePublicTime(info1.getUrl(), info2.getUrl());

            if (sorted.equals(Video.Sorted.ACSByTime)){
                if (comparePublicTime != -1){
                    Collections.reverse(videoInfoList);
                    return videoInfoList;
                }
            }else {
                if (comparePublicTime != 1){
                    Collections.reverse(videoInfoList);
                    return videoInfoList;
                }
            }
        }
        return videoInfoList;
    }

    /**
     * 获取视频发布时间
     * @param htmlStr
     * @return
     */
    private static LocalDate getPublicTime(String htmlStr){
        //获取发布时间字符串
        JsonObject videoPrimaryInfoRenderer = getContents(htmlStr).get(0).getAsJsonObject().get("videoPrimaryInfoRenderer")
                .getAsJsonObject();
        String publishDateText = videoPrimaryInfoRenderer.get("dateText").getAsJsonObject()
                .get("simpleText").getAsString();

        //解析时间
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);
        LocalDate publishDate = null;
        try {
            publishDate = LocalDate.parse(publishDateText, formatter);
        } catch (Exception e) {
            publishDate = LocalDate.now();
        }
        return publishDate;
    }

    /**
     * contents.twoColumnWatchNextResults.results.results.contents[]
     * @param htmlStr
     * @return
     */
    private static JsonArray getContents(String htmlStr){
        JsonObject ytInitialData = getYtInitialData(htmlStr);
        JsonArray contents = ytInitialData.get("contents").getAsJsonObject()
                .getAsJsonObject("twoColumnWatchNextResults").getAsJsonObject()
                .get("results").getAsJsonObject().get("results").getAsJsonObject().get("contents").getAsJsonArray();
        return contents;
    }

    /**
     * 获取ytInitialData并转换成json对象
     * @param htmlStr
     * @return
     */
    private static JsonObject getYtInitialData(String htmlStr){
        Document document = getVideoHtmlDoc(htmlStr);
        Elements scripts = document.getElementsByTag("script");
        Gson gson = new Gson();
        for (Element script : scripts) {
            if (script.data().startsWith("var ytInitialData")) {
                String data = script.data();
                String json = data.substring(data.indexOf('{'), data.length() - 1);
                return gson.fromJson(json, JsonObject.class);
            }
        }
        return null;
    }

    /**
     * 获取video页面的html Doc对象
     * @param htmlStr
     * @return
     */
    private static Document getVideoHtmlDoc(String htmlStr){
        Document document = Jsoup.parse(htmlStr);
        return document;
    }

    /**
     * 解析单个videoRenderer
     * @param videoRenderer
     * @return
     */
    public static VideoInfo getVideoInfo(JsonObject videoRenderer){
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

        //时长,如果找不到时长，那么说明正在直播，直接返回null
        String lengthText = null;
        if (videoRenderer.has("lengthText")){
            lengthText = videoRenderer.get("lengthText").getAsJsonObject().get("simpleText").getAsString();
        }else {
            return null;
        }
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
