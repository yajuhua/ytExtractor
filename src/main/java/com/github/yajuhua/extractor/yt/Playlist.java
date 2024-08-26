package com.github.yajuhua.extractor.yt;

import com.github.yajuhua.extractor.yt.downloader.Request;
import com.github.yajuhua.extractor.yt.downloader.Response;
import com.github.yajuhua.extractor.yt.pojo.PageInfo;
import com.github.yajuhua.extractor.yt.pojo.VideoInfo;
import com.github.yajuhua.extractor.yt.utils.Http;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;

public class Playlist {

    /**
     * 获取https://www.youtube.com/playlist?list=XXX页面的sectionListRenderer，
     * 里面包含了视频信息和下一个视频列表的token
     * @param pageInitialData
     * @return
     */
    private static JsonObject getSectionListRenderer(JsonObject pageInitialData){
        return pageInitialData.get("contents").getAsJsonObject()
                .get("twoColumnBrowseResultsRenderer").getAsJsonObject()
                .get("tabs").getAsJsonArray().get(0).getAsJsonObject()
                .get("tabRenderer").getAsJsonObject().get("content")
                .getAsJsonObject().get("sectionListRenderer")
                .getAsJsonObject();
    }

    /**
     * 获取https://www.youtube.com/playlist?list=XXX页面初始的视频列表，最多100个
     * @param url 视频列表链接
     * @param sorted 排序
     * @return
     */
    public static List<VideoInfo> getInit(String url,Video.Sorted sorted) throws Exception {
        //构建请求参数
        Request get = Request.builder()
                .httpMethod("GET")
                .url(url)
                .build();
        //响应数据
        Response response = YtExtrator.getDownloader().execute(get);
        if (response.getResponseCode() != 200){
            throw new Exception(response.getResponseMessage());
        }
        Document pageDoc = Page.getPageDoc(response.getResponseBody());
        return getInit(pageDoc,sorted);
    }

    /**
     * 获取https://www.youtube.com/playlist?list=XXX页面初始的视频列表，最多100个
     * @param document html页面Document对象
     * @param sorted 排序
     * @return
     */
    private static List<VideoInfo> getInit(Document document,Video.Sorted sorted) throws Exception {

        //获取html页面json数据并进行解析
        JsonObject pageInitialData = Page.getPageInitialData(document.toString());
        JsonObject sectionListRenderer = getSectionListRenderer(pageInitialData);
        JsonArray playlistVideoListRendererArr =  sectionListRenderer.get("contents").getAsJsonArray().get(0)
                .getAsJsonObject().get("itemSectionRenderer")
                .getAsJsonObject().get("contents").getAsJsonArray()
                .get(0).getAsJsonObject().get("playlistVideoListRenderer")
                .getAsJsonObject().get("contents").getAsJsonArray();

        //封装数据
        List<VideoInfo> videoInfoList = new ArrayList<>();
        for (JsonElement vr : playlistVideoListRendererArr) {
            JsonObject object = vr.getAsJsonObject();
            if (object.has("playlistVideoRenderer")){
                JsonObject playlistVideoRenderer = object.get("playlistVideoRenderer").getAsJsonObject();
                VideoInfo videoInfo = Video.getVideoInfo(playlistVideoRenderer);
                if (videoInfo != null){
                    videoInfoList.add(videoInfo);
                }
            }
        }
        //排序
        return Video.sorted(videoInfoList,sorted);
    }

    /**
     * 获取更多视频
     * @param url https://www.youtube.com/playlist?list=XXX
     * @param sorted
     * @return 初始 + 更多
     * @throws Exception
     */
    public static List<VideoInfo> getMore(String url,Video.Sorted sorted) throws Exception{
        //构建请求
        Request get = Request.builder()
                .url(url)
                .httpMethod("GET")
                .build();
        Response response = YtExtrator.getDownloader().execute(get);
        if (response.getResponseCode() != 200){
            throw new Exception(response.getResponseMessage());
        }
        //获取初始视频列表
        String htmlStr = response.getResponseBody();
        Document pageDoc = Page.getPageDoc(htmlStr);
        List<VideoInfo> initVideoList = getInit(pageDoc, Video.Sorted.Default);

        //初始化时最多显示100个视频
        Integer playlistCount = gerPlaylistCount(htmlStr);
        if (playlistCount != null && playlistCount > 100){
            //获取更多视频(最多200)
            //获取init的token
            String initContinuationToken =
                    getContinuationToken(getSectionListRenderer(Page.getPageInitialData(htmlStr)));

            //获取响应数据
            String nextResponse = getNextResponse(url, initContinuationToken);
            JsonArray continuationItems = getContinuationItems(nextResponse);

            //next的视频列表
            List<VideoInfo> nextVideoList = get(continuationItems);
            initVideoList.addAll(nextVideoList);

            //下一个token
            String nextResponseContinuationToken = getContinuationToken(continuationItems);
            while (nextResponseContinuationToken != null){
                nextResponse = getNextResponse(url,nextResponseContinuationToken);
                continuationItems = getContinuationItems(nextResponse);
                nextResponseContinuationToken =
                        getContinuationToken(getContinuationItems(nextResponse));
                //next的视频列表
                nextVideoList = get(continuationItems);
                initVideoList.addAll(nextVideoList);
            }
        }
        //排序
        return Video.sorted(initVideoList,sorted);
    }

    /**
     * 获取playlist.html 页面中的continuation.token
     * @param sectionListRenderer
     * @return token
     */
    private static String getContinuationToken(JsonObject sectionListRenderer){
        JsonArray contents = sectionListRenderer.get("contents").getAsJsonArray();
        JsonArray contents2 = contents.get(0).getAsJsonObject().get("itemSectionRenderer")
                .getAsJsonObject()
                .get("contents").getAsJsonArray().get(0).getAsJsonObject()
                .get("playlistVideoListRenderer")
                .getAsJsonObject().get("contents").getAsJsonArray();
        for (JsonElement element : contents2) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("continuationItemRenderer")){
                JsonObject continuationItemRenderer =
                        object.get("continuationItemRenderer").getAsJsonObject();
                JsonObject continuationEndpoint = continuationItemRenderer
                        .get("continuationEndpoint").getAsJsonObject();
                String token = continuationEndpoint.get("continuationCommand")
                        .getAsJsonObject().get("token").getAsString();
                return token;
            }
        }
        return null;
    }

    /**
     * 获取响应 https://www.youtube.com/youtubei/v1/browse?prettyPrint=false
     * @param url https://www.youtube.com/playlist?list=XXX
     * @param token continuationToken
     * @return
     */
    private static String getNextResponse(String url, String token) throws Exception {
        //构建请求头
        StringBuilder body = new StringBuilder();
        body.append("{").append(System.lineSeparator());
        body.append("  \"context\": {").append(System.lineSeparator());
        body.append("    \"client\": {").append(System.lineSeparator());
        body.append("      \"deviceMake\": \"\",").append(System.lineSeparator());
        body.append("      \"deviceModel\": \"\",").append(System.lineSeparator());
        body.append("      \"userAgent\": \"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36,gzip(gfe)\",").append(System.lineSeparator());
        body.append("      \"clientName\": \"WEB\",").append(System.lineSeparator());
        body.append("      \"clientVersion\": \"2.20240821.01.00\",").append(System.lineSeparator());
        body.append("      \"osName\": \"Windows\",").append(System.lineSeparator());
        body.append("      \"osVersion\": \"10.0\",").append(System.lineSeparator());
        body.append("      \"originalUrl\": \"" + url + "&themeRefresh=1\",").append(System.lineSeparator());
        body.append("      \"screenPixelDensity\": 2,").append(System.lineSeparator());
        body.append("      \"platform\": \"DESKTOP\",").append(System.lineSeparator());
        body.append("      \"clientFormFactor\": \"UNKNOWN_FORM_FACTOR\",").append(System.lineSeparator());
        body.append("      \"screenDensityFloat\": 1.5,").append(System.lineSeparator());
        body.append("      \"userInterfaceTheme\": \"USER_INTERFACE_THEME_DARK\",").append(System.lineSeparator());
        body.append("      \"browserName\": \"Chrome\",").append(System.lineSeparator());
        body.append("      \"browserVersion\": \"127.0.0.0\",").append(System.lineSeparator());
        body.append("      \"acceptHeader\": \"text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7\",").append(System.lineSeparator());
        body.append("      \"connectionType\": \"CONN_CELLULAR_3G\",").append(System.lineSeparator());
        body.append("      \"mainAppWebInfo\": {").append(System.lineSeparator());
        body.append("        \"graftUrl\": \""+ url +"\",").append(System.lineSeparator());
        body.append("        \"pwaInstallabilityStatus\": \"PWA_INSTALLABILITY_STATUS_UNKNOWN\",").append(System.lineSeparator());
        body.append("        \"webDisplayMode\": \"WEB_DISPLAY_MODE_BROWSER\",").append(System.lineSeparator());
        body.append("        \"isWebNativeShareAvailable\": true").append(System.lineSeparator());
        body.append("      },").append(System.lineSeparator());
        body.append("      \"timeZone\": \"Asia/Shanghai\"").append(System.lineSeparator());
        body.append("    },").append(System.lineSeparator());
        body.append("    \"request\": {").append(System.lineSeparator());
        body.append("      \"useSsl\": true,").append(System.lineSeparator());
        body.append("      \"internalExperimentFlags\": [],").append(System.lineSeparator());
        body.append("      \"consistencyTokenJars\": []").append(System.lineSeparator());
        body.append("    }").append(System.lineSeparator());
        body.append("  },").append(System.lineSeparator());
        body.append("  \"continuation\": \"").append(token).append("\"}");

        //发送请求
        Map<String,List<String>> headers = new HashMap<>();
        headers.put("Content-Type", Arrays.asList("application/json"));
        Request post = Request.builder()
                .url("https://www.youtube.com/youtubei/v1/browse?prettyPrint=false")
                .httpMethod("POST")
                .body(body.toString())
                .header(headers)
                .build();
        Response response = YtExtrator.getDownloader().execute(post);
        if (response.getResponseCode() != 200){
            throw new Exception(response.getResponseMessage());
        }

        return response.getResponseBody();
    }

    /**
     * 获取响应 https://www.youtube.com/youtubei/v1/browse?prettyPrint=false中的continuationItems
     * @param nextResponse
     * @return
     * @throws Exception
     */
    private static JsonArray getContinuationItems(String nextResponse) throws Exception{
        Gson gson = new Gson();
        JsonObject object = gson.fromJson(nextResponse, JsonObject.class);
        JsonArray onResponseReceivedActions =
                object.get("onResponseReceivedActions").getAsJsonArray();

        JsonObject appendContinuationItemsAction = onResponseReceivedActions.get(0)
                .getAsJsonObject()
                .get("appendContinuationItemsAction").getAsJsonObject();

        JsonArray continuationItems = appendContinuationItemsAction
                .get("continuationItems").getAsJsonArray();
        return continuationItems;
    }

    /**
     * 获取响应 https://www.youtube.com/youtubei/v1/browse?prettyPrint=false中的continuationItems的视频列表
     * @param continuationItems
     * @return
     */
    private static List<VideoInfo> get(JsonArray continuationItems){
        List<VideoInfo> videoInfoList = new ArrayList<>();
        for (JsonElement continuationItem : continuationItems) {
            JsonObject continuationItemObject = continuationItem.getAsJsonObject();
            if (continuationItemObject.has("playlistVideoRenderer")) {
                JsonObject playlistVideoRenderer = continuationItemObject.
                        getAsJsonObject("playlistVideoRenderer");
                VideoInfo videoInfo = Video.getVideoInfo(playlistVideoRenderer);
                if (videoInfo != null){
                    videoInfoList.add(videoInfo);
                }
            }
        }
        return videoInfoList;
    }

    /**
     * 获取响应 https://www.youtube.com/youtubei/v1/browse?prettyPrint=false中的continuationItems的token
     * @param continuationItems
     * @return
     */
    private static String getContinuationToken(JsonArray continuationItems){
        for (JsonElement continuationItem : continuationItems) {
            JsonObject object = continuationItem.getAsJsonObject();
            if (object.has("continuationItemRenderer")){
                return object.get("continuationItemRenderer").getAsJsonObject()
                        .get("continuationEndpoint").getAsJsonObject()
                        .get("continuationCommand").getAsJsonObject()
                        .get("token").getAsString();
            }
        }
        return null;
    }

    /**
     * 获取playlist中的视频总数
     * @param playlistHtmlStr
     * @return
     */
    private static Integer gerPlaylistCount(String playlistHtmlStr){
        Document document = Jsoup.parse(playlistHtmlStr);
        Elements scripts = document.getElementsByTag("script");
        Gson gson = new Gson();
        for (Element script : scripts) {
            if (script.data().startsWith("var ytInitialData")) {
                String data = script.data();
                String json = data.substring(data.indexOf('{'), data.length() - 1);
                JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

                //视频数在header.playlistHeaderRenderer.briefStats中的第1个
                //"briefStats": [{"runs": [{"text": "236"}, {"text": " 个视频"}]}],
                JsonArray briefStats = jsonObject.get("header").getAsJsonObject().get("playlistHeaderRenderer").getAsJsonObject()
                        .get("briefStats").getAsJsonArray();
                String text = briefStats.get(0).getAsJsonObject().get("runs").getAsJsonArray().get(0)
                        .getAsJsonObject().get("text").getAsString();
                return Integer.parseInt(text);
            }
        }
        return null;
    }

    /**
     * 获取playlist页面信息
     * @param url
     * @return
     */
    public static PageInfo getInfo(String url) throws Exception {
        String name = null;
        String image = null;
        String description = null;
        String id = null;
        Document document = Page.getPageDoc(Http.getHtml(url));
        Elements metaList = document.getElementsByTag("meta");

        //提取标签属性
        for (Element element : metaList) {
            String property = element.attr("property");
            String content = element.attr("content");

            if (!property.isEmpty() && property.equals("og:image")){
                image = content;
            }else if (!property.isEmpty() && property.equals("og:description")){
                description = content;
            }else if (!property.isEmpty() && property.equals("og:title")){
                name = content;
            }else if (!property.isEmpty() && property.equals("og:url")){
                if (content.contains("playlist?list=")){
                    id = content.split("=")[1];
                }else {
                    id = content.substring(content.lastIndexOf("/")+1);
                }
            }
        }
        //封装返回
        return PageInfo.builder()
                .name(name)
                .image(image)
                .description(description)
                .id(id)
                .build();
    }
}