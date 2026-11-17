package com.example.notelite3.ui.notes

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.notelite3.R
import com.example.notelite3.databinding.FragmentNoteListBinding
// [SỬA HILT] Bỏ import AppViewModelProvider: file đó đã bị xóa khi chuyển sang Hilt -> lỗi "Unresolved reference"
import com.example.notelite3.ui.common.setOnDeleteConfirmedListener
import com.example.notelite3.ui.common.showConfirmDeleteDialog
import com.example.notelite3.ui.edit.NoteEditViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NoteListFragment : Fragment(R.layout.fragment_note_list) {

    private val viewModel: NoteListViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentNoteListBinding.bind(view)

        val adapter = NoteAdapter(
            onClick = { note -> openEditor(note.id) },
            onLongClick = { note -> showConfirmDeleteDialog(REQUEST_DELETE, note.id) },
        )
        binding.rvNotes.adapter = adapter

        setOnDeleteConfirmedListener(REQUEST_DELETE) { noteId ->
            val note = adapter.currentList.firstOrNull { it.id == noteId } ?: return@setOnDeleteConfirmedListener
            viewModel.deleteNote(note)
            Snackbar.make(binding.root, "Đã xóa ghi chú", Snackbar.LENGTH_LONG)
                .setAnchorView(binding.fabAdd)
                .setAction("Hoàn tác") { viewModel.restoreNote(note) }
                .setActionTextColor(requireContext().getColor(R.color.brand_blue))
                .show()
        }

        binding.fabAdd.setOnClickListener {
            if (childFragmentManager.findFragmentByTag(QuickAddBottomSheet.TAG) == null) {
                QuickAddBottomSheet().show(childFragmentManager, QuickAddBottomSheet.TAG)
            }
        }

        setupSearch(binding)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapter.submitList(state.notes)
                    binding.emptyView.isVisible = !state.isLoading && state.notes.isEmpty()
                    binding.txtEmpty.text =
                        if (state.isSearching) "Không tìm thấy ghi chú phù hợp"
                        else "Chưa có ghi chú nào.\nBấm + để tạo ghi chú đầu tiên."
                }
            }
        }
    }

    private fun openEditor(noteId: Long) {
        findNavController().navigate(
            R.id.action_noteList_to_noteEdit,
            Bundle().apply { putLong(NoteEditViewModel.NOTE_ID_ARG, noteId) },
        )
    }
    private fun setupSearch(binding: FragmentNoteListBinding) {
        val searchItem = binding.toolbar.menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = "Tìm ghi chú"

        val currentQuery = viewModel.query.value
        if (currentQuery.isNotEmpty()) {
            searchItem.expandActionView()
            searchView.setQuery(currentQuery, false)
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.onQueryChange(newText.orEmpty())
                return true
            }
        })

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem) = true
            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                viewModel.onQueryChange("")
                return true
            }
        })
    }

    companion object {
        private const val REQUEST_DELETE = "note_list_delete"
    }
}