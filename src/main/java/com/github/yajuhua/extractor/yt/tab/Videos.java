package com.github.yajuhua.extractor.yt.tab;

import com.github.yajuhua.extractor.yt.Video;
import com.github.yajuhua.extractor.yt.pojo.VideoInfo;

import java.util.List;

/**
 * https://www.youtube.com/@laogao/videos中视频列表
 */
public class Videos {

    /**
     * 获取https://www.youtube.com/@laogao/videos初始化的视频列表
     * @param url
     * @param sorted
     * @return
     */
    public static List<VideoInfo> getInit(String url, Video.Sorted sorted) throws Exception {
        List<VideoInfo> videoList = Tabs.getVideoList(url);
        return Video.sorted(videoList,sorted);
    }

    /**
     * 获取https://www.youtube.com/@laogao/videos更多的视频列表
     * @param url
     * @param sorted
     * @return
     */
    public static List<VideoInfo> getMore(String url,Video.Sorted sorted) throws Exception {
        //暂不开发
        return getInit(url,sorted);
    }
}
