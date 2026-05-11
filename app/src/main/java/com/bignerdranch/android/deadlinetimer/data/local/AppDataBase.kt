import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Deadline::class, SubTask::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deadlineDao(): DeadlineDao
}