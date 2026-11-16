package com.example.notelite3.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val createAt: Long = System.currentTimeMillis(),
    val updateAt: Long = System.currentTimeMillis(),
    val searchText: String = "$title $content".lowercase(),

    @ColumnInfo(defaultValue = "4")
    val priority: Int = DEFAULT_PRIORITY,
) {
    companion object {
        const val HIGHEST_PRIORITY = 1
        const val DEFAULT_PRIORITY = 4
    }
}
