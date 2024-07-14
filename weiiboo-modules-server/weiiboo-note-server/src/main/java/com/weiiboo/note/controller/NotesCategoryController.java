package com.weiiboo.note.controller;

import com.weiiboo.common.domin.Result;
import com.weiiboo.modules.api.notes.domin.NotesCategoryDO;
import com.weiiboo.note.service.NotesCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/notes/catagory")
public class NotesCategoryController {
    @Resource
    private NotesCategoryService notesCategoryService;

    /**
     * 获取所有笔记类别
     * @return 笔记类别列表
     */
    @GetMapping("/getNotesCategoryList")
    @Operation(summary = "获取所有笔记类别")
    public Result<List<NotesCategoryDO>> getNotesCategory() {
        return notesCategoryService.getNotesCategory();
    }
}
