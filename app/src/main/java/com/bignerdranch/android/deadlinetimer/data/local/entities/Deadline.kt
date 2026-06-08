package com.bignerdranch.android.deadlinetimer.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deadlines")



data class Deadline(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String?,
    val startDate: Long,
    val endDate: Long,
    val priority: Int,
    val isCompleted: Boolean = false,
    val isExtended: Boolean = false
)
