package com.atguigu.gmall.manager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.manager.mapper.*;
import com.atguigu.gmall.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class SpuServiceImpl implements SpuService {
    @Autowired
    SpuMapper spuMapper;

    @Autowired
    BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    SpuImageMapper spuImageMapper;

    @Autowired
    SkuInfoMapper skuInfoMapper;

    @Override
    public List<SpuInfo> getSpuList(String catalog3Id) {
        return  spuMapper.selectAll();
    }

    @Override
    public List<BaseSaleAttr> baseSaleAttrList() {
        return baseSaleAttrMapper.selectAll();
    }

    @Override
    public void saveSpu(SpuInfo spuInfo) {
        // 第一层
        spuMapper.insertSelective(spuInfo);

        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        for (SpuSaleAttr spuSaleAttr:spuSaleAttrList) {
            // 获取插入返回的ID
            spuSaleAttr.setSpuId(spuInfo.getId());

            // 第二层(销售属性)
            spuSaleAttrMapper.insertSelective(spuSaleAttr);

            List<SpuSaleAttrValue> spuSaleAttrValueList =
                    spuSaleAttr.getSpuSaleAttrValueList();

            for (SpuSaleAttrValue spuSaleAttrValue:spuSaleAttrValueList ){
                spuSaleAttrValue.setSpuId(spuInfo.getId());
                //spuSaleAttrValue.setSaleAttrId(spuSaleAttr.getId());
                // 第三层（销售属性值）
                spuSaleAttrValueMapper.insert(spuSaleAttrValue);
            }

        }

        // 保存图片信息
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        for (SpuImage spuImage:spuImageList){
            spuImage.setSpuId(spuInfo.getId());
            spuImageMapper.insert(spuImage);
        }
    }

    @Override
    public List<SpuImage> getSpuImageList(String spuId) {
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);

        List<SpuImage> select = spuImageMapper.select(spuImage);
        return select;
    }

    @Override
    public String getSpuBySku(String skuId) {
        SkuInfo s = new SkuInfo();
        s.setId(skuId);
        SkuInfo skuInfo = skuInfoMapper.selectOne(s);
        return skuInfo.getSpuId();
    }
}
