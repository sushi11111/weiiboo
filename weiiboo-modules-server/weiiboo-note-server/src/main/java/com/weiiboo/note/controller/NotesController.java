package com.weiiboo.note.controller;

import com.weiiboo.common.domin.Result;
import com.weiiboo.common.web.exception.BusinessException;
import com.weiiboo.common.myEnum.ExceptionMsgEnum;
import com.weiiboo.common.redis.constant.BloomFilterMap;
import com.weiiboo.common.web.aop.bloomFilter.BloomFilterProcessing;
import com.weiiboo.common.web.aop.idempotent.Idempotent;
import com.weiiboo.modules.api.notes.vo.NotePublishVO;
import com.weiiboo.modules.api.notes.vo.NotesPageVO;
import com.weiiboo.modules.api.notes.vo.NotesVO;
import com.weiiboo.note.service.NotesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/notes")
public class NotesController {
    @Resource
    private NotesService notesService;

    /**
     * 发布笔记
     *
     * @param notePublishVO 发布的笔记信息
     * @return 发布结果
     */
    @PostMapping("/publish")
    @Idempotent(value = "/notes/publish",expireTime = 5000)
    public Result<?>publishNote(@RequestBody NotePublishVO notePublishVO){
        return notesService.publishNote(notePublishVO);
    }

    /**
     * 分页获取最新的笔记
     *
     * @param page 页数
     * @param pageSize 每页数量
     * @return 分页结果
     */
    @GetMapping("/getLastNotesByPage")
    public Result<NotesPageVO> getLastNotesByPage(Integer page, Integer pageSize) {
        if (page == null || pageSize == null) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        return notesService.getLastNotesByPage(page, pageSize);
    }

    /**
     * 分页获取自己发布的笔记
     *
     * @param page 页数
     * @param pageSize 每页数量
     * @param authority 观看权限
     * @param type 笔记类型
     * @return 分页结果
     */
    @GetMapping("/getMyNotes")
    public Result<NotesPageVO>getMyNotes(Integer page,Integer pageSize,Integer authority, Integer type){
        if(page == null || pageSize == null){
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        if(authority == null || authority < 0 || authority > 1){
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        if(type == null || type < 0 || type > 3){
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        if(page < 1){
            page = 1;
        }
        if(pageSize < 1){
            pageSize = 10;
        }
        return notesService.getMyNotes(page, pageSize, authority, type);
    }

    /**
     * 通过主页分页获得笔记
     *
     * @param page 页数
     * @param pageSize 每页数量
     * @param type 笔记类型
     * @return 分页结果
     */
    @GetMapping("/getNotesByView")
    public Result<NotesPageVO> getNotesByView(Integer page, Integer pageSize, Integer type, Long userId) {
        if (page == null || pageSize == null || userId == null) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        if (type == null || type < 0 || type > 1) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        return notesService.getNotesByView(page, pageSize, type, userId);
    }

    /**
     * 根据笔记id获取笔记
     *
     * @param notesId 笔记id
     * @return 笔记信息
     */
    @GetMapping("/getNotesByNotesId")
    @BloomFilterProcessing(map = BloomFilterMap.NOTES_ID_BLOOM_FILTER, keys = {"#notesId"})
    public Result<NotesVO> getNotesByNotesId(Long notesId) {
        if (notesId == null) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        return notesService.getNotesByNotesId(notesId);
    }

    /**
     * 点赞笔记
     *
     * @param notesId 笔记id
     * @param userId  用户id
     * @param targetUserId 目标用户id
     * @return 点赞结果
     */
    @PostMapping("/praieNotes")
    @Idempotent(value = "/notes/praieNotes",expireTime = 500)
    @BloomFilterProcessing(map = BloomFilterMap.NOTES_ID_BLOOM_FILTER,keys = {"#notesId"})
    public Result<?> praiseNotes(Long notesId,Long userId,Long targetUserId){
        return notesService.praiseNotes(notesId,userId,targetUserId);
    }

    /**
     * 收藏笔记
     *
     * @param notesId      笔记id
     * @param userId       用户id
     * @param targetUserId 目标用户id
     * @return 收藏结果
     */
    @PostMapping("/collectNotes")
    @Idempotent(value = "/notes/collectNotes", expireTime = 500)
    @BloomFilterProcessing(map = BloomFilterMap.NOTES_ID_BLOOM_FILTER, keys = {"#notesId"})
    public Result<?> collectNotes(Long notesId, Long userId, Long targetUserId) {
        if (notesId == null || userId == null || targetUserId == null) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        return notesService.collectNotes(notesId, userId, targetUserId);
    }

    /**
     * 查看笔记
     *
     * @param notesId 笔记id
     * @return 笔记内容
     */
    @PostMapping("/viewNotes")
    @BloomFilterProcessing(map = BloomFilterMap.NOTES_ID_BLOOM_FILTER, keys = {"#notesId"})
    public Result<?> viewNotes(Long notesId) {
        if (notesId == null) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        return notesService.viewNotes(notesId);
    }

    /**
     * 获取笔记所属用户
     *
     * @param notesId 笔记id
     * @return 笔记所属用户id
     */
    @GetMapping("/getNotesBelongUser")
    @BloomFilterProcessing(map = BloomFilterMap.NOTES_ID_BLOOM_FILTER, keys = {"#notesId"})
    public Long getNotesBelongUser(@RequestParam("notesId") Long notesId) {
        return notesService.getBelongUserId(notesId);
    }

    /**
     * 删除笔记
     *
     * @param notesId 笔记id
     * @return 删除结果
     */
    @DeleteMapping("/deleteNotes")
    @BloomFilterProcessing(map = BloomFilterMap.NOTES_ID_BLOOM_FILTER, keys = {"#notesId"})
    public Result<?> deleteNotes(@RequestParam("notesId") Long notesId) {
        if (Objects.isNull(notesId)) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        return notesService.deleteNotes(notesId);
    }

    /**
     * 更改笔记公开状态
     *
     * @param notesId   笔记id
     * @param authority 公开状态
     * @return 更改结果
     */
    @PostMapping("/changeNotesAuthority")
    @Idempotent(value = "/notes/changeNotesAuthority", expireTime = 5000)
    @BloomFilterProcessing(map = BloomFilterMap.NOTES_ID_BLOOM_FILTER, keys = {"#notesId"})
    public Result<?> changeNotesAuthority(@RequestParam("notesId") Long notesId, @RequestParam("authority") Integer authority) {
        if (Objects.isNull(notesId) || Objects.isNull(authority)) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        return notesService.changeNotesAuthority(notesId, authority);
    }

    /**
     * 获取笔记总数和点赞总数和收藏总数
     *
     * @return 笔记总数和点赞总数和收藏总数
     */
    @GetMapping("/getAllNotesCountAndPraiseCountAndCollectCount")
    public Result<Map<String, Integer>> getAllNotesCountAndPraiseCountAndCollectCount() {
        return notesService.getAllNotesCountAndPraiseCountAndCollectCount();
    }

    /**
     * 获取关注用户的笔记
     * @param page     页码
     * @param pageSize 每页数量
     * @return 笔记列表
     */
    @GetMapping("/getAttentionUserNotes")
    public Result<NotesPageVO> getAttentionUserNotes(Integer page, Integer pageSize) {
        if (Objects.isNull(page) || Objects.isNull(pageSize)) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        return notesService.getAttentionUserNotes(page, pageSize);
    }

    /**
     * 更新笔记
     *
     * @param notesPublishVO 笔记信息
     * @return 更新结果
     */
    @PutMapping("/updateNotes")
    @Idempotent(value = "/notes/updateNotes", expireTime = 5000)
    @BloomFilterProcessing(map = BloomFilterMap.NOTES_ID_BLOOM_FILTER, keys = {"#notesPublishVO.getNotesId()"})
    public Result<?> updateNotes(@RequestBody NotePublishVO notesPublishVO) {
        if (Objects.isNull(notesPublishVO) || Objects.isNull(notesPublishVO.getNotesId())) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        return notesService.updateNotes(notesPublishVO);
    }

    /**
     * 获取笔记通过分类id
     * @param page       页码
     * @param pageSize   每页数量
     * @param categoryId 分类id
     * @param type       排序类型，0：最新，1：最热
     * @return 笔记列表
     */
    @GetMapping("/getNotesByCategoryId")
    public Result<NotesPageVO> getNotesByCategoryId(Integer page, Integer pageSize, Integer categoryId, Integer type, Integer notesType) {
        if (Objects.isNull(page) || Objects.isNull(pageSize) || Objects.isNull(categoryId) || Objects.isNull(type) || Objects.isNull(notesType)) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        if (type < 0 || type > 1) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        if (notesType < 0 || notesType > 2) {
            notesType=2;
        }
        return notesService.getNotesByCategoryId(page, pageSize, categoryId, type, notesType);
    }
}