package org.niaz.minimax.utils

import org.niaz.minimax.data.MyLevel
import timber.log.Timber

class ComputerMove (val myLevel: MyLevel) {
    var tableSize:Int = 3
    var deepMax: Int = 1
    var deep: Int = 0
    var jSelected: Int = -1
    var nTurn: Int = 0
    var halfMyTurns: Int = 0
    var minNoNextStep: Int = 0 //if sum<min then no next step calculation
    var status: Int = 0
    var NUMBER_USED: Int = -99

    private val usedI = ArrayList<Int>()
    private val usedJ = ArrayList<Int>()
    private val savedNum = ArrayList<Int>()
    private val savedJSelected = ArrayList<Int>()
    private val savedMin = ArrayList<Int>()
    private val savedIMin = ArrayList<Int>()
    var table: Array<IntArray> = Array(myLevel.tableSize) { IntArray(myLevel.tableSize) }

    fun initTable(numbers: List<Int?>){
        tableSize = myLevel.tableSize
        deep = myLevel.deep

        var index = 0
        Timber.d("ComputerMove - initTable tableSize=$tableSize numbers.size=${numbers.size} deep=$deep")
        for (i in 0 until tableSize) {
            var line = ""
            for (j in 0 until tableSize) {
                table[i][j] = numbers[index++] ?: NUMBER_USED
                line += table[i][j].toString() + " "
            }
            Timber.d(line)
        }
    }

    /**
     * Select new cell position
     * Converted from my Java version app
     */
    fun calcRecursive(i1: Int, sumInput: Int): Int {
        Timber.d("=== ComputerMove - calcRecursive i1=$i1 sumInput=$sumInput status=$status")
        jSelected = -1
        deep++

        var max = -999
        var sumNew = sumInput
        for (j in 0 until tableSize) {
            val cellJ = table[i1][j]
            Timber.d("--for j=$j i1=$i1 cell=$cellJ deep=$deep sumNew=$sumNew usedI.size=${usedI.size} savedNum.size=${savedNum.size}")
            if (cellJ == NUMBER_USED) continue
            sumNew = sumInput + cellJ

            addUsed(i1,j)
            table[i1][j] = NUMBER_USED
            var min = 999
            var iMin = -1

            for (i in 0 until tableSize) {
                Timber.d("+for i=$i j=$j usedI.size=" + usedI.size + " usedJ.size=" + usedJ.size + " savedNum.size=" + savedNum.size)
                val cellI = table[i][j]
                if (cellI == NUMBER_USED) continue
                var difference = cellJ - cellI
                if (deep == 1) difference = difference * 2
                sumNew = sumInput + difference
                if (deep == deepMax && nTurn < halfMyTurns) {
                    sumNew = sumInput + cellJ / 2 - table[i][j] / 2
                }

                if (deep < deepMax && sumNew > minNoNextStep) {
                    savedMin.add(min)
                    savedIMin.add(iMin)
                    savedJSelected.add(jSelected)

                    addUsed(i, j)
                    table[i][j] = NUMBER_USED

                    sumNew = calcRecursive(i, sumNew)

                    var k: Int = usedI.size - 1
                    Timber.d("REST " + usedI.get(k) + "," + usedJ.get(k) + " tab=" + savedNum.get(k) + " size=" + usedI.size)
                    table[usedI.get(k)][usedJ.get(k)] = savedNum.get(k)
                    removeUsed(k)

                    k = savedJSelected.size - 1

                    min = savedMin.get(k)
                    iMin = savedIMin.get(k)
                    jSelected = savedJSelected.get(k)

                    savedMin.removeAt(k)
                    savedIMin.removeAt(k)
                    savedJSelected.removeAt(k)
                }

                if (sumNew < min) {
                    min = sumNew
                    iMin = i
                }
                Timber.d("for(i)-end i=" + i + " j=" + j + " sumNew=" + sumNew + " min=" + min + " iMin=" + iMin + " deep=" + deep)
            } //i

            val k: Int = usedI.size - 1
            Timber.d("REST " + usedI.get(k) + "," + usedJ.get(k) + " tab=" + savedNum.get(k) + " size=" + usedI.size)
            table[usedI.get(k)][usedJ.get(k)] = savedNum.get(k)
            removeUsed(k)
//
            if (iMin < 0) {  //no i - end of game
                Timber.d("[iMin<0" + " sumNew=" + sumNew + "]")
                min = if (sumNew >= 0) 500
                else -500
            }
            if (min > max) {
                max = min
                jSelected = j
            }

//            Timber.d("A i1=" + i1 + " j=" + j + " iMin=" + iMin
//                 + " min=" + min + " max=" + max + " sumNew=" + sumNew
//                 + " table[i1][j]=" + table[i1][j]
//                 + " jSelected=" + jSelected + " deep=" + deep)
        } //j

        if (jSelected < 0) {  //no j to select
            Timber.d("jSelected<0")
            max = sumInput
        }
        Timber.d("return max=" + max + " jSelected=" + jSelected + " deep=" + deep)
        deep--
        return jSelected
    }

    fun removeUsed(k: Int){
        usedI.removeAt(k)
        usedJ.removeAt(k)
        savedNum.removeAt(k)
    }

    fun addUsed(i: Int, j:Int){
        usedI.add(i)
        usedJ.add(j)
        savedNum.add(table[i][j])
//        Timber.d("add after usedI.size=" + usedI.size + " usedJ.size=" + usedJ.size + " savedNum.size=" + savedNum.size)
    }

}