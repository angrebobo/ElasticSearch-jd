package com.demo.elasticsearch_jd.controller;

import com.demo.elasticsearch_jd.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @Description TODO
 * @Author didi
 * @Date 2022/2/7 14:44
 **/
@RestController
public class ContentController {

    @Autowired
    private ContentService contentService;

//    @GetMapping("/parse/{key}")
//    public boolean parse(@PathVariable("key") String key) throws IOException {
//        return contentService.parseContent(key);
//    }


    @GetMapping("/search/{key}/{pageNumber}/{pageSize}")
    public List<Map<String,Object>> search(@PathVariable("key") String key,
                                           @PathVariable("pageNumber") int pageNumber,
                                           @PathVariable("pageSize") int pageSize) throws IOException, InterruptedException {
        return contentService.searchContent(key, pageNumber, pageSize);
    }

//    @GetMapping("/create/{key}")
//    public void create(@PathVariable("key") String key){
//        contentService.createIndex(key);
//    }
//
//    @GetMapping("/delete/{key}")
//    public void delete(@PathVariable("key") String key){
//        contentService.deleteIndex(key);
//    }

}
