package com.weiiboo.note.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.weiiboo.modules.api.notes.domin.UserCollectNotesDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserCollectNotesMapper extends BaseMapper<UserCollectNotesDO> {
}