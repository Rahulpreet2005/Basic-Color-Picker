package com.example.colorpicker

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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalClipboardManager
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ColorPickerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    //We're going to place here all the functions we are going to use
                    ColorPickerScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
@Composable
fun ColorPickerScreen(modifier: Modifier = Modifier){
    var red by remember { mutableFloatStateOf(0.5f) }
    var green by remember { mutableFloatStateOf(0.5f)}
    var blue by remember { mutableFloatStateOf(0.5f)}
    val currentColor = Color(red = red, green = green, blue = blue)
    val redInt = (red * 255).toInt()
    val greenInt = (green * 255).toInt()
    val blueInt = (blue * 255).toInt()

    val hexCode = String.format("#%02X%02X%02X", redInt, greenInt, blueInt)
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current


    //The main container
    Column(
        //how this column behaves in the layout
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        //how items are going to align horizontally
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Box(
            modifier = Modifier   //Modifiers order matter here, it executes from the top to bottom
                .fillMaxWidth()
                .height(200.dp)   //We define first the width and the height of the box
                .clip(RoundedCornerShape(16.dp))  //then we clip the area of the rectangle
                .background(currentColor)  //and finally fill it with a color
        )

        Text(
            text = hexCode,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )


        Spacer(modifier = Modifier.height(100.dp))

        //red slider
        Text(
            text = "Red: $redInt",
            modifier = Modifier.align(Alignment.Start)
        )
        Slider(
            value = red,
            onValueChange = { red = it },
            valueRange = 0f..1f
        )

        //green slider
        Text(
            text = "Green: $greenInt",
            modifier = Modifier.align(Alignment.Start)
        )
        Slider(
            value = green,
            onValueChange = { green = it },
            valueRange = 0f..1f
        )

        //blue slider
        Text(
            text = "Blue: $blueInt",
            modifier = Modifier.align(Alignment.Start)
        )
        Slider(
            value = blue,
            onValueChange = { blue = it},
            valueRange = 0f..1f
        )

        Spacer(modifier = Modifier.height(100.dp))


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
/*
In order to do the rgb, hsv and hsl conversion I need a few things:
- I need to be able to see the current state
- I need to be able to change the texts that are above each slider, it can't still be red if it's hsv
- I need to be able to store all the normalized data in 3 variables instead of storing converted numbers
- I need my functions to be flexible enough to change their formula according to the state in order for me to not need
to create functions for conversion. If I store only normalized data and my functions can conditionally change their formula
I should be able to output all 3 colors without needing conversion
- The output under the rectangle shouldn't be always a hex code but should instead be whatever format we're following
 */





/*
TODO:
- Add a notification when copying to the clipboard ✓
- Add a toggle to switch between rgb, hsv and hsl
- Add a save color button that appends the current color to scrollable grid
- Try to save the saved colors in the phone
- Make the sliders colored
 */