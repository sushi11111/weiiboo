package com.weiiboo.es.controller;

import com.weiiboo.common.domin.Result;
import com.weiiboo.common.myEnum.ExceptionMsgEnum;
import com.weiiboo.common.web.exception.BusinessException;
import com.weiiboo.es.service.UserSearchService;
import com.weiiboo.modules.api.user.domin.UserEsDO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/search/user")
public class EsUserSearchController {
    @Resource
    private UserSearchService userSearchService;

    /**
     * 根据关键字搜索用户
     *
     * @param keyword 关键字
     * @param page    页码
     * @param pageSize 每页数量
     * @return 用户列表
     */
    @GetMapping("/getUser")
    public Result<List<UserEsDO>> getUser(String keyword, Integer page, Integer pageSize){
        if (page == null || pageSize == null) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        return userSearchService.getUser(keyword, page, pageSize);
    }
}
