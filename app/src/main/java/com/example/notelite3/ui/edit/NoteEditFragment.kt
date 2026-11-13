package com.example.notelite3.ui.edit

import com.example.notelite3.ui.AppViewModelProvider
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.notelite3.R
import com.example.notelite3.databinding.FragmentNoteEditBinding
import com.example.notelite3.ui.common.Priority
import com.example.notelite3.ui.common.showConfirmDeleteDialog
import com.example.notelite3.ui.common.showPriorityDialog
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class NoteEditFragment : Fragment(R.layout.fragment_note_edit) {

    private val viewModel: NoteEditViewModel by viewModels { AppViewModelProvider.Factory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentNoteEditBinding.bind(view)

        // ① UI -> ViewModel: người dùng gõ thì báo cho ViewModel
        binding.edtTitle.doAfterTextChanged { viewModel.onTitleChange(it?.toString().orEmpty()) }
        binding.edtContent.doAfterTextChanged { viewModel.onContentChange(it?.toString().orEmpty()) }

        // ② Hai đường thoát đều tự lưu (spec mục 8)
        binding.toolbar.setNavigationOnClickListener { viewModel.saveAndExit() }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            viewModel.saveAndExit()
        }

        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_priority -> {
                    showPriorityDialog(viewModel.uiState.value.priority) { viewModel.onPriorityChange(it) }
                    true
                }
                R.id.action_delete -> {
                    showConfirmDeleteDialog { viewModel.deleteNote() }
                    true
                }
                else -> false
            }
        }

        // ③ ViewModel -> UI: chỉ thu thập khi màn hình đang hiển thị (STARTED)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect { render(binding, it) } }
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            NoteEditEvent.Exit -> findNavController().popBackStack()
                            is NoteEditEvent.ShowMessage ->
                                Snackbar.make(binding.root, event.message, Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun render(binding: FragmentNoteEditBinding, state: NoteEditUiState) {
        binding.toolbar.title = if (state.isNewNote) "Ghi chú mới" else "Sửa ghi chú"
        binding.toolbar.menu.findItem(R.id.action_delete).isVisible = !state.isNewNote
        binding.toolbar.menu.findItem(R.id.action_priority).apply {
            isEnabled = !state.isLoading
            iconTintList = Priority.colorStateList(requireContext(), state.priority)
            title = "Mức ưu tiên: ${Priority.label(state.priority)}"
        }

        binding.edtTitle.isEnabled = !state.isLoading
        binding.edtContent.isEnabled = !state.isLoading

        // Chỉ setText khi KHÁC nội dung đang hiển thị (giải thích bên dưới)
        if (binding.edtTitle.text.toString() != state.title) binding.edtTitle.setText(state.title)
        if (binding.edtContent.text.toString() != state.content) binding.edtContent.setText(state.content)
    }
}