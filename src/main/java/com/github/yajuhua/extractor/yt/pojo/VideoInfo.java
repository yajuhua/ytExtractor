package com.github.yajuhua.extractor.yt.pojo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VideoInfo {
    private String title;
    private String image;
    private String id;
    private String url;
    private String description;
    private Integer duration;
}
