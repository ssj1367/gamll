package com.atguigu.gmall.manager.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.manager.mapper.*;
import com.atguigu.gmall.service.AttrService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AttrServiceImpl implements AttrService {

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    Catalog1Mapper catalog1Mapper;

    @Autowired
    Catalog2Mapper catalog2Mapper;

    @Autowired
    Catalog3Mapper catalog3Mapper;

    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    SkuInfoMapper  skuInfoMapper;

    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
        BaseAttrInfo b = new BaseAttrInfo();
        b.setCatalog3Id(catalog3Id);
        List<BaseAttrInfo> select = baseAttrInfoMapper.select(b);
        return select;
    }

    @Override
    public List<BaseCatalog1> getCatalog1() {
        return catalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        BaseCatalog2 b = new BaseCatalog2();
        b.setCatalog1Id(catalog1Id);
        return catalog2Mapper.select(b);
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 b = new BaseCatalog3();
        b.setCatalog2Id(catalog2Id);
        return catalog3Mapper.select(b);
    }

    @Override
    public void saveAttr(BaseAttrInfo baseAttrInfo) {
        String id =  baseAttrInfo.getId();

        if(StringUtils.isBlank(id)){
            // 如果为空添加
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }else{
            baseAttrInfoMapper.updateByPrimaryKey(baseAttrInfo);
        }

        // 删掉 所有关联的属性值
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValue);


        // 重新增加最新的属性值内容
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();

        for (int i = 0; i < attrValueList.size(); i++) {
            BaseAttrValue b = attrValueList.get(i);
            b.setAttrId(baseAttrInfo.getId());
            baseAttrValueMapper.insert(b);
        }
    }

    @Override
    public List<BaseAttrInfo> attrInfoList(String catalog3Id) {
        List<BaseAttrInfo> list = baseAttrInfoMapper.selectAttrInfoList(Integer.parseInt(catalog3Id));
        return list;
    }

    @Override
    public List<SpuSaleAttr> saleAttrValueList(String spuId) {
        // 查询 saleAttr
        SpuSaleAttr s = new SpuSaleAttr();
        s.setSpuId(spuId);
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.select(s);

        // 查询saleAttrValue
        for (SpuSaleAttr spuSaleAttr :spuSaleAttrList) {
            SpuSaleAttrValue sv = new SpuSaleAttrValue();
            sv.setSaleAttrId(spuSaleAttr.getSaleAttrId());  //saleAttr的ID
            sv.setSpuId(spuId); //spuId

            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttrValueMapper.select(sv);

            // 把查询集合封装到spuSaleAttr中
            spuSaleAttr.setSpuSaleAttrValueList(spuSaleAttrValueList);
        }
        return spuSaleAttrList;
    }

    @Override
    public List<SpuSaleAttr> spuSaleAttrListCheckBySku(String skuId) {
        //需要传入skuId和spuId
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        SkuInfo skuInfo1 = skuInfoMapper.selectOne(skuInfo);

        Map<String,Object> map = new HashMap<String,Object>();
        map.put("skuId",skuId);
        map.put("spuId",skuInfo1.getSpuId());
        List<SpuSaleAttr> listSaleAttr = spuSaleAttrValueMapper.selectSpuSaleAttrListCheckBySku(map);
        return listSaleAttr;
    }

    @Override
    public List<BaseAttrInfo> getAttrListByValueIds(List<String> list) {
        // 把集合转化为字符串,并且以','分割
        String join = StringUtils.join(list.toArray(), ",");

        List<BaseAttrInfo> attrList = baseAttrInfoMapper.selectAttrListByValueIds(join);

        return attrList;
    }

    @Override
    public String getValueNameById(String valueId) {
        String valueName = baseAttrValueMapper.selectValueNameById(valueId);
        return valueName;
    }
}
