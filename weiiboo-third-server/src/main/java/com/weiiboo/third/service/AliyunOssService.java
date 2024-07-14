package com.weiiboo.third.service;


import com.weiiboo.common.domin.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AliyunOssService {
    Result<String> uploadImg(MultipartFile file);

    Result<List<String>> uploadImgs(MultipartFile[] file);

    Result<String> uploadAudio(MultipartFile file);

    Result<String> uploadVideo(MultipartFile file);
}