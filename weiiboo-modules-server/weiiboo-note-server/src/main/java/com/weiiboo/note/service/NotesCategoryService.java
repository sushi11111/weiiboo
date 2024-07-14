package com.weiiboo.note.service;

import com.weiiboo.common.domin.Result;
import com.weiiboo.modules.api.notes.domin.NotesCategoryDO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author subscriber
* @description 针对表【categorys】的数据库操作Service
* @createDate 2024-03-22 23:16:23
*/
public interface NotesCategoryService extends IService<NotesCategoryDO> {

    Result<List<NotesCategoryDO>> getNotesCategory();
}
