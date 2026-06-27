package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PaletteHistoryDao {
    @Query("SELECT * FROM palette_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<PaletteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPalette(palette: PaletteEntity): Long

    @Query("DELETE FROM palette_history WHERE id = :id")
    suspend fun deletePaletteById(id: Int)

    @Query("DELETE FROM palette_history")
    suspend fun clearAllHistory()
}
