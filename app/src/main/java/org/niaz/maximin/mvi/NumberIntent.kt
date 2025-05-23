package org.niaz.maximin.mvi

sealed class NumbersIntent {
    data object LoadRandomNumbers : NumbersIntent()
    data class NumberClicked(val row: Int, val col: Int) : NumbersIntent()
    data object InvalidClick : NumbersIntent()
}