package com.example.notelite3.ui.common

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.notelite3.R
import com.example.notelite3.databinding.DialogConfirmDeleteBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Hộp thoại xác nhận xóa: tiêu đề, đường kẻ, lời nhắn, 2 nút bo tròn.
 *
 * Là DialogFragment (không phải AlertDialog thường) nên sống sót khi xoay màn hình,
 * không bị rò rỉ cửa sổ, và kết quả trả về qua Fragment Result API.
 */
class ConfirmDeleteDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = requireArguments()
        val requestKey = requireNotNull(args.getString(ARG_REQUEST_KEY))
        val noteId = args.getLong(ARG_NOTE_ID)

        // Không dùng `layoutInflater` của Fragment ở đây: nó gọi lại onCreateDialog -> đệ quy
        val binding = DialogConfirmDeleteBinding.inflate(LayoutInflater.from(requireContext()))
        binding.btnKeep.setOnClickListener { dismiss() }
        binding.btnDelete.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                requestKey,
                Bundle().apply { putLong(RESULT_NOTE_ID, noteId) },
            )
            dismiss()
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
            .create()
    }

    companion object {
        const val TAG = "ConfirmDeleteDialog"
        private const val ARG_REQUEST_KEY = "request_key"
        private const val ARG_NOTE_ID = "note_id"
        private const val RESULT_NOTE_ID = "note_id"

        fun newInstance(requestKey: String, noteId: Long) = ConfirmDeleteDialogFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_REQUEST_KEY, requestKey)
                putLong(ARG_NOTE_ID, noteId)
            }
        }

        fun noteIdFrom(result: Bundle): Long = result.getLong(RESULT_NOTE_ID)
    }
}

/** Mở hộp thoại xóa. Kết quả nhận ở [setOnDeleteConfirmedListener] với cùng [requestKey]. */
fun Fragment.showConfirmDeleteDialog(requestKey: String, noteId: Long = 0L) {
    if (childFragmentManager.findFragmentByTag(ConfirmDeleteDialogFragment.TAG) != null) return
    ConfirmDeleteDialogFragment.newInstance(requestKey, noteId)
        .show(childFragmentManager, ConfirmDeleteDialogFragment.TAG)
}

/** Gọi trong onViewCreated. Chỉ nhận kết quả khi view còn sống (gắn với viewLifecycleOwner). */
fun Fragment.setOnDeleteConfirmedListener(requestKey: String, onConfirmed: (noteId: Long) -> Unit) {
    childFragmentManager.setFragmentResultListener(requestKey, viewLifecycleOwner) { _, result ->
        onConfirmed(ConfirmDeleteDialogFragment.noteIdFrom(result))
    }
}
