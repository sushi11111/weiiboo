package com.weiiboo.job.service;

public interface NotesService {
    void updateNotesLikeNum(String key,Long notesId, Integer notesLikeNum);

    void updateNotesCollectionNum(String key, Long notesId, Integer notesCollectionNum);

    void updateNotesViewNum(String key, Long notesId, Integer notesViewNum);
}
