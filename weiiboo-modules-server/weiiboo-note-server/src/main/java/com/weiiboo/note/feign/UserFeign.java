package com.weiiboo.note.feign;

import com.weiiboo.common.domin.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("weiiboo-user-server")
public interface UserFeign {
    @GetMapping("/user/getUserInfo")
    Result<?> getUserInfo(@RequestParam("userId") Long userId);

    @GetMapping("/user/relation/isAttention")
    Result<Boolean> selectOneByUserIdAndAttentionIdIsExist(@RequestParam("toId") Long toId, @RequestParam("fromId") Long fromId);

    @GetMapping("/user/relation/getAttentionUserId")
    Result<List<Long>> getAttentionUserId(@RequestParam("userId") Long userId);
}
