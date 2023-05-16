package com.example.dipolia.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@TypeConverters(value = [RoomTypeConverters::class])
@Database(entities = [LampDbModel::class], version = 12,  exportSchema = false)
abstract class AppDatabase: RoomDatabase() {

    abstract fun dipolsDao(): DipolsDao

    companion object{

        private var INSTANCE: AppDatabase? = null
        private const val DB_NAME = "dipolia.db"
        private val LOCK = Any()

        // To get an instance variable, we need a context. To avoid leaking the activity context, we use application.:
        fun getInstance(context: Context): AppDatabase {
// If it already exists:
            INSTANCE?.let {
                return it
            }
            synchronized(LOCK){
                INSTANCE?.let {
                    return it
                }
//If it is null, create a db:
                val db = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = db
                return db       //Because INSTANCE is a nullable
            }
        }
    }
}