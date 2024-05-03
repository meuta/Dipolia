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

        private const val DB_NAME = "dipolia.db"

        fun getInstance(context: Context): AppDatabase {
                return Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()

        }
    }
}