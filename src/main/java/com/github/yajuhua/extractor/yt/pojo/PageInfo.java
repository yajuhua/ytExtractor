package com.github.yajuhua.extractor.yt.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 封装playlist和channel页面的信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageInfo {
    private String name;
    private String image;
    private String description;
    private String id;
}
