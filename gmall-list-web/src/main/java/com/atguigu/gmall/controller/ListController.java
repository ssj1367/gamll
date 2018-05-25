package com.atguigu.gmall.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.AttrService;
import com.atguigu.gmall.service.ListService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
public class ListController {

    @Reference
    ListService listService;

    @Reference
    AttrService attrService;

    @RequestMapping("list.html")
    public String list(SkuLsParam skuLsParam, ModelMap map){
        // 从elasticsearch中获取数据
        List<SkuLsInfo> skuLsInfoList = listService.search(skuLsParam);
        map.put("skuLsInfoList",skuLsInfoList);

        // 获取平台属性列表和属性值
        List<BaseAttrInfo> attrList = getAttrList(skuLsInfoList,skuLsParam.getValueId());
        map.put("attrList",attrList);

        // 获取当前的请求的URL
        String urlParam = getUrl(skuLsParam,null);
        map.put("urlParam",urlParam);

        String valuename = "";
        String url = "";
        // 制作面包屑
        List<Crumb> crumbs = new ArrayList<Crumb>();
        String[] valueIds = skuLsParam.getValueId();
        if (valueIds != null) {
            for (int i = 0; i <valueIds.length ; i++) {
                String valueId = valueIds[i];
                valuename = attrService.getValueNameById(valueId);

                url = getUrl(skuLsParam,valueId);

                Crumb crumb = new Crumb();
                crumb.setValueName(valuename);
                crumb.setUrlParam(url);

                crumbs.add(crumb);
            }
        }

       /* if(valueIds == null){
            valuename = skuLsParam.getKeyword();
            url = getUrl(skuLsParam,null);

            Crumb crumb = new Crumb();
            if(valuename!=null){
                crumb.setValueName(valuename);
            }else{
                crumb.setValueName("");
            }

            crumb.setUrlParam(url);

            crumbs.add(crumb);
        }*/

        map.put("attrValueSelectedList",crumbs);

        return "list";
    }

    /**
     * 获取当前请求的Url
     * @param skuLsParam
     * @return
     */
    private String getUrl(SkuLsParam skuLsParam,String valueId) {
        String url = "";

        String catalog3Id = skuLsParam.getCatalog3Id();

        String keyword = skuLsParam.getKeyword();

        String[] valueIds = skuLsParam.getValueId();

        if(StringUtils.isNotBlank(catalog3Id)){
            if(StringUtils.isNotBlank(url)){
                url = url+"&";
            }
            url = url + "catalog3Id="+catalog3Id;
        }

        if(StringUtils.isNotBlank(keyword)){
            if(StringUtils.isNotBlank(url)){
                url = url+"&";
            }

            url = url + "keyword="+keyword;
        }

        if(valueIds!=null && valueIds.length>0){
            for (int i = 0; i <valueIds.length ; i++) {
                if(valueId == null){
                    url = url+"&";
                    url = url +"valueId="+valueIds[i];
                }else{
                    if(!valueId.equals(valueIds[i])){
                        url = url+"&";
                        url = url +"valueId="+valueIds[i];
                    }
                }

            }
        }

        return url;
    }

    /**
     * 获取list页面的平台属性的集合
     * @param skuLsInfoList
     * @return
     */
    private List<BaseAttrInfo> getAttrList(List<SkuLsInfo> skuLsInfoList,String[] valueIds){

        // 1.获得去重后的sku列表中的属性值集合,作为查询的条件
        // 使用set无序的，不可重复的集合去重
        Set<String> setValueId  = new HashSet<>();
        for (int i = 0; i <skuLsInfoList.size() ; i++) {
            SkuLsInfo skuLsInfo = skuLsInfoList.get(i);

            List<SkuLsAttrValue> skuAttrValueList = skuLsInfo.getSkuAttrValueList();
            for (SkuLsAttrValue skuLsAttrValue:skuAttrValueList) {
                String valueId = skuLsAttrValue.getValueId();
                // 把valueId一一添加到set集合中，相同会被覆盖掉
                setValueId.add(valueId);
            }
        }
        List<String> list = new ArrayList<>();
        list.addAll(setValueId);

        // 2.通过sku列表的属性值集合查询出显示在页面的属性值集合
        List<BaseAttrInfo> attrList = attrService.getAttrListByValueIds(list);

        // 3.在属性列表中去掉已经选择的属性
        if(valueIds!=null && valueIds.length>0 ){
            // 把数组转化为集合，使用contains方法判断是否存在
            List<String> removeList = new ArrayList<>();
            for (int i = 0; i <valueIds.length ; i++) {
                removeList.add(valueIds[i]);
            }

            // 迭代attrlist(即页面的平台属性)
            Iterator<BaseAttrInfo> iterator = attrList.iterator();
            while(iterator.hasNext()){
                BaseAttrInfo next = iterator.next();
                List<BaseAttrValue> attrValueList = next.getAttrValueList();

                for (BaseAttrValue baseAttrValue:attrValueList) {
                    String valueId = baseAttrValue.getId();
                    if(removeList.contains(valueId)){
                        iterator.remove(); // 移除相同的(即选中valueId)
                    }
                }
            }
        }

        return attrList;
    }
}
