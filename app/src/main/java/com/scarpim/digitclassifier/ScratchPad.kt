/**
 * Copyright @marcosscarpim.
 */

package com.scarpim.digitclassifier

import android.annotation.SuppressLint
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.unit.dp
import com.scarpim.digitclassifier.classifier.Recognition

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun ScratchPad(viewModel: MainViewModel) {
    val path = remember { mutableStateListOf<Path>() }
    val result = viewModel.result.collectAsState()

    Scaffold(
        topBar = {
            ComposePaintAppBar{
                path.clear()
            }
        }
    ) {
        Column {
            val snapShot = captureBitmap {
                PaintBody(path)
            }
            Spacer(modifier = Modifier.size(20.dp))
            ResultBody(
                Modifier.align(Alignment.CenterHorizontally),
                result = result.value
            ) {
                viewModel.classify(snapShot.invoke())
            }
        }
    }
}

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

@Composable
fun PaintBody(path: MutableList<Path>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.6f)
            .background(Color.White)
    ) {
        val drawColor = Color.Black
        val drawBrush = 100f

        DrawingCanvas(
            drawColor,
            drawBrush,
            path
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DrawingCanvas(
    drawColor: Color,
    drawBrush: Float,
    path: MutableList<Path>
) {
    val movePath = remember{ mutableStateOf<Offset?>(null)}
    val canvasSize = remember { mutableStateOf<Size?>(null) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape = RoundedCornerShape(4.dp))
            .pointerInteropFilter {
                when (it.action) {
                    MotionEvent.ACTION_DOWN -> {
                        path.add(Path())
                        path.last().moveTo(it.x, it.y)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        movePath.value = Offset(it.x,it.y)
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
            path.last().lineTo(it.x,it.y)
            drawPath(
                path = path.last(),
                color = drawColor,
                style = Stroke(drawBrush)
            )
        }
        path.forEach {
            drawPath(
                path = it,
                color = drawColor,
                style  = Stroke(drawBrush)
            )
        }
    }
}

@Composable
fun ResultBody(
    modifier: Modifier = Modifier,
    result: Recognition?,
    onBuildClick: () -> Unit
) {
    Column(modifier) {
        IconButton(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = { onBuildClick() }
        ) {
            Icon(
                imageVector = Icons.Default.Build,
                contentDescription ="Classify"
            )
        }
        result?.let {
            val text = "Result = ${it.label}"
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = text
            )
        }
    }
}