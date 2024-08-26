package com.github.yajuhua.extractor.yt.tab;

import com.github.yajuhua.extractor.yt.Video;
import com.github.yajuhua.extractor.yt.pojo.VideoInfo;

import java.util.List;

/**
 * https://www.youtube.com/@laogao/streams中视频列表
 */
public class Streams {

    /**
     * 获取https://www.youtube.com/@laogao/streams初始化的视频列表
     * @param url
     * @param sorted
     * @return
     */
    public static List<VideoInfo> getInit(String url, Video.Sorted sorted) throws Exception {
        List<VideoInfo> videoList = Tabs.getVideoList(url);
        return Video.sorted(videoList,sorted);
    }

    /**
     * 获取https://www.youtube.com/@laogao/streams更多的视频列表
     * @param url
     * @param sorted
     * @return
     */
    public static List<VideoInfo> getMore(String url,Video.Sorted sorted) throws Exception {
        //暂不开发
        return getInit(url,sorted);
    }
}
