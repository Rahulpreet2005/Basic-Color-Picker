package com.example.colorpicker

import android.R
import android.content.ClipData
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.colorpicker.ui.theme.ColorPickerTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalClipboardManager
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import kotlinx.serialization.descriptors.PrimitiveKind


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ColorPickerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ColorPickerScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun ColorPickerScreen(modifier: Modifier = Modifier){
    var currentColor by remember {mutableStateOf(Color(0.5f, 0.5f, 0.5f))}
    var selectedMode by remember {mutableStateOf(ColorMode.RGB)}
    val hexCode = String.format("#%06X", 0xFFFFFF and currentColor.toArgb())

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ){
        Box(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
                .aspectRatio(16 / 9f)
                .clip(RoundedCornerShape(24.dp))
                .background(currentColor)
        )
        Text(
            text = hexCode,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(40.dp))

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            ColorMode.entries.forEachIndexed {index, mode ->
                SegmentedButton(
                    selected = selectedMode == mode,
                    onClick = {selectedMode = mode},
                    shape = SegmentedButtonDefaults.itemShape(index, ColorMode.entries.size)
                ) {
                    Text(mode.name)
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        when(selectedMode){
            ColorMode.RGB -> {
                RgbSliders(color = currentColor, onColorChange = { currentColor = it})
            }
            ColorMode.HSV -> {
                HsvSliders(color = currentColor, onColorChange = {currentColor = it})
            }
            ColorMode.HSL -> {
                HslSliders(color = currentColor, onColorChange = {currentColor = it})
            }
        }
        val clipboard = LocalClipboard.current
        val scope = rememberCoroutineScope()
        val context = LocalContext.current

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                val clipEntry = ClipEntry(
                    ClipData.newPlainText("HEX Color", hexCode)  //for some reason it's the standard way
                )
                scope.launch {
                    clipboard.setClipEntry(clipEntry)    //some functions require coroutines since they are asynchronous
                }
                Toast.makeText(context, "Copied HEX code", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
        ) {
            Text(text = "Copy Hex Code")
        }

    }
}

@Composable
fun ColorSliderRow(label: String, value: Float, valueRange: ClosedFloatingPointRange<Float> = 0f..1f, onValueChange: (Float) -> Unit){
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text("%.2f".format(value), style = MaterialTheme.typography.bodySmall)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange
        )
    }
}

@Composable
fun RgbSliders(color: Color, onColorChange: (Color) -> Unit){
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ColorSliderRow("Red", color.red, valueRange = 0f..1f, onValueChange = { onColorChange(color.copy(red = it))})
        ColorSliderRow("Green", color.green, valueRange = 0f..1f, onValueChange = { onColorChange(color.copy(green = it))})
        ColorSliderRow("Blue", color.blue, valueRange = 0f..1f, onValueChange = { onColorChange(color.copy(blue = it))})
    }
}

@Composable
fun HsvSliders(color: Color, onColorChange: (Color) -> Unit) {
    // 1. Keep track of HSV state locally without re-calculating on every 'color' change
    var hsv by remember {
        mutableStateOf(
            FloatArray(3).apply {
                AndroidColor.colorToHSV(color.toArgb(), this)
            }
        )
    }

    // 2. Sync local state if external 'color' changes (e.g., switched tabs from RGB mode)
    LaunchedEffect(color) {
        val currentRgbFromHsv = Color(AndroidColor.HSVToColor(hsv))
        // Only re-parse if the incoming color isn't what we just generated
        if (color != currentRgbFromHsv) {
            val newHsv = FloatArray(3)
            AndroidColor.colorToHSV(color.toArgb(), newHsv)
            hsv = newHsv
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ColorSliderRow(
            label = "Hue",
            value = hsv[0],
            valueRange = 0f..360f,
            onValueChange = { newHue ->
                // Clamp to prevent 360f wrapping to 0f
                val safeHue = if (newHue >= 360f) 359.99f else newHue
                val updatedHsv = hsv.copyOf().apply { this[0] = safeHue }
                hsv = updatedHsv
                onColorChange(Color(AndroidColor.HSVToColor(updatedHsv)))
            }
        )
        ColorSliderRow(
            label = "Saturation",
            value = hsv[1],
            valueRange = 0f..1f,
            onValueChange = { newSat ->
                val updatedHsv = hsv.copyOf().apply { this[1] = newSat }
                hsv = updatedHsv
                onColorChange(Color(AndroidColor.HSVToColor(updatedHsv)))
            }
        )
        ColorSliderRow(
            label = "Value",
            value = hsv[2],
            valueRange = 0f..1f,
            onValueChange = { newValue ->
                val updatedHsv = hsv.copyOf().apply { this[2] = newValue }
                hsv = updatedHsv
                onColorChange(Color(AndroidColor.HSVToColor(updatedHsv)))
            }
        )
    }
}

@Composable
fun HslSliders(color: Color, onColorChange: (Color) -> Unit) {
    var hsl by remember {
        mutableStateOf(
            FloatArray(3).apply {
                ColorUtils.colorToHSL(color.toArgb(), this)
            }
        )
    }

    LaunchedEffect(color) {
        val currentRgbFromHsl = Color(ColorUtils.HSLToColor(hsl))
        if (color != currentRgbFromHsl) {
            val newHsl = FloatArray(3)
            ColorUtils.colorToHSL(color.toArgb(), newHsl)
            hsl = newHsl
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ColorSliderRow(
            label = "Hue",
            value = hsl[0],
            valueRange = 0f..360f,
            onValueChange = { newHue ->
                val safeHue = if (newHue >= 360f) 359.99f else newHue
                val updatedHsl = hsl.copyOf().apply { this[0] = safeHue }
                hsl = updatedHsl // FIX: Assigning updatedHsl updates the state so UI re-renders!
                onColorChange(Color(ColorUtils.HSLToColor(updatedHsl)))
            }
        )
        ColorSliderRow(
            label = "Saturation",
            value = hsl[1],
            valueRange = 0f..1f,
            onValueChange = { newSat ->
                val updatedHsl = hsl.copyOf().apply { this[1] = newSat }
                hsl = updatedHsl
                onColorChange(Color(ColorUtils.HSLToColor(updatedHsl)))
            }
        )
        ColorSliderRow(
            label = "Lightness",
            value = hsl[2],
            valueRange = 0f..1f,
            onValueChange = { newLight ->
                val updatedHsl = hsl.copyOf().apply { this[2] = newLight }
                hsl = updatedHsl
                onColorChange(Color(ColorUtils.HSLToColor(updatedHsl)))
            }
        )
    }
}

enum class ColorMode {RGB, HSV, HSL}