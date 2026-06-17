package com.task.ibiproductsapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.task.ibiproductsapp.data.local.dao.FavoriteDao
import com.task.ibiproductsapp.data.local.dao.ProductDao
import com.task.ibiproductsapp.data.local.entity.FavoriteEntity
import com.task.ibiproductsapp.data.local.entity.ProductEntity

@Database(
    entities = [ProductEntity::class, FavoriteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun favoriteDao(): FavoriteDao
}
