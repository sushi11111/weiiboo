package com.weiiboo.es.service;

import com.weiiboo.common.domin.PageParam;
import com.weiiboo.common.domin.Result;
import com.weiiboo.modules.api.notes.domin.NotesDO;
import com.weiiboo.modules.api.notes.vo.NotesPageVO;

import java.util.Map;

public interface NotesSearchService {
    void addNotes(NotesDO notesDO);

    void deleteNotes(Long notesId);

    void updateNotes(NotesDO notesDO);

    Result<NotesPageVO> getNotesByNearBy(PageParam pageParam);

    Result<NotesPageVO> getNotesByKeyword(String keyword, Integer page, Integer pageSize, Integer notesType, Integer hot);

    void updateCount(Map<String, String> map);
}
