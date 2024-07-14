package com.weiiboo.note.mapper;

import com.weiiboo.modules.api.notes.domin.NotesCategoryDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author subscriber
* @description 针对表【categorys】的数据库操作Mapper
* @createDate 2024-03-22 23:16:23
* @Entity generator.domain.Categorys
*/
@Mapper
public interface NotesCategorysMapper extends BaseMapper<NotesCategoryDO> {
    @Select("select * from categorys order by category_sort")
    List<NotesCategoryDO> getNotesCategory();
}




