package com.github.yajuhua.extractor.yt.downloader;

import com.sun.istack.internal.NotNull;

import java.io.IOException;

/**
 * http请求获取数据的下载器
 */
public abstract class Downloader {
    /**
     * 执行http下载请求
     * @param request
     * @return
     * @throws IOException
     */
    public abstract Response execute(@NotNull Request request)throws IOException;
}
