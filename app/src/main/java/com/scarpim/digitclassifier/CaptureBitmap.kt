/**
 * Copyright @marcosscarpim.
 */

package com.scarpim.digitclassifier

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.drawToBitmap

@Composable
fun captureBitmap(
    content: @Composable ()->Unit
) : () -> Bitmap // <---- this will return a callback which returns a bitmap
{
    val context = LocalContext.current

    val composeView = remember { ComposeView(context) }

    //callback that convert view to bitmap
    fun captureBitmap() = composeView.drawToBitmap()

    AndroidView(
        factory = {
            composeView.apply {
                setContent {
                    content.invoke() // <--- Content get places in between this code
                }
            }
        }
    )

    return ::captureBitmap // <--- return functional reference of the callback
}