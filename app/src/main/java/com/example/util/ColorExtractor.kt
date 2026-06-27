package com.example.util

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.sqrt

object ColorExtractor {

    data class ExtractedPalette(
        val dominant: String,
        val colors: List<String>
    )

    fun extractColors(bitmap: Bitmap, targetCount: Int = 5): ExtractedPalette {
        // Scale down the bitmap to 50x50 to make color extraction extremely fast
        val scaled = Bitmap.createScaledBitmap(bitmap, 50, 50, false)
        val width = scaled.width
        val height = scaled.height
        val pixels = IntArray(width * height)
        scaled.getPixels(pixels, 0, width, 0, 0, width, height)

        // Count color frequencies, ignore transparent pixels
        val counts = mutableMapOf<Int, Int>()
        for (color in pixels) {
            val alpha = Color.alpha(color)
            if (alpha < 100) continue // Skip transparent/semi-transparent pixels

            // Soft-group similar colors by rounding their R, G, B components to nearest multiple of 12
            // This reduces the space from 16M colors to a few hundred, creating strong frequency clusters!
            val r = (Color.red(color) / 12) * 12
            val g = (Color.green(color) / 12) * 12
            val b = (Color.blue(color) / 12) * 12
            val groupedColor = Color.rgb(r, g, b)

            counts[groupedColor] = (counts[groupedColor] ?: 0) + 1
        }

        if (counts.isEmpty()) {
            return ExtractedPalette(
                dominant = "#1A1C1C",
                colors = listOf("#1A1C1C", "#BA1A1A", "#5E5E61", "#F9F9F9", "#000000")
            )
        }

        // Sort grouped colors by frequency
        val sortedByFreq = counts.entries.sortedByDescending { it.value }.map { it.key }

        val selectedColors = mutableListOf<Int>()
        // The first one is the absolute dominant color!
        selectedColors.add(sortedByFreq[0])

        // Iterate through other colors and select those that are visually distinct
        val minDistance = 75.0 // RGB Euclidean distance threshold
        for (groupedColor in sortedByFreq.drop(1)) {
            if (selectedColors.size >= targetCount) break

            var isDistinct = true
            for (selected in selectedColors) {
                if (colorDistance(groupedColor, selected) < minDistance) {
                    isDistinct = false
                    break
                }
            }
            if (isDistinct) {
                selectedColors.add(groupedColor)
            }
        }

        // If we didn't reach targetCount because of distance threshold, reduce threshold and fill up
        if (selectedColors.size < targetCount) {
            for (groupedColor in sortedByFreq.drop(1)) {
                if (selectedColors.size >= targetCount) break
                if (!selectedColors.contains(groupedColor)) {
                    selectedColors.add(groupedColor)
                }
            }
        }

        // Convert to hex strings
        val hexList = selectedColors.map { colorToHex(it) }
        val dominantHex = colorToHex(sortedByFreq[0])

        return ExtractedPalette(
            dominant = dominantHex,
            colors = hexList
        )
    }

    fun colorToRgbString(hex: String): String {
        return try {
            val colorInt = Color.parseColor(hex)
            "${Color.red(colorInt)}, ${Color.green(colorInt)}, ${Color.blue(colorInt)}"
        } catch (e: Exception) {
            "0, 0, 0"
        }
    }

    private fun colorDistance(c1: Int, c2: Int): Double {
        val rDiff = Color.red(c1) - Color.red(c2)
        val gDiff = Color.green(c1) - Color.green(c2)
        val bDiff = Color.blue(c1) - Color.blue(c2)
        return sqrt((rDiff * rDiff + gDiff * gDiff + bDiff * bDiff).toDouble())
    }

    private fun colorToHex(color: Int): String {
        return String.format("#%06X", 0xFFFFFF and color)
    }
}
