package com.scarpim.digitclassifier.classifier

data class Recognition(
    val label: Int,
    val confidence: Float,
    val timeCost: Long
)
