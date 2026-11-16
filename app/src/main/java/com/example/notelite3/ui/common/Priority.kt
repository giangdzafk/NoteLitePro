package com.example.notelite3.ui.common

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.notelite3.R
import com.example.notelite3.data.local.Note
import com.example.notelite3.databinding.DialogPriorityBinding
import com.example.notelite3.databinding.ItemPriorityBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object Priority {
    val ALL = Note.HIGHEST_PRIORITY..Note.DEFAULT_PRIORITY

    fun label(priority: Int) = "Ưu tiên $priority"

    @ColorRes
    fun colorRes(priority: Int): Int = when (priority) {
        1 -> R.color.priority_1
        2 -> R.color.priority_2
        3 -> R.color.priority_3
        else -> R.color.priority_4
    }

    fun colorStateList(context: Context, priority: Int): ColorStateList =
        ColorStateList.valueOf(ContextCompat.getColor(context, colorRes(priority)))
}

class PriorityDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = requireArguments()
        val requestKey = requireNotNull(args.getString(ARG_REQUEST_KEY))
        val current = args.getInt(ARG_CURRENT, Note.DEFAULT_PRIORITY)

        val inflater = LayoutInflater.from(requireContext())
        val binding = DialogPriorityBinding.inflate(inflater)

        for (priority in Priority.ALL) {
            val row = ItemPriorityBinding.inflate(inflater, binding.priorityList, true)
            row.imgFlag.imageTintList = Priority.colorStateList(requireContext(), priority)
            row.txtPriority.text = Priority.label(priority)
            row.imgCheck.isInvisible = priority != current
            row.root.isSelected = priority == current
            row.root.setOnClickListener {
                if (priority != current) {
                    parentFragmentManager.setFragmentResult(
                        requestKey,
                        Bundle().apply { putInt(RESULT_PRIORITY, priority) },
                    )
                }
                dismiss()
            }
        }
        binding.btnClose.setOnClickListener { dismiss() }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
            .create()
    }

    companion object {
        const val TAG = "PriorityDialog"
        private const val ARG_REQUEST_KEY = "request_key"
        private const val ARG_CURRENT = "current"
        private const val RESULT_PRIORITY = "priority"

        fun newInstance(requestKey: String, current: Int) = PriorityDialogFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_REQUEST_KEY, requestKey)
                putInt(ARG_CURRENT, current)
            }
        }

        fun priorityFrom(result: Bundle): Int = result.getInt(RESULT_PRIORITY, Note.DEFAULT_PRIORITY)
    }
}

fun Fragment.showPriorityDialog(requestKey: String, current: Int) {
    if (childFragmentManager.findFragmentByTag(PriorityDialogFragment.TAG) != null) return
    PriorityDialogFragment.newInstance(requestKey, current)
        .show(childFragmentManager, PriorityDialogFragment.TAG)
}

fun Fragment.setOnPrioritySelectedListener(requestKey: String, onSelected: (Int) -> Unit) {
    childFragmentManager.setFragmentResultListener(requestKey, viewLifecycleOwner) { _, result ->
        onSelected(PriorityDialogFragment.priorityFrom(result))
    }
}
