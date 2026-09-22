package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Database(entities = [MenuItem::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun menuDao(): MenuDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lucknawi_chai_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(MenuDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class MenuDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.menuDao())
                }
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    val dao = database.menuDao()
                    // If table is empty, populate it
                    if (dao.getAllMenuItems().first().isEmpty()) {
                        populateDatabase(dao)
                    }
                }
            }
        }

        suspend fun populateDatabase(menuDao: MenuDao) {
            val items = listOf(
                MenuItem(name = "Plain Tea", category = "Tea Menu", price = 20.0),
                MenuItem(name = "Kulhad Tea", category = "Tea Menu", price = 30.0),
                MenuItem(name = "Tandoori Tea", category = "Tea Menu", price = 40.0),
                
                MenuItem(name = "Black Coffee", category = "Coffee", price = 25.0),
                MenuItem(name = "Hot Coffee", category = "Coffee", price = 40.0),
                
                MenuItem(name = "Classic Butter Bun", category = "Bun Makkhan", price = 35.0),
                MenuItem(name = "Fry Butter Bun", category = "Bun Makkhan", price = 40.0),
                MenuItem(name = "Tandoori Butter Bun", category = "Bun Makkhan", price = 50.0),
                
                MenuItem(name = "Veg Sandwich", category = "Sandwich", price = 50.0),
                MenuItem(name = "Cheese Sandwich", category = "Sandwich", price = 60.0),
                MenuItem(name = "Paneer Sandwich", category = "Sandwich", price = 70.0),
                
                MenuItem(name = "Plain Maggie", category = "Maggie", price = 40.0),
                MenuItem(name = "Veg Maggie", category = "Maggie", price = 50.0),
                MenuItem(name = "Butter Maggie", category = "Maggie", price = 60.0),
                MenuItem(name = "Cheese Maggie", category = "Maggie", price = 65.0),
                MenuItem(name = "Paneer Maggie", category = "Maggie", price = 75.0),
                MenuItem(name = "Handi Tandoori Maggie", category = "Maggie", price = 85.0)
            )
            menuDao.insertAll(items)
        }
    }
}
