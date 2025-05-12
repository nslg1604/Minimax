package org.niaz.minimax.mvi

import org.niaz.minimax.R

data class NumbersState(
    val numbers: List<Int?> = ArrayList(),
    val sum: Int = 0,
    val titleId:Int = R.string.scores1
)