/**
 * Copyright @marcosscarpim.
 */

package com.scarpim.digitclassifier

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.scarpim.digitclassifier.classifier.Classifier
import com.scarpim.digitclassifier.classifier.Recognition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class MainViewModel(application: Application): AndroidViewModel(application) {

    private var classifier: Classifier? = null

    private val _result: MutableStateFlow<Recognition?> = MutableStateFlow(null)
    val result: StateFlow<Recognition?>
        get() = _result

    fun initModel() {
        if (classifier == null) {
            viewModelScope.launch {
                try {
                    classifier = Classifier(getApplication())
                    Log.v("MarcosLog", "Classifier initialized")
                } catch (e: IOException) {
                    Log.e("MarcosLog", "init(): Failed to create Classifier", e)
                }
            }
        }
    }

    fun classify(bitmap: Bitmap) {
        viewModelScope.launch {
            // TODO HARDCODED values knowing the size of my model. This is not ideal...
            val scaled = bitmap.scaleBitmap(28, 28)
            _result.emit(classifier?.classify(scaled))
        }
    }

    override fun onCleared() {
        super.onCleared()
        classifier?.close()
    }
}

fun Bitmap.scaleBitmap(width: Int, height: Int): Bitmap {
    val scaledBitmap = Bitmap.createScaledBitmap(this, width, height, false)
    this.recycle()
    return scaledBitmap
}
