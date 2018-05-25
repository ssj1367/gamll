package com.atguigu.gmall.manager.test;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Test1 {

    @Test
    public void test1() throws IOException, MyException {
        //1 fastdfs全局配置ClientGlobal
        String file = Test1.class.getClassLoader().getResource("tracker.properties").getFile();
        ClientGlobal.init(file);

        //2 trackerClient
        TrackerClient trackerClient = new TrackerClient();

        //3 trackerServer
        TrackerServer trackerServer = trackerClient.getConnection();

        //4 storageClient
        StorageClient storageClient = new StorageClient(trackerServer,null);

        // 开始测试fdfs上传测试
        String path = "G://test.jpg";
        String[] jpgs = storageClient.upload_file(path, "jpg",null);

        // 返回上传图片路径
        String fileName = "http://192.168.4.24";
        for (int i=0;i<jpgs.length;i++){
            fileName += "/" + jpgs[i];
        }

        System.out.print(fileName);

    }
}
