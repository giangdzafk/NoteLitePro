package com.example.notelite3.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.notelite3.NoteliteApp
import com.example.notelite3.ui.edit.NoteEditViewModel
import com.example.notelite3.ui.notes.NoteListViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            NoteListViewModel(
                repository = app().container.noteRepository,
            )
        }
        initializer {
            NoteEditViewModel(
                savedStateHandle = createSavedStateHandle(), // chứa noteId từ Navigation
                repository = app().container.noteRepository,
            )
        }
    }
}

private fun CreationExtras.app(): NoteliteApp = this[APPLICATION_KEY] as NoteliteApp
