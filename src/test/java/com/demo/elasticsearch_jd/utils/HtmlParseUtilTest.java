package com.demo.elasticsearch_jd.utils;

import org.junit.jupiter.api.Test;

/**
 * @Description TODO
 * @Author didi
 * @Date 2022/2/6 21:31
 **/
public class HtmlParseUtilTest {

    @Test
    public void test(){
        String key = "心理学";
        HtmlParseUtil.getContent(key).forEach(System.out::println);
    }
}
