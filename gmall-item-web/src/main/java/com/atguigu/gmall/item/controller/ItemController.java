package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuSaleAttrValue;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.service.AttrService;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ItemController {
    @Reference
    SkuService skuService;

    @Reference
    AttrService attrService;

    @Reference
    SpuService spuService;

    @Reference
    ListService listService;

    @RequestMapping("index")
    public String index(){
        return "item";
    }

    @RequestMapping("/{skuId}.html")
    public String getItem(@PathVariable String skuId, ModelMap map){
        SkuInfo skuInfo = skuService.getItem(skuId);
        map.put("skuInfo",skuInfo);

        // 销售属性列表
        List<SpuSaleAttr> saleAttrList = attrService.spuSaleAttrListCheckBySku(skuId);
        map.put("spuSaleAttrListCheckBySku",saleAttrList);

        // HashTable的表的查询
        String spuId = spuService.getSpuBySku(skuId);
        List<SkuInfo> skuInfos = skuService.skuAttrValueListBySpu(spuId);

        // 拼接sku列表的hash表，放到页面
        // 61|71|81 : 78
        // 颜色/容量/版本 ：skuId
        // 页面hash表的sku的key的顺序需要跟销售属性列表的顺序一直，方便查找sku的hash表

        //[{"61|71|81":"78"},{"61|71|82":"81"},{"62|71|81":"79"},{"61|72|81":"80"}]
        //{"65|66|69|72":"72","64|68|69|72":"73","63|66|69|72":"18","65|68|70|73":"35","64|66|69|72":"74","65|68|70|72":"25","63|66|71|72":"26","64|67|71|73":"24","65|68|71|74":"19","63|66|70|74":"17"}

        Map<String,String> valueMap = new HashMap<String,String>();
        for (SkuInfo sk:skuInfos) {
            String sku_Id = sk.getId(); // 获取sku_id

            String valueKey = "";

            List<SkuSaleAttrValue> skuSaleAttrValueList = sk.getSkuSaleAttrValueList();

            int i = 0;
            for (SkuSaleAttrValue skuSaleAttrValue:skuSaleAttrValueList) {
                i++;
                if(i > 1){
                    valueKey = valueKey + "|";
                }
                valueKey += skuSaleAttrValue.getSaleAttrValueId();
            }
            // 把获取的value_id和sku_id放入map中
            valueMap.put(valueKey,sku_Id);
        }
        // 把valueMap转化为json字符串
        String json = JSON.toJSONString(valueMap);
       // System.out.print(json);

        // 设置到request域中
        map.put("valueMap",json);
        map.put("sku_id",skuInfo.getId());


        // 更新热度
        listService.incrHotScore(skuId);


        return "item";
    }
}
