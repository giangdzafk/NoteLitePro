package com.example.notelite3.ui.notes


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.notelite3.data.local.Note
import com.example.notelite3.databinding.ItemNoteBinding
import com.example.notelite3.ui.common.Priority
import com.example.notelite3.ui.util.formatTime


class NoteAdapter(
    private val onClick: (Note) -> Unit,
    private val onLongClick: (Note) -> Unit,
) : ListAdapter<Note, NoteAdapter.NoteViewHolder>(DIFF) {

    inner class NoteViewHolder(
        private val binding: ItemNoteBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note) {
            binding.txtTitle.text = note.title.ifBlank { "(Không có tiêu đề)" }
            binding.txtContent.text = note.content.trim()
            binding.txtContent.isVisible = note.content.isNotBlank()
            binding.txtTime.text = formatTime(note.updateAt)
            binding.icHasContent.isVisible = note.content.isNotBlank()
            binding.imgPriority.imageTintList =
                Priority.colorStateList(binding.root.context, note.priority)
            binding.imgPriority.contentDescription = Priority.label(note.priority)

            binding.root.setOnClickListener { onClick(note) }
            binding.root.setOnLongClickListener {
                onLongClick(note)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder =
        NoteViewHolder(ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Note>() {
            override fun areItemsTheSame(oldItem: Note, newItem: Note) = oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Note, newItem: Note) = oldItem == newItem
        }
    }
}