package com.example.notelite3.di

import android.content.Context
import com.example.notelite3.data.local.NoteDatabase
import com.example.notelite3.data.repository.NoteRepository

class AppContainer(context: Context) {
    private val database = NoteDatabase.getInstance(context)
    val noteRepository : NoteRepository by lazy { NoteRepository (database.noteDao()) }
}