package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.SkuInfo;

import java.util.List;

public interface SkuService {
    List<SkuInfo> skuInfoListBySpu(String spuId);

    void saveSku(SkuInfo skuInfo);

    SkuInfo getItem(String skuId);

    List<SkuInfo> skuAttrValueListBySpu(String spuId);

}
