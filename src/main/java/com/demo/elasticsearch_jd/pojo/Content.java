package com.demo.elasticsearch_jd.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description 商品信息
 * @Author Huangsibo
 * @Date 2022/1/23 22:59
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Content {
    //名称
    private String title;
    //价格
    private String price;
    //图片
    private String img;
}
