import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.bignerdranch.android.deadlinetimer.data.local.entities.Category
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

    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT COUNT(*) FROM deadlines WHERE isCompleted = 0")
    suspend fun getActiveDeadlinesCount(): Int

    @Query("UPDATE deadlines SET category = 'Все дедлайны' WHERE category = :categoryName")
    suspend fun resetDeadlinesCategory(categoryName: String)

    @Query("DELETE FROM categories WHERE name = :categoryName")
    suspend fun deleteCategoryByName(categoryName: String)

    // Объединяем оба действия в безопасную транзакцию
    @Transaction
    suspend fun deleteCategoryAndResetDeadlines(categoryName: String) {
        resetDeadlinesCategory(categoryName)
        deleteCategoryByName(categoryName)
    }
}