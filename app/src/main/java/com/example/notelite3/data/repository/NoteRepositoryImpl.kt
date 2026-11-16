package com.example.notelite3.data.repository

import com.example.notelite3.data.local.Note
import com.example.notelite3.data.local.NoteDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {

    override fun observeNotes(query: String): Flow<List<Note>> {
        val keyword = query.trim().lowercase()
        return if (keyword.isEmpty()) {
            noteDao.observeAll()
        } else {
            noteDao.search(escapeLike(keyword))
        }
    }

    override suspend fun getNote(id: Long): Note? = noteDao.getById(id)

    override suspend fun addNote(title: String, content: String, priority: Int): Long =
        noteDao.insert(Note(title = title, content = content, priority = priority))

    override suspend fun updateNote(note: Note, newTitle: String, newContent: String, newPriority: Int) {
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

    override suspend fun deleteNote(note: Note) = noteDao.delete(note)

    override suspend fun restoreNote(note: Note) {
        noteDao.insert(note)
    }

    private fun escapeLike(s: String) =
        s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")

}