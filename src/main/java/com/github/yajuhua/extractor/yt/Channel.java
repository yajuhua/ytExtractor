package com.github.yajuhua.extractor.yt;

import com.github.yajuhua.extractor.yt.pojo.PageInfo;
import com.github.yajuhua.extractor.yt.pojo.VideoInfo;
import com.github.yajuhua.extractor.yt.tab.Streams;
import com.github.yajuhua.extractor.yt.tab.Tabs;
import com.github.yajuhua.extractor.yt.tab.Videos;
import com.github.yajuhua.extractor.yt.utils.Http;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

/**
 * 获取https://www.youtube.com/@bulianglin or https://www.youtube.com/channel/xxxx
 */
public class Channel {

    /**
     * 获取channel页面信息
     * @param url
     * @return
     */
    public static PageInfo getInfo(String url) throws Exception {
        String name = null;
        String image = null;
        String description = null;
        String id = null;
        Document document = Jsoup.parse(Http.getHtml(url));
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

    /**
     * 获取页面初始化视频列表
     * @return
     */
    public static List<VideoInfo> getInit(String url, Video.Sorted sorted) throws Exception {
        String contentType = Tabs.getChannelContentType(url);
        if ("streams".equalsIgnoreCase(contentType)){
            return Streams.getInit(url,sorted);
        }else {
            return Videos.getInit(url,sorted);
        }
    }

    /**
     * 获取最新视频
     * @param url
     * @return
     */
    public static VideoInfo getLatest(String url) throws Exception {
        List<VideoInfo> init = getInit(url, Video.Sorted.Default);
        if (init != null && !init.isEmpty()){
            return init.get(0);
        }
        throw new Exception("找不到最新视频");
    }

    /**
     * 获取更多视频
     * @param url
     * @return
     */
    public static List<VideoInfo> getMore(String url) throws Exception {
        //暂不开发
        return getInit(url, Video.Sorted.Default);
    }
}
