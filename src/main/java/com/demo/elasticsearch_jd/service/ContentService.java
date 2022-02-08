package com.demo.elasticsearch_jd.service;

import com.alibaba.fastjson.JSON;
import com.demo.elasticsearch_jd.pojo.Content;
import com.demo.elasticsearch_jd.utils.HtmlParseUtil;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Description TODO
 * @Author didi
 * @Date 2022/2/6 21:37
 **/
@Service
public class ContentService {

    @Autowired
    private RestHighLevelClient client;

    /**
     * 判断索引是否存在
     * @param index
     * @return
     */
    public boolean isIndexExist(String index) {
        GetIndexRequest request = new GetIndexRequest(index);
        try {
            boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
            if (exists) {
                return true;
            }
        } catch (IOException e) {
            System.out.println("判断" + index + "索引是否存在请求失败");
        }
        return false;
    }

    /**
     * 删除索引
     * @param index
     */
    public void deleteIndex(String index) {
        DeleteIndexRequest request = new DeleteIndexRequest(index);
        request.timeout("2m");
        try {
            client.indices().delete(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            System.out.println(index + "索引删除失败");
        }
    }

    /**
     * 创建索引
     * @param index
     * @return
     */
    public boolean createIndex(String index){
        CreateIndexRequest request = new CreateIndexRequest(index);
        request.settings(Settings.builder()
                //设置分片数
                .put("index.number_of_shards", 3)
                //设置副本数
                .put("index.number_of_replicas", 1)
                .build());
        request.setTimeout(TimeValue.timeValueSeconds(2));
        request.mapping(
                "{\"properties\":{\"title\":{\"type\":\"text\"},\"price\":{\"type\":\"text\"},\"img\":{\"type\":\"text\"}}}",
                XContentType.JSON);

        try {
            client.indices().create(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            System.out.println("索引" + index + "创建失败");
        }

        return true;
    }

    /**
     * 根据关键字key，在京东商品页面搜索相关商品，然后将商品名称和价格插入到ES中
     * @param key 关键字
     * @return
     */
    public boolean parseContent(String key) {
        List<Content> contents = HtmlParseUtil.getContent(key);
        //创建一个批量请求
        BulkRequest bulkRequest = new BulkRequest();
        //设置超时时间
        bulkRequest.timeout("2m");
        //ES索引名称
        String index = "jd_goods";

        for (Content content : contents) {
            bulkRequest.add(new IndexRequest(index)
                    .source(JSON.toJSONString(content), XContentType.JSON));
        }

        BulkResponse bulkResponse = null;
        try {
            bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return !bulkResponse.hasFailures();
    }

    /**
     * 分页搜索，高亮显示
     * @param key 关键字
     * @param pageNumber 页面编号
     * @param pageSize 页面大小
     * @return
     */
    public List<Map<String,Object>> searchContent(String key, int pageNumber, int pageSize) throws IOException, InterruptedException {
        if(pageNumber < 1){
            pageNumber = 1;
        }

        //当索引不存在时，创建索引
        if( !isIndexExist("jd_goods") ){
            createIndex("jd_goods");
        }

        parseContent(key);

        Thread.sleep(300);

        //ES索引名称
        String index = "jd_goods";
        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //分页
        searchSourceBuilder.from(pageNumber);
        searchSourceBuilder.size(pageSize);
        //模糊查找
        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("title", key);
        searchSourceBuilder.query(matchQueryBuilder);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        //高亮搜索
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        //关闭多个高亮显示
        highlightBuilder.requireFieldMatch(false);
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlighter(highlightBuilder);

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        ArrayList<Map<String, Object>> list = new ArrayList<>();
        for(SearchHit documentFields: searchResponse.getHits().getHits()){
            //解析高亮的字段
            //1.获取高亮的字段
            Map<String, HighlightField> highlightFields = documentFields.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            //原来的结果
            Map<String, Object> sourceAsMap = documentFields.getSourceAsMap();
            //高亮就是将原来的字段换为高亮的字段即可
            if(title!=null){
                Text[] fragments = title.getFragments();
                String newTitle = "";
                for (Text fragment : fragments) {
                    newTitle += fragment;
                }
                //高亮字段的替换
                sourceAsMap.put("title",newTitle);
            }
            list.add(sourceAsMap);
        }

        return list;
    }
 }
