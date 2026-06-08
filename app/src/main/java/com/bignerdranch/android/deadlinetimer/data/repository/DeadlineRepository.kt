package com.bignerdranch.android.deadlinetimer.data.repository

import DeadlineDao
import com.bignerdranch.android.deadlinetimer.data.local.entities.Deadline
import com.bignerdranch.android.deadlinetimer.data.local.entities.SubTask
import kotlinx.coroutines.flow.Flow

class DeadlineRepository(private val deadlineDao: DeadlineDao) {


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

    suspend fun getDeadlinesCount(): Int {
        return deadlineDao.getDeadlinesCount()
    }

    fun getSubTasks(deadlineId: Int): Flow<List<SubTask>> = deadlineDao.getSubTasksForDeadline(deadlineId)
    suspend fun insertSubTask(subTask: SubTask) = deadlineDao.insertSubTask(subTask)
    suspend fun updateSubTask(subTask: SubTask) = deadlineDao.updateSubTask(subTask)
    suspend fun deleteSubTask(subTask: SubTask) = deadlineDao.deleteSubTask(subTask)
}