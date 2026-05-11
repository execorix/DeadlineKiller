import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DeadlineDao {
    @Query("SELECT * FROM deadlines ORDER BY priority DESC, endDate ASC")
    fun getAllDeadlines(): Flow<List<Deadline>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeadline(deadline: Deadline): Long

    @Delete
    suspend fun deleteDeadline(deadline: Deadline)

    @Query("SELECT * FROM sub_tasks WHERE parentDeadlineId = :deadlineId")
    fun getSubTasks(deadlineId: Int): Flow<List<SubTask>>
}