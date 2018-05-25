package com.atguigu.gmall.manager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.SkuAttrValue;
import com.atguigu.gmall.bean.SkuImage;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuSaleAttrValue;
import com.atguigu.gmall.manager.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.manager.mapper.SkuImageMapper;
import com.atguigu.gmall.manager.mapper.SkuInfoMapper;
import com.atguigu.gmall.manager.mapper.SkuSaleAttrValueMapper;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class SkuServiceImpl implements SkuService {
    @Autowired
    SkuInfoMapper skuInfoMapper;

    @Autowired
    SkuImageMapper skuImageMapper;

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    RedisUtil redisUtil; //注入redis工具类

    @Override
    public List<SkuInfo> skuInfoListBySpu(String spuId) {
        List<SkuInfo> list = skuInfoMapper.selectSkuInfoListBySpu(Integer.parseInt(spuId));
        return list;
    }

    @Override
    public void saveSku(SkuInfo skuInfo) {

        // 根据id判断修改或者插入
        if (skuInfo.getId() == null || skuInfo.getId().length() == 0) {
            skuInfoMapper.insertSelective(skuInfo);
        } else {
            // 第一层
            skuInfoMapper.updateByPrimaryKeySelective(skuInfo);
        }

        // 先删除图片
        SkuImage s = new SkuImage();
        s.setSkuId(skuInfo.getId());
        skuImageMapper.delete(s);

        // 封装图片
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        for (SkuImage skuImage : skuImageList) {
            skuImage.setSkuId(skuInfo.getId());
            skuImage.setId(null); // 把id设置为空
            skuImageMapper.insertSelective(skuImage);
        }


        // 先删除平台属性
        Example skuAttrValueExample = new Example(SkuAttrValue.class);
        skuAttrValueExample.createCriteria().andEqualTo("skuId", skuInfo.getId());
        skuAttrValueMapper.deleteByExample(skuAttrValueExample);

        // 封装平台属性
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for (SkuAttrValue skuAttrValue : skuAttrValueList) {
            skuAttrValue.setSkuId(skuInfo.getId());
            skuAttrValue.setId(null);
            skuAttrValueMapper.insertSelective(skuAttrValue);
        }

        // 删除销售属性
        Example skuSaleAttrValueExample = new Example(SkuSaleAttrValue.class);
        skuSaleAttrValueExample.createCriteria().andEqualTo("skuId", skuInfo.getId());
        skuSaleAttrValueMapper.deleteByExample(skuSaleAttrValueExample);

        // 封装销售属性
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
            skuSaleAttrValue.setSkuId(skuInfo.getId());
            skuSaleAttrValue.setId(null);
            skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
        }

    }

    @Override
    public SkuInfo getItem(String skuId) {
        Jedis jedis = null;
        SkuInfo skuInfo = null;

        try {
            // 添加Redis缓存
            jedis = redisUtil.getJedis();

            String s1 = jedis.get("sku:" + skuId + ":info");

            if ("empty".equals(s1)) {
                return null;
            } else if (StringUtils.isBlank(s1) || "null".equals(s1)) {
                // 缓存失败则从数据库查找
                if (skuInfo == null) {
                    // 查询mysql,先设置分布式锁,是数据库的并发访问排队解决
                    // SkuConst
                    String ok = jedis.set("sku:" + skuId + ":lock", "1", "nx", "px", 10000);

                    // 拿到锁，访问数据库
                    if ("OK".equals(ok)) {
                        skuInfo = getSkuByIdFromDb(skuId);
                        //如果没有拿到数据
                        if (skuInfo == null) {
                            // 返回一个信息给其他人,获取到empty就不要访问数据库了
                            jedis.setex("sku:" + skuId + ":info", 60000, "empty");
                        } else {
                            //set到redis缓存中
                            jedis.set("sku:" + skuId + ":info", JSON.toJSONString(skuInfo));

                            // 交还分布式锁
                            jedis.del("sku:" + skuId + ":lock");
                        }
                    }
                }
            }else{
                // 把字符串转化为json对象
                skuInfo = JSON.parseObject(s1, SkuInfo.class);
            }
        } catch (Exception e) {

        }

        // 关闭redis连接
        try {
            jedis.close();
        } catch (Exception e) {

        }

        return skuInfo;
    }

    @Override
    public List<SkuInfo> skuAttrValueListBySpu(String spuId) {
        List<SkuInfo> list = skuSaleAttrValueMapper.selectSkuAttrValueListBySpu(Integer.parseInt(spuId));
        return list;
    }

    private SkuInfo getSkuByIdFromDb(String skuId) {
        // 查找库存商品
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo = skuInfoMapper.selectOne(skuInfo);

        if (skuInfo == null) {
            return null;
        } else {

            // 查找skuImg
            SkuImage img = new SkuImage();
            img.setSkuId(skuId);
            List<SkuImage> skuImglist = skuImageMapper.select(img);
            // 封装
            skuInfo.setSkuImageList(skuImglist);
        }
        return skuInfo;
    }

}
