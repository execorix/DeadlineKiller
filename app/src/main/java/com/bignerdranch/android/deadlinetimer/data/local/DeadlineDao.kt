import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bignerdranch.android.deadlinetimer.data.local.entities.Deadline
import com.bignerdranch.android.deadlinetimer.data.local.entities.SubTask
import kotlinx.coroutines.flow.Flow

@Dao
interface DeadlineDao {
    @Query("SELECT * FROM deadlines ORDER BY priority DESC, endDate ASC")
    fun getAllDeadlines(): Flow<List<Deadline>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeadline(deadline: Deadline): Long


    @Query("SELECT COUNT(*) FROM deadlines")
    suspend fun getDeadlinesCount(): Int

    @Query("DELETE FROM sqlite_sequence WHERE name = 'deadlines'")
    suspend fun resetIdSequence()
    @Delete
    suspend fun deleteDeadline(deadline: Deadline)

    @Query("SELECT * FROM deadlines WHERE id = :id")
    fun getDeadlineById(id: Int): Flow<Deadline?>


    @Update
    suspend fun updateDeadline(deadline: Deadline)

    @Query("SELECT * FROM subtasks_table WHERE parentDeadlineId = :deadlineId")
    fun getSubTasksForDeadline(deadlineId: Int): Flow<List<SubTask>>

    @Insert
    suspend fun insertSubTask(subTask: SubTask)

    @Update
    suspend fun updateSubTask(subTask: SubTask)

    @Delete
    suspend fun deleteSubTask(subTask: SubTask)
}