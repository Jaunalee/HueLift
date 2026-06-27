package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.HueLiftViewModel
import com.example.util.ColorExtractor
import com.example.util.SampleImageGenerator
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExtractScreen(
    viewModel: HueLiftViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val selectedBitmap by viewModel.selectedBitmap.collectAsState()
    val palette by viewModel.extractedPalette.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val aiAnalysisText by viewModel.aiAnalysisText.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    var toastMessage by remember { mutableStateOf<String?>(null) }
    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }

    var selectedColorForInspection by remember { mutableStateOf<Pair<Int, String>?>(null) } // index to hex
    var previewStyle by remember { mutableStateOf("app_ui") } // app_ui, dashboard, poster

    // Clear toasts automatically
    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(2000)
            toastMessage = null
        }
    }
    LaunchedEffect(saveSuccessMessage) {
        if (saveSuccessMessage != null) {
            delay(2000)
            saveSuccessMessage = null
        }
    }

    // Image Picker Setup
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, it)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                }
                viewModel.selectCustomImage(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 96.dp) // Leave room for bottom navigation
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- Presets Section ---
            Text(
                text = "Preset Samples",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SampleImageGenerator.presets.forEach { preset ->
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { viewModel.selectPresetImage(preset.type) }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .width(130.dp)
                    ) {
                        Text(
                            text = preset.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = preset.description,
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Drag & Drop / Upload Area ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .aspectRatio(1.4f)
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { imagePickerLauncher.launch("image/*") }
                    .testTag("upload_drop_zone"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    if (selectedBitmap != null) {
                        Image(
                            bitmap = selectedBitmap!!.asImageBitmap(),
                            contentDescription = "Selected Brand Logo",
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tap to choose a different logo",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Upload Icon",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Upload Logo",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap to upload or drag logo here",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "PNG, JPG or SVG up to 10MB",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- Extracted Colors Title ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Extracted Colors",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${palette?.colors?.size ?: 0} COLORS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Color Bento Grid ---
            if (palette != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Two-column layout for the standard palette colors
                    val indexedColors = palette!!.colors.mapIndexed { index, hex -> index to hex }
                    val chunked = indexedColors.chunked(2)
                    chunked.forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            pair.forEach { (index, hex) ->
                                val isDominant = hex.equals(palette!!.dominant, ignoreCase = true)
                                ColorCard(
                                    hex = hex,
                                    isDominant = isDominant,
                                    isDarkTheme = isDarkTheme,
                                    modifier = Modifier.weight(1f),
                                    onCopy = {
                                        selectedColorForInspection = index to hex
                                    }
                                )
                            }
                            // If it's a single item, put a spacer
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    // Highlight the Dominant color uniquely as a col-span-2 full row at the bottom (matching screenshot asymmetry!)
                    val dominantIndex = palette!!.colors.indexOfFirst { it.equals(palette!!.dominant, ignoreCase = true) }.let { if (it == -1) 0 else it }
                    ColorDominantRow(
                        hex = palette!!.dominant,
                        isDarkTheme = isDarkTheme,
                        onCopy = {
                            selectedColorForInspection = dominantIndex to palette!!.dominant
                        }
                    )
                }
            } else {
                Text(
                    text = "No colors extracted yet. Choose an image or preset to begin.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- Composition Preview & AI Analysis Side-by-Side/Stack ---
            if (palette != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Composition Preview",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    // Tiny low-profile guide label
                    Text(
                        text = "SELECT MOCKUP STYLE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Segmented visual selector for Preview Styles
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val styles = listOf(
                        "app_ui" to "Mobile App",
                        "dashboard" to "Dashboard",
                        "poster" to "Poster Art"
                    )
                    styles.forEach { (styleKey, styleLabel) ->
                        val isSelected = previewStyle == styleKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(100.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .clickable { previewStyle = styleKey }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = styleLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Render real-time preview card using selected mockup style
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(24.dp)
                ) {
                    if (previewStyle == "app_ui") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Header Row of mock app UI
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "H U E L I F T  M O B I L E",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(android.graphics.Color.parseColor(palette!!.colors.getOrElse(0) { palette!!.dominant }))
                                )
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(palette!!.colors.getOrElse(1) { palette!!.dominant })))
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Primary brand hero card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(android.graphics.Color.parseColor(palette!!.colors.getOrElse(0) { palette!!.dominant })))
                                    .padding(16.dp),
                                contentAlignment = Alignment.BottomStart
                            ) {
                                Column {
                                    Text(
                                        text = "Aesthetic Balance",
                                        color = if (isColorDark(try { android.graphics.Color.parseColor(palette!!.colors.getOrElse(0) { palette!!.dominant }) } catch (e: Exception) { android.graphics.Color.BLACK })) Color.White else Color.Black,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "Swiss minimalism applied dynamically",
                                        color = if (isColorDark(try { android.graphics.Color.parseColor(palette!!.colors.getOrElse(0) { palette!!.dominant }) } catch (e: Exception) { android.graphics.Color.BLACK })) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Interactive pill lists
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(android.graphics.Color.parseColor(palette!!.colors.getOrElse(1) { palette!!.dominant })).copy(alpha = 0.15f))
                                        .border(1.dp, Color(android.graphics.Color.parseColor(palette!!.colors.getOrElse(1) { palette!!.dominant })), RoundedCornerShape(12.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "Primary Accent",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(android.graphics.Color.parseColor(palette!!.colors.getOrElse(1) { palette!!.dominant }))
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(android.graphics.Color.parseColor(palette!!.colors.getOrElse(2) { palette!!.dominant })).copy(alpha = 0.15f))
                                        .border(1.dp, Color(android.graphics.Color.parseColor(palette!!.colors.getOrElse(2) { palette!!.dominant })), RoundedCornerShape(12.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "Secondary Accent",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(android.graphics.Color.parseColor(palette!!.colors.getOrElse(2) { palette!!.dominant }))
                                    )
                                }
                            }
                        }
                    } else if (previewStyle == "dashboard") {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ANALYTICS ENGINE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(android.graphics.Color.parseColor(palette!!.colors.getOrElse(1) { palette!!.dominant })).copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "LIVE TELEMETRY",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(android.graphics.Color.parseColor(palette!!.colors.getOrElse(1) { palette!!.dominant }))
                                    )
                                }
                            }

                            // Dynamic high-contrast charts representing color parity
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                val heights = listOf(0.4f, 0.75f, 0.95f, 0.6f, 0.85f)
                                heights.forEachIndexed { idx, pct ->
                                    val colorHex = palette!!.colors.getOrElse(idx % palette!!.colors.size) { palette!!.dominant }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(pct)
                                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 0.dp, bottomEnd = 0.dp))
                                            .background(Color(android.graphics.Color.parseColor(colorHex)))
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Chromatic Dispersion",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Balanced multi-tone structure configured",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "98%",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(android.graphics.Color.parseColor(palette!!.colors.getOrElse(0) { palette!!.dominant }))
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            // Geometric brand shapes overlays
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(palette!!.colors.getOrElse(1) { palette!!.dominant })).copy(alpha = 0.25f))
                                    .align(Alignment.CenterEnd)
                            )
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(android.graphics.Color.parseColor(palette!!.colors.getOrElse(2) { palette!!.dominant })).copy(alpha = 0.35f))
                                    .align(Alignment.TopCenter)
                            )

                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "SWISS\nSYSTEM.",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 28.sp,
                                    lineHeight = 28.sp,
                                    color = Color(android.graphics.Color.parseColor(palette!!.colors.getOrElse(0) { palette!!.dominant }))
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Text(
                                        text = "CLINICAL SPATIAL METRICS //\nHUELIFT SYSTEM LAYOUTS",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 8.sp,
                                        lineHeight = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "v2.0",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(android.graphics.Color.parseColor(palette!!.colors.getOrElse(3) { palette!!.dominant }))
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- AI Vision Analysis Card ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AI Visual Analysis",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Button(
                            onClick = { viewModel.triggerAiAnalysis() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(100.dp),
                            contentPadding = ButtonDefaults.ContentPadding,
                            modifier = Modifier.testTag("ai_analysis_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Magic AI",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Analyze", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(20.dp)
                    ) {
                        if (isAnalyzing) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "HueLift AI is scanning branding layers...",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else if (aiAnalysisText != null) {
                            Column {
                                Text(
                                    text = "\"$aiAnalysisText\"",
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp,
                                    fontWeight = FontWeight.Normal,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "AI ANALYSIS READY",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        } else {
                            Column {
                                Text(
                                    text = "Tap 'Analyze' above to scan this image using HueLift's AI vision models. It will compose an elegant narrative explaining your brand's emotional spectrum and composition synergy.",
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                // --- CTAs Section ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.saveCurrentPalette()
                            saveSuccessMessage = "Palette saved to History!"
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("save_palette_button")
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = "Save Palette")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Palette", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Button(
                        onClick = {
                            // Copy all colors comma separated as export
                            val exportStr = palette!!.colors.joinToString(", ")
                            clipboardManager.setText(AnnotatedString(exportStr))
                            toastMessage = "Copied whole palette: $exportStr"
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("export_palette_button")
                    ) {
                        Icon(imageVector = Icons.Default.Upload, contentDescription = "Export Palette")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Palette", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        // --- Clipboard Toast Alert ---
        AnimatedVisibility(
            visible = toastMessage != null,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(if (isDarkTheme) Color(0xFFE5E2E3) else Color(0xFF131314))
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .testTag("toast_alert")
            ) {
                Text(
                    text = toastMessage ?: "",
                    color = if (isDarkTheme) Color(0xFF131314) else Color(0xFFE5E2E3),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // --- Color Studio Inspector Dialog ---
        selectedColorForInspection?.let { (index, hex) ->
            ColorInspectorDialog(
                hex = hex,
                onDismiss = { selectedColorForInspection = null },
                onSave = { newHex ->
                    viewModel.updatePaletteColor(index, newHex)
                    selectedColorForInspection = null
                    toastMessage = "Color adjusted to $newHex"
                },
                onCopy = {
                    clipboardManager.setText(AnnotatedString(hex))
                    toastMessage = "$hex copied to clipboard"
                    selectedColorForInspection = null
                }
            )
        }

        // --- Save Success Alert ---
        AnimatedVisibility(
            visible = saveSuccessMessage != null,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color(0xFF4CAF50))
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Success", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = saveSuccessMessage ?: "",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ColorCard(
    hex: String,
    isDominant: Boolean,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    onCopy: () -> Unit
) {
    val rGbText = ColorExtractor.colorToRgbString(hex)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = if (isDominant) 2.dp else 1.dp,
                color = if (isDominant) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onCopy() }
    ) {
        // Color block
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color(android.graphics.Color.parseColor(hex)))
        )

        // Details block
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = hex.uppercase(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy Hex",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = rGbText,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ColorDominantRow(
    hex: String,
    isDarkTheme: Boolean,
    onCopy: () -> Unit
) {
    val rGbText = ColorExtractor.colorToRgbString(hex)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onCopy() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Large dominant color block
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(android.graphics.Color.parseColor(hex)))
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = hex.uppercase(),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = rGbText,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "DOMINANT",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy Hex",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ColorInspectorDialog(
    hex: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onCopy: () -> Unit
) {
    // Parse hex safely
    val colorInt = remember(hex) {
        try {
            android.graphics.Color.parseColor(hex)
        } catch (e: Exception) {
            android.graphics.Color.BLACK
        }
    }

    val hsv = remember(hex) {
        val arr = FloatArray(3)
        android.graphics.Color.colorToHSV(colorInt, arr)
        arr
    }

    var hue by remember(hex) { mutableStateOf(hsv[0]) }
    var saturation by remember(hex) { mutableStateOf(hsv[1]) }
    var value by remember(hex) { mutableStateOf(hsv[2]) }

    // Calculate current edited color
    val currentEditedColorInt = remember(hue, saturation, value) {
        android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value))
    }
    val currentEditedHex = remember(currentEditedColorInt) {
        String.format("#%06X", 0xFFFFFF and currentEditedColorInt)
    }

    // Calculate WCAG contrasts
    val contrastWhite = remember(currentEditedColorInt) {
        calculateContrast(currentEditedColorInt, android.graphics.Color.WHITE)
    }
    val contrastBlack = remember(currentEditedColorInt) {
        calculateContrast(currentEditedColorInt, android.graphics.Color.BLACK)
    }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Color Studio Inspector",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Tweak the HSV sliders below to fine-tune your brand palette color in real-time.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Large Comparison Blocks
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    horizontalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    // Original color
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color(colorInt)),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "ORIGINAL",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isColorDark(colorInt)) Color.White else Color.Black,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isColorDark(colorInt)) Color(0x22FFFFFF) else Color(0x22000000))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = hex.uppercase(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (isColorDark(colorInt)) Color.White else Color.Black
                            )
                        }
                    }

                    // Edited color
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight()
                            .background(Color(currentEditedColorInt)),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "ADJUSTED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isColorDark(currentEditedColorInt)) Color.White else Color.Black,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isColorDark(currentEditedColorInt)) Color(0x44FFFFFF) else Color(0x44000000))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentEditedHex,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                color = if (isColorDark(currentEditedColorInt)) Color.White else Color.Black
                            )
                        }
                    }
                }

                // Fine-tuning HSV Sliders
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Hue
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Hue: ${hue.toInt()}°",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Color Tone",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        androidx.compose.material3.Slider(
                            value = hue,
                            onValueChange = { hue = it },
                            valueRange = 0f..360f,
                            colors = androidx.compose.material3.SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                        )
                    }

                    // Saturation
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Saturation: ${(saturation * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Intensity",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        androidx.compose.material3.Slider(
                            value = saturation,
                            onValueChange = { saturation = it },
                            valueRange = 0f..1f,
                            colors = androidx.compose.material3.SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                        )
                    }

                    // Brightness (Value)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Value (Brightness): ${(value * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Luminance",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        androidx.compose.material3.Slider(
                            value = value,
                            onValueChange = { value = it },
                            valueRange = 0f..1f,
                            colors = androidx.compose.material3.SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                        )
                    }
                }

                // WCAG Contrast Verification Badge Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "WCAG 2.1 CONTRAST VERIFICATION",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Contrast vs White
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "vs White Text", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = String.format("%.1f:1", contrastWhite),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (contrastWhite >= 4.5) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                            Text(
                                text = if (contrastWhite >= 7.0) "AAA PASS" else if (contrastWhite >= 4.5) "AA PASS" else "FAIL",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (contrastWhite >= 4.5) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                        }

                        // Contrast vs Black
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "vs Black Text", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = String.format("%.1f:1", contrastBlack),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (contrastBlack >= 4.5) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                            Text(
                                text = if (contrastBlack >= 7.0) "AAA PASS" else if (contrastBlack >= 4.5) "AA PASS" else "FAIL",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (contrastBlack >= 4.5) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.Button(
                onClick = { onSave(currentEditedHex) },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Apply Changes", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel", fontSize = 13.sp)
            }
        }
    )
}

fun isColorDark(color: Int): Boolean {
    val r = android.graphics.Color.red(color)
    val g = android.graphics.Color.green(color)
    val b = android.graphics.Color.blue(color)
    val luma = 0.299 * r + 0.587 * g + 0.114 * b
    return luma < 140
}

fun calculateLuminance(colorInt: Int): Double {
    val r = android.graphics.Color.red(colorInt) / 255.0
    val g = android.graphics.Color.green(colorInt) / 255.0
    val b = android.graphics.Color.blue(colorInt) / 255.0

    val rL = if (r <= 0.03928) r / 12.92 else Math.pow((r + 0.055) / 1.055, 2.4)
    val gL = if (g <= 0.03928) g / 12.92 else Math.pow((g + 0.055) / 1.055, 2.4)
    val bL = if (b <= 0.03928) b / 12.92 else Math.pow((b + 0.055) / 1.055, 2.4)

    return 0.2126 * rL + 0.7152 * gL + 0.0722 * bL
}

fun calculateContrast(color1: Int, color2: Int): Double {
    val lum1 = calculateLuminance(color1)
    val lum2 = calculateLuminance(color2)
    val brightest = maxOf(lum1, lum2)
    val darkest = minOf(lum1, lum2)
    return (brightest + 0.05) / (darkest + 0.05)
}

