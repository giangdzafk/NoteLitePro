package com.example.notelite3.ui.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notelite3.data.local.Note
import com.example.notelite3.data.repository.NoteRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NoteEditUiState(
    val title: String = "",
    val content: String = "",
    val priority: Int = Note.DEFAULT_PRIORITY,
    val isNewNote: Boolean = true,
    val isLoading: Boolean = false,
)

sealed interface NoteEditEvent {
    data object Exit : NoteEditEvent
    data class ShowMessage(val message: String) : NoteEditEvent
}

class NoteEditViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: NoteRepository,
) : ViewModel() {

    private val noteId: Long = savedStateHandle[NOTE_ID_ARG] ?: NEW_NOTE_ID
    private val isNewNote = noteId == NEW_NOTE_ID
    private var originalNote: Note? = null
    private var isSaving = false
    private val _uiState = MutableStateFlow(
        NoteEditUiState(
            title = savedStateHandle[KEY_TITLE] ?: "",
            content = savedStateHandle[KEY_CONTENT] ?: "",
            priority = savedStateHandle[KEY_PRIORITY] ?: Note.DEFAULT_PRIORITY,
            isNewNote = isNewNote,
            isLoading = !isNewNote,
        )
    )
    val uiState: StateFlow<NoteEditUiState> = _uiState.asStateFlow()

    private val _events = Channel<NoteEditEvent>(Channel.BUFFERED)
    val events: Flow<NoteEditEvent> = _events.receiveAsFlow()

    init {
        if (!isNewNote) {
            viewModelScope.launch {
                val note = repository.getNote(noteId)
                if (note == null) {
                    _events.send(NoteEditEvent.ShowMessage("Không tìm thấy ghi chú"))
                    _events.send(NoteEditEvent.Exit)
                } else {
                    originalNote = note
                    _uiState.update {
                        it.copy(
                            title = savedStateHandle[KEY_TITLE] ?: note.title,
                            content = savedStateHandle[KEY_CONTENT] ?: note.content,
                            priority = savedStateHandle[KEY_PRIORITY] ?: note.priority,
                            isLoading = false,
                        )
                    }
                }
            }
        }
    }

    fun onTitleChange(title: String) {
        if (_uiState.value.isLoading || title == _uiState.value.title) return
        savedStateHandle[KEY_TITLE] = title
        _uiState.update { it.copy(title = title) }
    }

    fun onContentChange(content: String) {
        if (_uiState.value.isLoading || content == _uiState.value.content) return
        savedStateHandle[KEY_CONTENT] = content
        _uiState.update { it.copy(content = content) }
    }

    fun onPriorityChange(priority: Int) {
        if (_uiState.value.isLoading) return
        savedStateHandle[KEY_PRIORITY] = priority
        _uiState.update { it.copy(priority = priority) }
    }

    fun saveAndExit() {
        if (isSaving) return
        isSaving = true
        viewModelScope.launch {
            val state = _uiState.value
            val original = originalNote
            val title = state.title.trim()
            val content = state.content.trim()
            when {
                state.isLoading -> Unit
                original == null -> {
                    if (title.isNotEmpty() || content.isNotEmpty()) {
                        repository.addNote(title, content, state.priority)
                    }
                }
                original.title != title ||
                    original.content != content ||
                    original.priority != state.priority ->
                    repository.updateNote(original, title, content, state.priority)
            }
            _events.send(NoteEditEvent.Exit)
        }
    }

    fun deleteNote() {
        val note = originalNote ?: return
        if (isSaving) return
        isSaving = true
        viewModelScope.launch {
            repository.deleteNote(note)
            _events.send(NoteEditEvent.Exit)
        }
    }

    companion object {
        const val NOTE_ID_ARG = "noteId"
        const val NEW_NOTE_ID = -1L
        private const val KEY_TITLE = "draft_title"
        private const val KEY_CONTENT = "draft_content"
        private const val KEY_PRIORITY = "draft_priority"
    }
}
