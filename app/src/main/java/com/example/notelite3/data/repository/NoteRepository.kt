package com.example.notelite3.data.repository

import com.example.notelite3.data.local.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun observeNotes(query: String): Flow<List<Note>>
    suspend fun getNote(id: Long): Note?
    suspend fun addNote(title: String, content: String, priority: Int): Long
    suspend fun updateNote(note: Note, newTitle: String, newContent: String, newPriority: Int)
    suspend fun deleteNote(note: Note)
    suspend fun restoreNote(note: Note)
}
