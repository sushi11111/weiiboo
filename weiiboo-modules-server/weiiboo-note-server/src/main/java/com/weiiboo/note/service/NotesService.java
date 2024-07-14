package com.weiiboo.note.service;

import com.weiiboo.common.domin.Result;
import com.weiiboo.modules.api.notes.domin.NotesDO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.weiiboo.modules.api.notes.vo.NotePublishVO;
import com.weiiboo.modules.api.notes.vo.NotesPageVO;
import com.weiiboo.modules.api.notes.vo.NotesVO;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
* @author subscriber
* @description 针对表【notes】的数据库操作Service
* @createDate 2024-03-22 14:39:00
*/
public interface NotesService extends IService<NotesDO> {
    Result<?> publishNote(NotePublishVO notePublishVO);
    Result<?> praiseNotes(Long notesId, Long userId, Long targetUserId);
    Result<?> deleteNotes(Long notesId);
    Result<NotesPageVO> getMyNotes(Integer page, Integer pageSize, Integer authority, Integer type);
    Long getBelongUserId(Long notesId);

    Result<NotesPageVO> getLastNotesByPage(Integer page, Integer pageSize);

    Result<NotesPageVO> getNotesByView(Integer page, Integer pageSize, Integer type, Long userId);

    Result<NotesPageVO> getNotesByCategoryId(Integer page, Integer pageSize, Integer categoryId, Integer type, Integer notesType);

    Result<NotesVO> getNotesByNotesId(Long notesId);

    Result<?> viewNotes(Long notesId);

    Result<Map<String, Integer>> getAllNotesCountAndPraiseCountAndCollectCount();

    Result<NotesPageVO> getAttentionUserNotes(Integer page, Integer pageSize);

    Result<?> changeNotesAuthority(Long notesId, Integer authority);

    Result<?> updateNotes(NotePublishVO notesPublishVO);

    Result<?> collectNotes(Long notesId, Long userId, Long targetUserId);

    Result<?> initNotesLike(Long notesId);

    Result<?> initNotesCollect(Long notesId);
}
