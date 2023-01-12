/**
 * Copyright @marcosscarpim.
 */

package com.scarpim.digitclassifier

import android.annotation.SuppressLint
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
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
import androidx.compose.ui.unit.dp
import com.scarpim.digitclassifier.classifier.Recognition

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun ScratchPad(viewModel: MainViewModel) {
    val path = remember { mutableStateOf(mutableListOf<Path>()) }
    val result = viewModel.result.collectAsState()
    Scaffold(
        topBar = {
            ComposePaintAppBar{
                path.value = mutableListOf()
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
fun PaintBody(path: MutableState<MutableList<Path>>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.6f)
            .background(Color.White)
    ) {
        val drawColor = Color.Black
        val drawBrush = 100f

        path.value.add(Path())

        DrawingCanvas(
            drawColor,
            drawBrush,
            path.value
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
    val currentPath = path.last()
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