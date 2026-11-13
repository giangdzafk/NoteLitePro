package com.example.notelite3.ui.notes

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.example.notelite3.R
import com.example.notelite3.data.local.Note
import com.example.notelite3.databinding.BottomSheetQuickAddBinding
import com.example.notelite3.ui.AppViewModelProvider
import com.example.notelite3.ui.common.Priority
import com.example.notelite3.ui.common.showPriorityDialog
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class QuickAddBottomSheet : BottomSheetDialogFragment() {

    // Dùng chung ViewModel với màn danh sách (fragment cha)
    private val viewModel: NoteListViewModel by viewModels(
        ownerProducer = { requireParentFragment() },
        factoryProducer = { AppViewModelProvider.Factory },
    )

    private var _binding: BottomSheetQuickAddBinding? = null
    private val binding get() = _binding!!

    /** null = người dùng chưa chọn -> lưu với mức mặc định. */
    private var selectedPriority: Int? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetQuickAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Giữ lựa chọn ưu tiên khi xoay màn hình
        selectedPriority = savedInstanceState
            ?.getInt(KEY_PRIORITY, NO_PRIORITY)
            ?.takeIf { it != NO_PRIORITY }

        binding.edtTitle.doAfterTextChanged { updateSaveButton() }
        binding.edtContent.doAfterTextChanged { updateSaveButton() }
        updateSaveButton()
        renderPriorityChip()

        if (binding.edtContent.text.isNotEmpty()) showContentField(focus = false)
        binding.chipContent.setOnClickListener { showContentField(focus = true) }

        binding.chipPriority.setOnClickListener {
            showPriorityDialog(selectedPriority ?: Note.DEFAULT_PRIORITY) { priority ->
                selectedPriority = priority
                renderPriorityChip()
            }
        }

        binding.btnSave.setOnClickListener { save() }
        binding.edtTitle.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                save()
                true
            } else false
        }

        binding.edtTitle.requestFocus()
    }

    private fun renderPriorityChip() {
        val chip = binding.chipPriority
        val priority = selectedPriority
        if (priority == null) {
            chip.text = "Ưu tiên"
            chip.setChipIconTintResource(R.color.text_primary)
        } else {
            chip.text = Priority.label(priority)
            chip.chipIconTint = Priority.colorStateList(requireContext(), priority)
        }
    }

    private fun showContentField(focus: Boolean) {
        binding.edtContent.isVisible = true
        binding.chipContent.isVisible = false
        if (focus) binding.edtContent.requestFocus()
    }

    private fun hasText() =
        binding.edtTitle.text.isNotBlank() || binding.edtContent.text.isNotBlank()

    private fun updateSaveButton() {
        val enabled = hasText()
        binding.btnSave.isEnabled = enabled
        binding.btnSave.alpha = if (enabled) 1f else 0.4f
    }

    private fun save() {
        if (!hasText()) return
        viewModel.addNote(
            title = binding.edtTitle.text.toString(),
            content = binding.edtContent.text.toString(),
            priority = selectedPriority ?: Note.DEFAULT_PRIORITY,
        )
        dismiss()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_PRIORITY, selectedPriority ?: NO_PRIORITY)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "QuickAddBottomSheet"
        private const val KEY_PRIORITY = "priority"
        private const val NO_PRIORITY = 0
    }
}
