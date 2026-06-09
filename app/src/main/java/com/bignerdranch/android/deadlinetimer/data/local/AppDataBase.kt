import androidx.room.Database
import androidx.room.RoomDatabase
import com.bignerdranch.android.deadlinetimer.data.local.entities.Category
import com.bignerdranch.android.deadlinetimer.data.local.entities.Deadline
import com.bignerdranch.android.deadlinetimer.data.local.entities.SubTask


@Database(
    entities = [Deadline::class, SubTask::class, Category::class],
    version = 3
)
abstract class AppDataBase : RoomDatabase() {
    abstract fun deadlineDao(): DeadlineDao
}