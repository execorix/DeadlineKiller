import androidx.room.Database
import androidx.room.RoomDatabase
import com.bignerdranch.android.deadlinetimer.data.local.entities.Deadline
import com.bignerdranch.android.deadlinetimer.data.local.entities.SubTask


@Database(
    entities = [Deadline::class, SubTask::class],
    version = 2
)
abstract class AppDataBase : RoomDatabase() {
    abstract fun deadlineDao(): DeadlineDao
}