package org.niaz.maximin.mvi

import org.niaz.maximin.R

data class NumbersState(
    val numbers: List<Int?> = ArrayList(),
    val sum: Int = 0,
    val titleId:Int = R.string.scores1,
    val isThinking:Boolean = false,
    val levelsCompleted:Boolean = false
)