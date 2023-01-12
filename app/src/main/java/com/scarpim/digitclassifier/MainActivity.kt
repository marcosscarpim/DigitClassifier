package com.scarpim.digitclassifier

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.scarpim.digitclassifier.classifier.Classifier
import com.scarpim.digitclassifier.classifier.Recognition
import com.scarpim.digitclassifier.ui.theme.DigitClassifierTheme
import java.io.IOException

class MainActivity : ComponentActivity() {

    private lateinit var classifier: Classifier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initClassifier()

        setContent {
            DigitClassifierTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    ScratchPad(classifier)
                }
            }
        }
    }

    private fun initClassifier() {
        try {
            classifier = Classifier(this)
            Log.v("MarcosLog", "Classifier initialized")
        } catch (e: IOException) {
            Toast.makeText(this, "Failed to load model", Toast.LENGTH_LONG).show()
            Log.e("MarcosLog", "init(): Failed to create Classifier", e)
        }
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun ScratchPad(classifier: Classifier) {
    val path = remember {mutableStateOf(mutableListOf<PathState>())}
    val result = remember { mutableStateOf<Recognition?>(null) }
    Scaffold(
        topBar = {
            ComposePaintAppBar{
                path.value = mutableListOf()
                result.value = null
            }
        }
    ) {

        Column {
            val snapShot = captureBitmap {
                PaintBody(path)
            }
            Spacer(modifier = Modifier.size(20.dp))
            IconButton(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = {
                    val bitmap = snapShot.invoke()
                    val scaled = bitmap.scaleBitmap(28, 28)
                    result.value = classifier.classify(scaled)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription ="Classify"
                )
            }
            if (result.value != null) {
                val text = "Result = ${result.value?.label}"
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = text
                )
            }
        }
    }
}

fun Bitmap.scaleBitmap(width: Int, height: Int): Bitmap {
    val scaledBitmap = Bitmap.createScaledBitmap(this, width, height, false)
    //this.recycle()
    return scaledBitmap
}


// Top app Bar composable with application name and Icon
@Composable
fun ComposePaintAppBar(
    onDelete:()-> Unit
) {
    TopAppBar(
        title = {
            Text(text = "Digit Classifier")
        },
        actions = {
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription ="Delete"
                )
            }
        }
    )
}

// Define all the components used in drawing that is the drawing canvas and drawing tool
@Composable
fun PaintBody(path:MutableState<MutableList<PathState>>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.6f)
            .background(Color.White)
    ) {
        val drawColor = Color.Black
        val drawBrush = 100f

        path.value.add(PathState(Path(),drawColor,drawBrush))

        DrawingCanvas(
            drawColor,
            drawBrush,
            path.value
        )
    }
}

// A composable that listen to all the movements across the XY axis from the current path to all other movements made and draws the line on each movement detected.
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DrawingCanvas(
    drawColor: Color,
    drawBrush: Float,
    path: MutableList<PathState>
) {
    val currentPath = path.last().path
    val movePath = remember{ mutableStateOf<Offset?>(null)}
    val canvasSize = remember { mutableStateOf<Size?>(null) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 100.dp)
            .pointerInteropFilter {
                when (it.action) {
                    MotionEvent.ACTION_DOWN -> {
                        currentPath.moveTo(it.x, it.y)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (it.y < (canvasSize.value?.height ?: 0f)) {
                            movePath.value = Offset(it.x, it.y)
                        }
                    }
                    else -> {
                        movePath.value = null
                    }
                }
                true
            }
    ){
        canvasSize.value = size
        movePath.value?.let {
            currentPath.lineTo(it.x,it.y)
            drawPath(
                path = currentPath,
                color = drawColor,
                style = Stroke(drawBrush)
            )
        }
        path.forEach {
            drawPath(
                path = it.path,
                color = it.color,
                style  = Stroke(it.stroke)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DigitClassifierTheme {
        //ScratchPad()
    }
}