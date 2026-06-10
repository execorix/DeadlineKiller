package com.bignerdranch.android.deadlinetimer.data.repository

import DeadlineDao
import com.bignerdranch.android.deadlinetimer.data.local.entities.Category
import com.bignerdranch.android.deadlinetimer.data.local.entities.Deadline
import com.bignerdranch.android.deadlinetimer.data.local.entities.SubTask
import com.bignerdranch.android.deadlinetimer.ui.profile.ProfileStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DeadlineRepository(private val deadlineDao: DeadlineDao) {

    val allCategories: Flow<List<Category>> = deadlineDao.getAllCategories()

    suspend fun insertCategory(category: Category) {
        deadlineDao.insertCategory(category)
    }
    val allDeadline: Flow<List<Deadline>> = deadlineDao.getAllDeadlines()

    fun getDeadlineById(id: Int): Flow<Deadline?>{
        return deadlineDao.getDeadlineById(id)
    }

    suspend fun insertDeadline(deadline: Deadline){
        deadlineDao.insertDeadline(deadline)
    }

    suspend fun updateDeadline(deadline: Deadline){
        deadlineDao.updateDeadline(deadline)
    }

    suspend fun deleteDeadline(deadline: Deadline) {
        deadlineDao.deleteDeadline(deadline)
    }

    suspend fun resetIdSequence() {
        deadlineDao.resetIdSequence()
    }
    fun getStatsFlow(): Flow<ProfileStats> {
        return deadlineDao.getAllDeadlines()
            .map { deadlines ->
                val completed = deadlines.count { it.isCompleted }
                val active = deadlines.count { !it.isCompleted }
                val total = completed + active

                val percentage = if (total > 0) {
                    (completed.toFloat() / total.toFloat()) * 100f
                } else {
                    0f
                }
                ProfileStats(
                    completedCount = completed,
                    activeCount = active,
                    completedPercentage = percentage
                )
            }
    }

    suspend fun getDeadlinesCount(): Int {
        return deadlineDao.getDeadlinesCount()
    }

    suspend fun getActiveDeadlinesCount(): Int {
        return deadlineDao.getActiveDeadlinesCount()
    }
    suspend fun deleteCategoryAndResetDeadlines(categoryName: String) {
        deadlineDao.deleteCategoryAndResetDeadlines(categoryName)
    }


    fun getSubTasks(deadlineId: Int): Flow<List<SubTask>> = deadlineDao.getSubTasksForDeadline(deadlineId)
    suspend fun insertSubTask(subTask: SubTask) = deadlineDao.insertSubTask(subTask)
    suspend fun updateSubTask(subTask: SubTask) = deadlineDao.updateSubTask(subTask)
    suspend fun deleteSubTask(subTask: SubTask) = deadlineDao.deleteSubTask(subTask)
}