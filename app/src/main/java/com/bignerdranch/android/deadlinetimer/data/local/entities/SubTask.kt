package com.bignerdranch.android.deadlinetimer.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
@Entity(
    tableName = "subtasks_table",
    foreignKeys = [
        ForeignKey(
            entity = Deadline::class,
            parentColumns = ["id"],
            childColumns = ["parentDeadlineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["parentDeadlineId"])]
)
data class SubTask(
    @PrimaryKey(autoGenerate = true) val subTaskId: Int = 0,
    val parentDeadlineId: Int,
    val taskText: String,
    val isCompleted: Boolean = false
)