package com.scarpim.digitclassifier

import android.os.Bundle
import android.view.MotionEvent
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.scarpim.digitclassifier.ui.theme.DigitClassifierTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DigitClassifierTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    ScratchPad()
                }
            }
        }
    }
}

@Composable
fun ScratchPad() {
    val path = remember {mutableStateOf(mutableListOf<PathState>())}
    Scaffold(
        topBar = {
            ComposePaintAppBar{
                path.value = mutableListOf()
            }
        }
    ) {
        Column {
            PaintBody(path)
            Spacer(modifier = Modifier.size(20.dp))
            IconButton(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = {
                // TODO take screenshot and call model
            }) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription ="Classify"
                )
            }
        }
    }
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
            .fillMaxHeight(0.7f)
            .background(Color.Black)
    ) {
        val drawColor = Color.White
        val drawBrush = 30f

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
        ScratchPad()
    }
}