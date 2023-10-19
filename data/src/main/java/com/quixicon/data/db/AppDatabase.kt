package com.quixicon.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.quixicon.data.db.dao.CardDao
import com.quixicon.data.db.dao.CollectionDao
import com.quixicon.data.db.dao.LinkDao
import com.quixicon.data.db.entities.CardEntity
import com.quixicon.data.db.entities.CollectionEntity
import com.quixicon.data.db.entities.LinkEntity
import timber.log.Timber
import java.util.concurrent.Executors

@Database(
    version = 1,
    exportSchema = false,
    entities = [CardEntity::class, CollectionEntity::class, LinkEntity::class]
)

abstract class AppDatabase : RoomDatabase() {

    abstract fun cardDao(): CardDao

    abstract fun collectionDao(): CollectionDao

    abstract fun linkDao(): LinkDao

    companion object {
        private const val DB_NAME = "app_database.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun createInstance(context: Context) {
            if (instance == null) {
                instance = Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
                    .setQueryCallback(object: QueryCallback {
                        override fun onQuery(sqlQuery: String, bindArgs: List<Any?>) {
                            Timber.e("SQL Query: $sqlQuery SQL Args: $bindArgs")
                        }

                    }, Executors.newSingleThreadExecutor())
                    .fallbackToDestructiveMigration()
                    .build()
            }
        }

        fun getInstance(): AppDatabase {
            if (instance == null) throw NullPointerException("AppDatabase instance is null")
            return instance!!
        }
    }
}
