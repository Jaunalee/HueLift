package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

@Entity(tableName = "palette_history")
data class PaletteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val imageBytes: ByteArray? = null, // Scaled-down JPEG thumbnail
    val colorsString: String, // Comma-separated list of hex strings
    val dominantColor: String,
    val aiAnalysis: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

class RoomConverters {
    @TypeConverter
    fun fromString(value: String): List<String> {
        if (value.isEmpty()) return emptyList()
        return value.split(",")
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        return list.joinToString(",")
    }
}
