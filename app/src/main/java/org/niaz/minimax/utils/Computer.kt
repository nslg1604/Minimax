package org.niaz.minimax.utils

import org.niaz.minimax.data.MyLevel
import timber.log.Timber

class Computer (val myLevel: MyLevel) {
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
        deepMax = myLevel.deep

        var index = 0
        Timber.d("Computer - initTable tableSize=$tableSize deepMax=$deepMax numbers.size=${numbers.size}")
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
        Timber.d("*** Computer - calcRecursive i1=$i1 sumInput=$sumInput status=$status")
        jSelected = -1
        deep++

        var max = -999
        for (j in 0 until tableSize) {
            val cellJ = table[i1][j]
            if (cellJ == NUMBER_USED) continue
            var sumNew = sumInput + cellJ
            Timber.d("===for j=$j i1=$i1 cell=$cellJ sumNew=$sumNew deep=$deep usedI.size=${usedI.size} savedNum.size=${savedNum.size}")
            addUsed(i1,j)

            var min = 999
            var iMin = -1

            for (i in 0 until tableSize) {
                val cellI = table[i][j]
                if (cellI == NUMBER_USED) continue
                val difference = cellJ - cellI
 //               if (deep == 1) difference = difference * 2
                sumNew = sumInput + difference
//                if (deep == deepMax && nTurn < halfMyTurns) {
//                    sumNew = sumInput + cellJ / 2 - cellI/ 2
//                }
//                Timber.d("-----for j=$j i=$i cell=$cellI sumNew=$sumNew  deep=$deep usedI.size=${usedI.size} savedNum.size=${savedNum.size}\"")

                if (deep < deepMax /*&& sumNew > minNoNextStep*/) {
                    savedMin.add(min)
                    savedIMin.add(iMin)
                    savedJSelected.add(jSelected)

                    addUsed(i, j)
                    sumNew = calcRecursive(i, sumNew)
                    restoreUsed()

                    val k:Int = savedJSelected.size - 1
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
                Timber.d("for i end j=$j i=$i sumNew=$sumNew min=$min iMin=$iMin deep=$deep")
            } //i

            restoreUsed()

            if (iMin < 0) {  //no i - end of game
                Timber.d("game over for j=$j" + " sumNew=" + sumNew)
                min = if (sumNew <= 0) -100
                else 100
            }
            if (min > max) {
                max = min
                jSelected = j
            }
            Timber.d("result j=$j i1=$i1 max=$max iMin=$iMin")
        } //j

        if (jSelected < 0) {  //no j to select
            Timber.d("jSelected<0")
            max = sumInput
        }
        Timber.d("return jSelected=$jSelected max=$max deep=$deep")
        deep--
        return max
    }

    fun restoreUsed(){
        val k: Int = usedI.size - 1
//        Timber.d("restoreUsed" + usedI.get(k) + "," + usedJ.get(k) + " tab=" + savedNum.get(k) + " size=" + usedI.size)
        table[usedI.get(k)][usedJ.get(k)] = savedNum.get(k)

        usedI.removeAt(k)
        usedJ.removeAt(k)
        savedNum.removeAt(k)
    }

    fun addUsed(i: Int, j:Int){
        usedI.add(i)
        usedJ.add(j)
        savedNum.add(table[i][j])
        table[i][j] = NUMBER_USED
//        Timber.d("add after usedI.size=" + usedI.size + " usedJ.size=" + usedJ.size + " savedNum.size=" + savedNum.size)
    }

}