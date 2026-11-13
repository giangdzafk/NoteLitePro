package com.example.notelite3.ui.notes


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notelite3.data.local.Note
import com.example.notelite3.data.repository.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NoteListUiState(
    val notes: List<Note> = emptyList(),
    val isSearching: Boolean = false,   
    val isLoading: Boolean = true,
)

class NoteListViewModel(
    private val repository: NoteRepository,
) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<NoteListUiState> = _query
        .debounce { if (it.isBlank()) 0L else 300L }
        .flatMapLatest { q ->
            repository.observeNotes(q).map { notes ->
                NoteListUiState(notes = notes, isSearching = q.isNotBlank(), isLoading = false)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NoteListUiState(),
        )

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun addNote(title: String, content: String, priority: Int) {
        if (title.isBlank() && content.isBlank()) return
        viewModelScope.launch { repository.addNote(title.trim(), content.trim(), priority) }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch { repository.deleteNote(note) }
    }

    fun restoreNote(note: Note) {
        viewModelScope.launch { repository.restoreNote(note) }
    }
}