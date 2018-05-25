package com.atguigu.gmall.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.AttrService;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class SkuController {
    @Reference
    SpuService spuService;

    @Reference
    SkuService skuService;

    @Reference
    AttrService attrService;

    @RequestMapping(value = "saveSku")
    @ResponseBody
    public String saveSku(SkuInfo skuInfo){

        skuService.saveSku(skuInfo);
        return "success";
    }

    @RequestMapping(value = "saleAttrValueList")
    @ResponseBody
    public List<SpuSaleAttr> saleAttrValueList(@RequestParam Map<String,String> map){
        String spuId = map.get("spuId");

        List<SpuSaleAttr> list = attrService.saleAttrValueList(spuId);

        return list;
    }

    @RequestMapping(value = "attrInfoList")
    @ResponseBody
    public List<BaseAttrInfo> attrInfoList(@RequestParam Map<String,String> map){
        String catalog3Id = map.get("catalog3Id");

        List<BaseAttrInfo> list = attrService.attrInfoList(catalog3Id);

        return list;
    }

    @RequestMapping(value = "getSpuImageList")
    @ResponseBody
    public List<SpuImage> getSpuImageList(@RequestParam Map<String,String> map){
        String spuId = map.get("spuId");

        List<SpuImage> spuImageList = spuService.getSpuImageList(spuId);

        return spuImageList;
    }

    @RequestMapping(value = "skuInfoListBySpu")
    @ResponseBody
    public List<SkuInfo> skuInfoListBySpu(@RequestParam Map<String,String> map){
        String spuId = map.get("spuId");

        List<SkuInfo> skuInfoList = skuService.skuInfoListBySpu(spuId);

        return skuInfoList;
    }
}
