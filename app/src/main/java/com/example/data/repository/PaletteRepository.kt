package com.example.data.repository

import com.example.data.database.PaletteEntity
import com.example.data.database.PaletteHistoryDao
import kotlinx.coroutines.flow.Flow

class PaletteRepository(private val dao: PaletteHistoryDao) {
    val allHistory: Flow<List<PaletteEntity>> = dao.getAllHistory()

    suspend fun savePalette(palette: PaletteEntity): Long {
        return dao.insertPalette(palette)
    }

    suspend fun deletePalette(id: Int) {
        dao.deletePaletteById(id)
    }

    suspend fun clearHistory() {
        dao.clearAllHistory()
    }
}
