package com.atguigu.gmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParam;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.util.RedisUtil;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.Update;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ListServiceImpl implements ListService {
    @Autowired
    JestClient jestClient;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public List<SkuLsInfo> search(SkuLsParam skuLsParam) {

        // (1)创建查询工具对象
        SearchSourceBuilder searchTools = new SearchSourceBuilder();

        // (2)创建bool对象
        BoolQueryBuilder bool = new BoolQueryBuilder();

        // (3)创建terms对象，作为过滤查询条件
           // 3级ID过滤
        if(StringUtils.isNotBlank(skuLsParam.getCatalog3Id())){
            TermsQueryBuilder term1 = new TermsQueryBuilder("catalog3Id",skuLsParam.getCatalog3Id());
            // (4)filter调用term
            bool.filter(term1);
        }

        // 属性值id过滤
        String[] valueIds = skuLsParam.getValueId();
        if(valueIds!=null && valueIds.length>0){
            for (int i = 0; i < valueIds.length; i++) {
                String value = valueIds[i];
                TermsQueryBuilder term = new TermsQueryBuilder("skuAttrValueList.valueId",value);
                bool.filter(term);
            }

        }

      /*  String[] valueIds = new String[2];
        valueIds[0] = "43";
        valueIds[1] = "48";
        TermsQueryBuilder term2 = new TermsQueryBuilder("skuAttrValueList.valueId",valueIds);*/



        // (5)查询条件
        //创建match
        if(StringUtils.isNotBlank(skuLsParam.getKeyword())){
            MatchQueryBuilder match = new MatchQueryBuilder("skuName",skuLsParam.getKeyword());
            bool.must(match);
        }
        // 排序，分页from和size，高亮
        searchTools.sort("hotScore");
        searchTools.from(0);
        searchTools.size(80);
        HighlightBuilder h = new HighlightBuilder();
        h.preTags("<span style='color:red;font-weight:bolder'>");
        h.field("skuName");
        h.postTags("</span>");
        searchTools.highlight(h);

        // (6)把bool放入query中
        searchTools.query(bool);

        // 加上聚合查询
        TermsBuilder aggs_valueId = AggregationBuilders.terms("aggs_valueId").field("skuAttrValueList.valueId");
        searchTools.aggregation(aggs_valueId);

        // (7)把searchTools对象转换为字符串、
        String query = searchTools.toString();
        System.out.print(query);

        // 1.执行查询语句
        Search search = new Search.Builder(query).addIndex("gmall").addType("SkuLsInfo").build();

        // 2.返回执行结果
        List<SkuLsInfo> skuList = new ArrayList<SkuLsInfo>();
        SearchResult execute = null;
        try {
            execute = jestClient.execute(search);
            //把结果解析为java对象的集合
            List<SearchResult.Hit<SkuLsInfo, Void>> hits = execute.getHits(SkuLsInfo.class);

            for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
                SkuLsInfo sku = hit.source;

                if(StringUtils.isNotBlank(skuLsParam.getKeyword())){
                    Map<String, List<String>> highlight = hit.highlight;

                    String highlightSkuName =  highlight.get("skuName").get(0);

                    sku.setSkuName(highlightSkuName);

                }

                skuList.add(sku);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 解析聚合结果
        MetricAggregation aggregations = execute.getAggregations();
        TermsAggregation groupby_valueId = aggregations.getTermsAggregation("aggs_valueId");
        List<TermsAggregation.Entry> buckets = groupby_valueId.getBuckets();
        // 封装进入集合
        List<String> valueIdList=new ArrayList<>(buckets.size());
        for (TermsAggregation.Entry bucket : buckets) {
            String valueId = bucket.getKey();
            valueIdList.add(valueId);
        }

        return skuList;
    }

    @Override
    public void incrHotScore(String skuId) {
        Jedis jedis = redisUtil.getJedis();
        int timesToEs=100;
        Double hotScore = jedis.zincrby("hotScore", 1, "skuId:" + skuId);
        if(hotScore%timesToEs==0){
            updateHotScore(  skuId,  Math.round(hotScore)   );
        }

    }

    private void updateHotScore(String skuId,Long hotScore){
        String updateJson="{\n" +
                "   \"doc\":{\n" +
                "     \"hotScore\":"+hotScore+"\n" +
                "   }\n" +
                "}";

        Update update = new Update.Builder(updateJson).index("gmall").type("SkuLsInfo").id(skuId).build();
        try {
            jestClient.execute(update);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
