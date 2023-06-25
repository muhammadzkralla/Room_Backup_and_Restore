package com.zkrallah.backupandrestore

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.zkrallah.backupandrestore.App.Companion.ctx

@Database(entities = [User::class], version = 2)
abstract class UsersDatabase : RoomDatabase() {
    abstract fun usersDAO(): UserDAO

    companion object {
        private var instance: UsersDatabase? = null
        private val context = ctx

        @Synchronized
        fun getInstance(): UsersDatabase {
            if (instance == null)
                instance = Room.databaseBuilder(context.applicationContext, UsersDatabase::class.java,
                    "users_database")
                    .fallbackToDestructiveMigration()
                    .build()

            return instance!!
        }
    }
}