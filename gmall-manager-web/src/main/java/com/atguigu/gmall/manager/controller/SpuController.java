package com.atguigu.gmall.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.BaseSaleAttr;
import com.atguigu.gmall.bean.SpuInfo;
import com.atguigu.gmall.manager.test.Test1;
import com.atguigu.gmall.service.SpuService;
import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
public class SpuController {
    @Reference
    SpuService spuService;

    //使用注解获取配置文件项
    @Value("${fileName}")
    String fileName;



    @RequestMapping(value = "fileUpload",method= RequestMethod.POST)
    @ResponseBody
    public String fileUpload(@RequestParam("file") MultipartFile file){
        // 获取配置项值
        String path = fileName;

        //上传图片
        //1 fastdfs全局配置ClientGlobal
        String fileStr = Test1.class.getClassLoader().getResource("tracker.properties").getFile();
        try {
            ClientGlobal.init(fileStr);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }

        //2 trackerClient
        TrackerClient trackerClient = new TrackerClient();

        //3 trackerServer
        TrackerServer trackerServer = null;
        try {
            trackerServer = trackerClient.getConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //4 storageClient
        StorageClient storageClient = new StorageClient(trackerServer,null);

        try {
            String originalFilename = file.getOriginalFilename();
            // 开始测试fdfs上传测试
            String[] imgs  = storageClient.upload_file(file.getBytes(), StringUtils.substringAfterLast(originalFilename,"."),null);


            // 返回上传图片路径

            for (int i=0;i<imgs.length;i++){
                path += "/" + imgs[i];
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }


        return path;
    }

    @RequestMapping(value = "saveSpu")
    @ResponseBody
    public String saveSpu(SpuInfo spuInfo){
        //调用保存service的服务
        spuService.saveSpu(spuInfo);

        return "success";
    }

    @RequestMapping(value = "getSpuList")
    @ResponseBody
    public List<SpuInfo> getSpuList(@RequestParam Map<String,String> map){
        String catalog3Id = map.get("catalog3Id");
        List<SpuInfo> spuList = spuService.getSpuList(catalog3Id);

        return spuList;
    }


    @RequestMapping(value = "baseSaleAttrList")
    @ResponseBody
    public List<BaseSaleAttr> baseSaleAttrList(){
        List<BaseSaleAttr> baseSaleAttrList = spuService.baseSaleAttrList();
        return baseSaleAttrList;
    }
}
