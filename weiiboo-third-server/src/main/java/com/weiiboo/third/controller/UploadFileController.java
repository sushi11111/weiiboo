package com.weiiboo.third.controller;


import com.weiiboo.common.domin.Result;
import com.weiiboo.third.service.AliyunOssService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/third")
public class UploadFileController {
    @Resource
    private AliyunOssService aliyunOssService;

    /**
     * 上传单张图片
     * @param file 图片文件
     * @return 上传结果
     */
    @PostMapping("/uploadImg")
    public Result<String> uploadImg(@RequestParam("file") MultipartFile file) {
        return aliyunOssService.uploadImg(file);
    }

    /**
     * 上传多张图片
     * @param file 图片文件列表
     * @return 上传结果
     */
    @PostMapping("/uploadImgs")
    public Result<List<String>> uploadImgs(@RequestParam("file") MultipartFile[] file) {
        return aliyunOssService.uploadImgs(file);
    }

    /**
     * 上传音频
     * @param file 音频文件
     * @return 上传结果
     */
    @PostMapping("/uploadAudio")
    public Result<String> uploadAudio(@RequestParam("file") MultipartFile file) {
        return aliyunOssService.uploadAudio(file);
    }

    /**
     * 上传视频
     * @param file 视频文件
     * @return 上传结果
     */
    @PostMapping("/uploadVideo")
    public Result<String> uploadVideo(@RequestParam("file") MultipartFile file) {
        return aliyunOssService.uploadVideo(file);
    }
}