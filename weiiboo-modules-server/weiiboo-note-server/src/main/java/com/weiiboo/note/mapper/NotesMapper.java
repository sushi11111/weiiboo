package com.weiiboo.note.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.weiiboo.modules.api.notes.domin.NotesDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author subscriber
* @description 针对表【notes】的数据库操作Mapper
* @createDate 2024-03-22 14:39:00
* @Entity generator.domain.Notes
*/
@Mapper
public interface NotesMapper extends BaseMapper<NotesDO> {

    List<NotesDO> selectPageByTime(Integer offset, Integer pageSize);

    List<NotesDO> selectPageByUserId(Integer offset, Integer pageSize, Long userId, int i);

    List<NotesDO> selectPageByCategoryIdOrderByPraise(Integer offset, Integer pageSize, Integer categoryId, Integer notesType);

    List<NotesDO> selectPageByCategoryIdByUpdateTime(Integer offset, Integer pageSize, Integer categoryId, Integer notesType);

    Integer getPraiseCountByUserId(Long userId);

    Integer getCollectCountByUserId(Long userId);

    List<NotesDO> selectPageByAttentionUserId(Integer offset, Integer pageSize, List<Long> attentionUserId);
}




