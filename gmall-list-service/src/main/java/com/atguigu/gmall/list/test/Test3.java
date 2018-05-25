package com.atguigu.gmall.list.test;

import com.atguigu.gmall.bean.SkuAttrValue;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.list.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.list.mapper.SkuInfoMapper;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.beanutils.BeanUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Test3 {
    @Autowired
    SkuInfoMapper skuInfoMapper;

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    JestClient jestClient;

    @Test
    public void test2() {
        // 查询数据使用search对象
        // es的dsl的查询语句：（1）直接使用字符串 （2）使用es查询工具SearchSourceBuilder对象

        // (1)创建查询工具对象
        SearchSourceBuilder searchTools = new SearchSourceBuilder();

        // (2)创建bool对象
        BoolQueryBuilder bool = new BoolQueryBuilder();

        // (3)创建terms对象，作为过滤查询条件
        TermsQueryBuilder term1 = new TermsQueryBuilder("catalog3Id","61");

        String[] valueIds = new String[2];
        valueIds[0] = "43";
        valueIds[1] = "48";
        TermsQueryBuilder term2 = new TermsQueryBuilder("skuAttrValueList.valueId",valueIds);

        // (4)filter调用term
        bool.filter(term1);
        bool.filter(term2);

        // (5)查询条件
        //创建match
        MatchQueryBuilder match = new MatchQueryBuilder("skuName","小米大米");
        bool.must(match);

        // (6)把bool放入query中
        searchTools.query(bool);

        // (7)把searchTools对象转换为字符串、
        String query = searchTools.toString();


        // 1.执行查询语句
        Search search = new Search.Builder(query).addIndex("gmall").addType("SkuLsInfo").build();

        // 2.返回执行结果
        List<SkuLsInfo> skuList = new ArrayList<SkuLsInfo>();
        try {
            SearchResult execute = jestClient.execute(search);
            //把结果解析为java对象的集合
            List<SearchResult.Hit<SkuLsInfo, Void>> hits = execute.getHits(SkuLsInfo.class);

            for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
                SkuLsInfo sku = hit.source;

                skuList.add(sku);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.print(skuList);
    }

    @Test
    public void test1() {
        // 添加数据到elasticsearch服务器
        // 1.先查找数据库中的数据
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setCatalog3Id("61");
        List<SkuInfo> skuInfoList = skuInfoMapper.select(skuInfo);

        for (SkuInfo sku : skuInfoList) {
            SkuAttrValue skuAttrValue = new SkuAttrValue();
            skuAttrValue.setSkuId(sku.getId());
            List<SkuAttrValue> skuAttrValueList = skuAttrValueMapper.select(skuAttrValue);

            sku.setSkuAttrValueList(skuAttrValueList);
        }
        System.out.print(skuInfoList);

        // 2.把skuInfo对象的值封装到skulsInfo对象中,并把skulsInfo对象塞到集合中
        List<SkuLsInfo> skuLsInfos = new ArrayList<SkuLsInfo>();

        //采用BeanUtil工具类封装

        try {
            for (SkuInfo sku : skuInfoList) {
                SkuLsInfo skuLsInfo = new SkuLsInfo();
                BeanUtils.copyProperties(skuLsInfo, sku);

                skuLsInfos.add(skuLsInfo);

            }

            // 打印对象的json
            //System.out.print(JSON.toJSONString(skuLsInfos));

            for (SkuLsInfo skuLsInfo : skuLsInfos) {
                // 3.把转化后的skuLsInfo对象一一放置到Es中
                Index index = new Index.Builder(skuLsInfo).index("gmall").type("SkuLsInfo").id(skuLsInfo.getId()).build();

                try {
                    jestClient.execute(index);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }
}
