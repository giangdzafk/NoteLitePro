package com.example.notelite3.data.repository

import com.example.notelite3.data.local.Note
import com.example.notelite3.data.local.NoteDao
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {

    fun observeNotes(query: String): Flow<List<Note>> {
        val keyword = query.trim().lowercase()
        return if (keyword.isEmpty()) {
            noteDao.observeAll()
        } else {
            noteDao.search(escapeLike(keyword))
        }
    }

    suspend fun getNote(id: Long): Note? = noteDao.getById(id)

    suspend fun addNote(title: String, content: String, priority: Int): Long =
        noteDao.insert(Note(title = title, content = content, priority = priority))

    suspend fun updateNote(note: Note, newTitle: String, newContent: String, newPriority: Int) {
        noteDao.update(
            note.copy(
                title = newTitle,
                content = newContent,
                priority = newPriority,
                updateAt = System.currentTimeMillis(),
                searchText = "$newTitle $newContent".lowercase(),
            )
        )
    }

    suspend fun deleteNote(note: Note) = noteDao.delete(note)

    /** Khôi phục ghi chú vừa xóa (giữ nguyên id, thời gian và mức ưu tiên). */
    suspend fun restoreNote(note: Note) {
        noteDao.insert(note)
    }

    /** Để tìm "50%" hay "a_b" không bị hiểu thành ký tự đại diện của LIKE. */
    private fun escapeLike(s: String) =
        s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
}
