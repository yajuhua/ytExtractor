package com.github.yajuhua.extractor.yt;

import com.github.yajuhua.extractor.yt.utils.Http;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Builder;
import lombok.Data;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;

/**
 * 获取频道详细信息
 */
@Data
@Builder
public class ChannelInfo {
    private String name;
    private String image;
    private String description;
    private String id;
    private List<String> tabNames;
    public static ChannelInfo getInfo(String url) throws Exception{
        //获取html
        String htmlStr = Http.get(url, null, null);

        String name = null;
        String image = null;
        String description = null;
        String id = null;
        List<String> tabNames = new ArrayList<>();
        Document document = Jsoup.parse(htmlStr);
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

        //获取tab
        for (String s : getTabRenderer(htmlStr).keySet()) {
            tabNames.add(s);
        }

        //封装返回
        return ChannelInfo.builder()
                .description(description)
                .image(image)
                .tabNames(tabNames)
                .name(name)
                .id(id)
                .build();
    }

    /**
     * 获取频道tab集合
     * @param htmlStr html页面字符串
     * @return tabName:tabRenderer
     * @throws Exception
     */
    public static Map<String, JsonObject> getTabRenderer(String htmlStr) throws Exception {
        Map<String,JsonObject> tabsMap = new HashMap<>();
        Document document = Jsoup.parse(htmlStr);
        Elements scripts = document.getElementsByTag("script");
        Gson gson = new Gson();
        for (Element script : scripts) {
            if (script.data().startsWith("var ytInitialData")){
                String data = script.data();
                String json = data.substring(data.indexOf('{'),data.length() -1);
                JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
                JsonArray tabs = jsonObject.get("contents").getAsJsonObject().get("twoColumnBrowseResultsRenderer").getAsJsonObject().get("tabs").getAsJsonArray();
                for (JsonElement tab : tabs) {
                    if (!tab.getAsJsonObject().has("tabRenderer")){
                        continue;
                    }
                    JsonObject tabRenderer = tab.getAsJsonObject().get("tabRenderer").getAsJsonObject().getAsJsonObject();
                    if (!tabRenderer.isJsonNull() && tabRenderer.has("title") && !tabRenderer.get("title").isJsonNull()){
                        tabsMap.put(tabRenderer.get("title").getAsString(),tabRenderer);
                    }
                }
            }
        }
        return tabsMap;
    }
}
