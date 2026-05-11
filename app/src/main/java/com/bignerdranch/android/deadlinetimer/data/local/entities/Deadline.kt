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
    val isExtended: Boolean = false
)fg


@Entity(tableName = "sub_tasks")
data class SubTask(
    @PrimaryKey(autoGenerate = true) val subTaskId: Int = 0,
    val parentDeadlineId: Int,
    val taskText: String,
    val isCompleted: Boolean = false
)