package com.example.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader

object SampleImageGenerator {

    data class PresetSample(
        val name: String,
        val description: String,
        val type: String
    )

    val presets = listOf(
        PresetSample("Warm Sunset", "Warm gradient of a golden horizon", "sunset"),
        PresetSample("Neon Cyberpunk", "Futuristic neon pink and deep cobalt", "neon"),
        PresetSample("Nordic Forest", "Calming forest teals and earthen oak", "forest"),
        PresetSample("Corporate Tech", "Sleek slate with sharp electric blue", "corporate")
    )

    fun generatePresetBitmap(type: String): Bitmap {
        val width = 300
        val height = 300
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply { isAntiAlias = true }

        when (type) {
            "sunset" -> {
                // Gradient from gold -> coral -> crimson -> deep purple
                val shader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    intArrayOf(
                        Color.parseColor("#FFE082"), // Light gold
                        Color.parseColor("#FF8A65"), // Coral
                        Color.parseColor("#E53935"), // Red/Crimson
                        Color.parseColor("#5E35B1")  // Deep purple
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )
                paint.shader = shader
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

                // Draw a beautiful glowing golden sun in the center
                paint.shader = null
                paint.color = Color.parseColor("#FFF59D")
                canvas.drawCircle(width / 2f, height / 1.6f, 60f, paint)
            }
            "neon" -> {
                // Dark background
                canvas.drawColor(Color.parseColor("#121214"))

                // Draw neon pink circle
                paint.color = Color.parseColor("#FF4081")
                canvas.drawCircle(100f, 100f, 50f, paint)

                // Draw electric cyan circle overlapping
                paint.color = Color.parseColor("#00E5FF")
                canvas.drawCircle(200f, 180f, 70f, paint)

                // Draw neon yellow geometric border lines
                paint.color = Color.parseColor("#FFEA00")
                paint.strokeWidth = 12f
                paint.style = Paint.Style.STROKE
                canvas.drawRect(50f, 50f, 250f, 250f, paint)
            }
            "forest" -> {
                // Earthen soft mist background
                canvas.drawColor(Color.parseColor("#ECEFF1"))

                // Forest green triangles (trees)
                paint.style = Paint.Style.FILL

                // Tree 1 (Deep forest green)
                paint.color = Color.parseColor("#1B5E20")
                val path1 = android.graphics.Path().apply {
                    moveTo(150f, 50f)
                    lineTo(80f, 200f)
                    lineTo(220f, 200f)
                    close()
                }
                canvas.drawPath(path1, paint)

                // Tree 2 (Teal green)
                paint.color = Color.parseColor("#00796B")
                val path2 = android.graphics.Path().apply {
                    moveTo(220f, 100f)
                    lineTo(160f, 250f)
                    lineTo(280f, 250f)
                    close()
                }
                canvas.drawPath(path2, paint)

                // Tree trunk / soil (Brown)
                paint.color = Color.parseColor("#5D4037")
                canvas.drawRect(135f, 200f, 165f, 280f, paint)
                canvas.drawRect(205f, 250f, 235f, 290f, paint)
            }
            "corporate" -> {
                // Sleek slate grey background
                canvas.drawColor(Color.parseColor("#F5F5F7"))

                // Clean corporate geometric logo (Electric Blue and Navy Blue)
                paint.style = Paint.Style.FILL

                // Left block (Electric Blue)
                paint.color = Color.parseColor("#2979FF")
                canvas.drawRect(60f, 60f, 140f, 240f, paint)

                // Right block (Navy)
                paint.color = Color.parseColor("#1A237E")
                canvas.drawRect(160f, 60f, 240f, 240f, paint)

                // Diagonal accent line (Silver Slate)
                paint.color = Color.parseColor("#90A4AE")
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 15f
                canvas.drawLine(40f, 150f, 260f, 150f, paint)
            }
            else -> {
                canvas.drawColor(Color.RED)
            }
        }

        return bitmap
    }
}
