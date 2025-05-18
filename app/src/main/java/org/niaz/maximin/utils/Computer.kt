package org.niaz.maximin.utils

import org.niaz.maximin.data.MyLevel

class Computer (val myLevel: MyLevel) {
    val BIG = 999
    val SMALL = -999
    var tableSize:Int = 3
    var deepMax: Int = 1
    var deep: Int = 0
    var jSelected: Int = -1
    var NUMBER_USED: Int = -99
    var halfMaxNumber = 0

    var table: Array<IntArray> = Array(myLevel.tableSize) { IntArray(myLevel.tableSize) }

    fun initTable(numbers: List<Int?>){
        tableSize = myLevel.tableSize
        deepMax = myLevel.deep
        halfMaxNumber = -myLevel.maxNumber / 2

        var index = 0
        //Timber.d("Computer - initTable tableSize=$tableSize deepMax=$deepMax numbers.size=${numbers.size}")
        for (i in 0 until tableSize) {
            var line = ""
            for (j in 0 until tableSize) {
                table[i][j] = numbers[index++] ?: NUMBER_USED
                line += table[i][j].toString() + " "
            }
        }
    }

    /**
     * Select new cell position
     * Converted from my Java version app
     */
    fun calcRecursive(i1: Int, j1:Int, sumInput: Int): Int {
        deep++
        table[i1][j1] = NUMBER_USED
        var margin = ""
        for (k in 1 until deep){
            margin += "  "
        }
        //Timber.d("deep=$deep " + margin + "***calcRecursive i1=$i1 sumInput=$sumInput")
        var jSelected = -1

        var max = SMALL
        for (j in 0 until tableSize) {
            val cellJ = table[i1][j]
            if (cellJ == NUMBER_USED) continue
            var sumNew = sumInput + cellJ
            //Timber.d("deep=$deep " + margin + "===for j=$j i1=$i1 cell=$cellJ sumNew=$sumNew usedI.size=${usedI.size} savedNum.size=${savedNum.size}")

            table[i1][j] = NUMBER_USED

            var min = BIG
            var iMin = -1

            for (i in 0 until tableSize) {
                val cellI = table[i][j]
                if (cellI == NUMBER_USED) continue
                var difference = cellJ - cellI

//                if (deep > 1) difference /= deep
                sumNew = sumInput + difference
                if (difference > halfMaxNumber) {
                    //Timber.d("deep=$deep " + margin + "i=$i cell=$cellI sumNew=$sumNew usedI.size=${usedI.size} savedNum.size=${savedNum.size}")
                    if (deep < deepMax /*&& sumNew > minNoNextStep*/) {
                        sumNew = calcRecursive(i, j, sumNew)
                        table[i][j] = cellI
                    }
                }
                else {
                    //Timber.d("deep=$deep " + margin + "i=$i cell=$cellI - SKIP BAD difference=$difference")
                }

                if (sumNew < min) {
                    min = sumNew
                    iMin = i
                }
                //Timber.d("deep=$deep " + margin + "end i=$i sumNew=$sumNew min=$min iMin=$iMin")
            } //i

            table[i1][j] = cellJ

            if (iMin < 0) {  //no i - end of game
                //Timber.d("deep=$deep " + margin + "game over for j=$j" + " sumNew=$sumNew")
                if (sumNew < 0) min = SMALL
                else min = BIG
            }

            if (min >= max) {
                max = min
                jSelected = j
            }
            //Timber.d("deep=$deep " + margin + "result j=$j i1=$i1 max=$max iMin=$iMin")
        } //j

        if (jSelected < 0) {  //no j to select
            //Timber.d("deep=$deep " + margin + "jSelected<0")
            max = sumInput
        }
        //Timber.d("deep=$deep " + margin + "return jSelected=$jSelected max=$max")
        deep--
        this.jSelected = jSelected
        return max
    }
}