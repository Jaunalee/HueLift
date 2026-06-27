package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiClient
import com.example.data.database.AppDatabase
import com.example.data.database.PaletteEntity
import com.example.data.repository.PaletteRepository
import com.example.util.ColorExtractor
import com.example.util.SampleImageGenerator
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HueLiftViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = PaletteRepository(database.paletteHistoryDao())

    // --- State Variables ---

    private val _selectedBitmap = MutableStateFlow<Bitmap?>(null)
    val selectedBitmap: StateFlow<Bitmap?> = _selectedBitmap.asStateFlow()

    private val _extractedPalette = MutableStateFlow<ColorExtractor.ExtractedPalette?>(null)
    val extractedPalette: StateFlow<ColorExtractor.ExtractedPalette?> = _extractedPalette.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _aiAnalysisText = MutableStateFlow<String?>(null)
    val aiAnalysisText: StateFlow<String?> = _aiAnalysisText.asStateFlow()

    private val _customApiKey = MutableStateFlow("")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true) // Default to elegant dark theme matching modern vision analytics
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    val historyList: StateFlow<List<PaletteEntity>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Initialize with the first preset (Warm Sunset) so the screen is beautifully populated on first launch
        selectPresetImage("sunset")
    }

    // --- Actions ---

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun setCustomApiKey(key: String) {
        _customApiKey.value = key
    }

    fun selectPresetImage(type: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val bitmap = SampleImageGenerator.generatePresetBitmap(type)
            withContext(Dispatchers.Main) {
                _selectedBitmap.value = bitmap
                _aiAnalysisText.value = null // Clear old analysis
                extractColorsFromBitmap(bitmap)
            }
        }
    }

    fun selectCustomImage(bitmap: Bitmap) {
        _selectedBitmap.value = bitmap
        _aiAnalysisText.value = null // Clear old analysis
        extractColorsFromBitmap(bitmap)
    }

    private fun extractColorsFromBitmap(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.Default) {
            val result = ColorExtractor.extractColors(bitmap)
            withContext(Dispatchers.Main) {
                _extractedPalette.value = result
            }
        }
    }

    fun updatePaletteColor(index: Int, newHex: String) {
        val current = _extractedPalette.value ?: return
        val updatedList = current.colors.toMutableList()
        if (index in updatedList.indices) {
            val oldColor = updatedList[index]
            updatedList[index] = newHex
            val updatedDominant = if (current.dominant.equals(oldColor, ignoreCase = true)) {
                newHex
            } else {
                current.dominant
            }
            _extractedPalette.value = ColorExtractor.ExtractedPalette(
                dominant = updatedDominant,
                colors = updatedList
            )
        }
    }

    fun updateDominantColor(newHex: String) {
        val current = _extractedPalette.value ?: return
        _extractedPalette.value = ColorExtractor.ExtractedPalette(
            dominant = newHex,
            colors = current.colors
        )
    }

    fun triggerAiAnalysis() {
        val bitmap = _selectedBitmap.value ?: return
        _isAnalyzing.value = true
        _aiAnalysisText.value = null

        viewModelScope.launch(Dispatchers.IO) {
            val apiKey = _customApiKey.value.trim()
            val result = GeminiClient.analyzeImagePalette(bitmap, apiKey.ifEmpty { null })
            withContext(Dispatchers.Main) {
                _isAnalyzing.value = false
                if (result != null) {
                    _aiAnalysisText.value = result.aiAnalysis
                    // Update palette if Gemini refined it
                    _extractedPalette.value = ColorExtractor.ExtractedPalette(
                        dominant = result.dominantColor,
                        colors = result.paletteColors
                    )
                } else {
                    _aiAnalysisText.value = "Unable to analyze palette. Please check that your Gemini API Key is entered correctly in the Settings tab."
                }
            }
        }
    }

    fun saveCurrentPalette() {
        val bitmap = _selectedBitmap.value ?: return
        val palette = _extractedPalette.value ?: return
        val analysis = _aiAnalysisText.value

        viewModelScope.launch(Dispatchers.IO) {
            // Scale bitmap down to thumbnail size (e.g. 128x128)
            val scaled = Bitmap.createScaledBitmap(bitmap, 128, 128, true)
            val outputStream = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val thumbnailBytes = outputStream.toByteArray()

            val entity = PaletteEntity(
                imageBytes = thumbnailBytes,
                colorsString = palette.colors.joinToString(","),
                dominantColor = palette.dominant,
                aiAnalysis = analysis,
                timestamp = System.currentTimeMillis()
            )
            repository.savePalette(entity)
        }
    }

    fun deleteHistoryItem(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePalette(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearHistory()
        }
    }

    // Helper to decode stored bytes back to Bitmap
    fun bytesToBitmap(bytes: ByteArray?): Bitmap? {
        if (bytes == null) return null
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}
