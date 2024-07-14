package com.weiiboo.note.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.weiiboo.common.Utils.ResultUtil;
import com.weiiboo.common.domin.Result;
import com.weiiboo.modules.api.notes.domin.NotesCategoryDO;
import com.weiiboo.note.mapper.NotesCategorysMapper;
import com.weiiboo.note.service.NotesCategoryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
* @author subscriber
* @description 针对表【categorys】的数据库操作Service实现
* @createDate 2024-03-22 23:16:23
*/
@Service
public class NotesCategoryServiceImpl extends ServiceImpl<NotesCategorysMapper, NotesCategoryDO>
    implements NotesCategoryService {
    @Resource
    private NotesCategorysMapper notesCategorysMapper;

    @Override
    public Result<List<NotesCategoryDO>> getNotesCategory() {
        List<NotesCategoryDO> notesCategoryDOList = notesCategorysMapper.getNotesCategory();
        return ResultUtil.successGet(notesCategoryDOList);
    }
}




