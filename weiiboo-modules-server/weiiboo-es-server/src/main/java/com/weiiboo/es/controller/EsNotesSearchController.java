package com.weiiboo.es.controller;

import com.weiiboo.common.domin.PageParam;
import com.weiiboo.common.domin.Result;
import com.weiiboo.common.myEnum.ExceptionMsgEnum;
import com.weiiboo.common.web.exception.BusinessException;
import com.weiiboo.es.service.NotesSearchService;
import com.weiiboo.modules.api.notes.vo.NotesPageVO;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/search/notes")
public class EsNotesSearchController {
    @Resource
    private NotesSearchService notesSearchService;

    /**
     * 获取附近的笔记
     * @param pageParam 请求参数
     * @return 附近笔记列表
     */
    @PostMapping("/getNotesNearBy")
    public Result<NotesPageVO>getNotesByNearBy(@RequestBody PageParam pageParam){
        if(pageParam.getPage() == null || pageParam.getPageSize() == null){
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        if(pageParam.getLatitude() == null || pageParam.getLongitude() == null){
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        if(pageParam.getPage() < 1){
            pageParam.setPage(1);
        }
        if(pageParam.getPageSize() < 1){
            pageParam.setPageSize(10);
        }
        return notesSearchService.getNotesByNearBy(pageParam);
    }

    /**
     * 根据关键字获取笔记列表
     * @param keyword 关键字
     * @param page    页码
     * @param pageSize 每页数量
     * @param notesType 笔记类型
     * @param hot       0：最新，1：最热，2：全部
     * @return 分页笔记列表
     */
    @RequestMapping("/getNotesByKeyword")
    public Result<NotesPageVO> getNotesByKeyword(String keyword, Integer page,
                                                 Integer pageSize,Integer notesType,Integer hot){
        if(page == null || pageSize == null || !StringUtils.hasText(keyword)){
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        // 0：最新，1：最热，2：全部
        if (hot == null||hot < 0||hot > 2) {
            hot = 2;
        }
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        return notesSearchService.getNotesByKeyword(keyword,page,pageSize,notesType,hot);
    }
}
