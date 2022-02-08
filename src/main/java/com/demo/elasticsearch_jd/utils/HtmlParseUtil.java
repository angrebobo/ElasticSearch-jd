package com.demo.elasticsearch_jd.utils;

import com.demo.elasticsearch_jd.pojo.Content;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description
 * @Author Huangsibo
 * @Date 2022/1/23 22:59
 **/
@Component
public class HtmlParseUtil {
    private static final Logger logger = Logger.getLogger(HtmlParseUtil.class);
    private static final String URL = "https://search.jd.com/Search?keyword=";

    /**
     * 获取商品的名称，价格，图片
     */
    public static List<Content> getContent(String key){

        List<Content> contents = new ArrayList<>();

        Document document = null;
        try {
            document = Jsoup.parse(new URL(URL + key + "&enc=utf-8"), 3000);
        } catch (IOException e) {
            logger.info("HtmlParseUtil.getContent() 失败");
        }

        // 每个li标签保存一本书的信息，li标签下是一个div，class是 gl-i-wrap
        Elements formElement = document.getElementsByClass("gl-i-wrap");
        // 从每个div中提取出书籍信息
        for(Element element : formElement){
            String title = element.getElementsByClass("p-name").eq(0).text();
            String price = element.select(".p-price > strong > i").text();
            String img = element.getElementsByTag("img").eq(0).attr("data-lazy-img");
            contents.add(Content.builder()
                    .title(title)
                    .price(price)
                    .img(img)
                    .build());
        }
        return contents;
    }
}
