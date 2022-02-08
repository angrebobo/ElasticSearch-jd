package com.demo.elasticsearch_jd.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;

/**
 * @Description 创建一个ES客户端
 * @Author HuangSiBo
 * @Date 2022/2/7 14:17
 **/
public class ElasticSearchClientConfig {

    @Bean
    public RestHighLevelClient restHighLevelClient(){
        RestHighLevelClient restHighLevelClient = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("127.0.0.1", 9200, "http")));

        return restHighLevelClient;
    }
}
