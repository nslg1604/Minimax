package org.niaz.minimax.mvi

import android.content.Context
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.niaz.minimax.R
import org.niaz.minimax.data.MyData
import org.niaz.minimax.data.MyLevel
import org.niaz.minimax.utils.Computer
import org.niaz.minimax.utils.MyPrefs
import timber.log.Timber
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    @Inject lateinit var myPrefs: MyPrefs

    val NOT_DEFINED = -1
    val MAX_NUMBER = 6

    var newGameSelected = false
    var gameOver = false
    var tableSize = 3
    private val _state = MutableStateFlow(NumbersState())
    val state: StateFlow<NumbersState> = _state.asStateFlow()
    var currentRow: Int = NOT_DEFINED
    var currentColumn: Int = NOT_DEFINED
    var tapAllowed = true
    var gameStarting = true
    var currentLevel = 0
    var wins = 0
    val LEVELS = listOf(
        MyLevel(4, 3), //test
        MyLevel(3, 1), //1
        MyLevel(4, 1), //2
        MyLevel(4, 2),
        MyLevel(5, 1),
        MyLevel(5, 2),
        MyLevel(5, 3),
        MyLevel(5, 3),
    )

    fun processIntent(intent: NumbersIntent) {
        when (intent) {
            is NumbersIntent.LoadRandomNumbers -> loadRandomNumbers()
            is NumbersIntent.NumberClicked -> myClick(intent.row, intent.col)
            is NumbersIntent.InvalidClick -> playSound(R.raw.beep)
        }
    }

    private fun loadRandomNumbers() {
        MyData.achievement = myPrefs.read(myPrefs.MAX_LEVEL)?.toInt() ?: 0
        if (newGameSelected){
            currentLevel = 0
            wins = 0
        }
        else {
            currentLevel = MyData.achievement
        }
        Timber.d("Loading currentLevel=$currentLevel achievement=${MyData.achievement} wins=$wins")
        gameOver = false
        tapAllowed = true
        currentRow = NOT_DEFINED;
        currentColumn = NOT_DEFINED;
        if (wins >= MyData.MAX_WINS){
            if (currentLevel < LEVELS.size - 1) {
                currentLevel += 1
                MyData.achievement = currentLevel
                myPrefs.write(myPrefs.MAX_LEVEL, currentLevel.toString())
                wins = 0
            }
            else {
                Timber.d("GAME OVER - no more levels")
            }
        }
        tableSize = LEVELS[currentLevel].tableSize

        viewModelScope.launch(Dispatchers.IO) {
            var numbers: MutableList<Int> = ArrayList()
            for (num in 0 until tableSize * tableSize) {
                if (MyData.DEBUG_NUMBERS) {
                    numbers.add(num)
                } else {
                    numbers.add(Random.nextInt(1, MAX_NUMBER))
                }
            }

            _state.value = _state.value.copy(
                numbers = numbers,
                titleId = R.string.scores1,
                sum = 0
            )

            Timber.d("Numbers loaded: $numbers")
        }
    }

    private fun myClick(row: Int, col: Int) {
        if (!tapAllowed) {
            Timber.d("myClick - click NOT allowed")
            return
        }
//        Timber.d("Number clicked at position ($row, $col)")
        tapAllowed = false
        currentRow = row
        currentColumn = col
        numberClick(row, col, 1, R.raw.pop1)
        if (isRowNotEmpty()) {
            thinking()
        }
        else {
            processGameOver()
        }
    }

    private fun numberClick(row: Int, col: Int, sign: Int, soundResId: Int) {
        playSound(soundResId)
        val currentState = _state.value
        val currentNumbers = currentState.numbers.toMutableList()
        val indexClicked = row * tableSize + col
        val clickedValue = currentNumbers[indexClicked]
        if (clickedValue == null) {
            Timber.e("numberClick - error clickedValue=null")
            return
        }
        currentNumbers[indexClicked] = null

        val newSum = currentState.sum + (clickedValue * sign)
        var who = "user"
        if (sign < 0) who = "comp"
        Timber.d("click - $who ($row,$col) value=$clickedValue newSum=$newSum")

        _state.value = currentState.copy(
            numbers = currentNumbers,
            sum = newSum,
        )

        Timber.d("Updated sum: ${_state.value.sum}")
    }

    private fun thinking() {
        Timber.d("MainViewModel - thinking")

        viewModelScope.launch(Dispatchers.IO) {
            delay(2000)
            val computer = Computer(LEVELS[currentLevel])
            computer.initTable(numbers = _state.value.numbers);
            computer.calcRecursive(
                currentRow,
                - _state.value.sum
            )
            currentColumn = computer.jSelected

            Timber.d("!!! MainViewModel - thinking result currentCol=" + currentColumn)
            withContext(Dispatchers.Main) {
                computerClick(currentRow, currentColumn)
            }
        }
    }

    private fun computerClick(row: Int, col: Int) {
//        Timber.d("MainViewModel - computerClick - position ($row, $col)")
        numberClick(row, col, -1, R.raw.pop3)
        tapAllowed = true
        if (!isColumnNotEmpty()){
            processGameOver()
        }
    }

    fun playSound(resId: Int) {
        try {
            val mediaPlayer = MediaPlayer.create(context, resId)
            mediaPlayer?.start()
//            Timber.d("Playing sound")
        } catch (e: Exception) {
            Timber.e(e, "Failed to play sound")
        }
    }

    fun isColumnNotEmpty():Boolean{
        var iCellsFound = false
        if (currentColumn >= 0) {
            for (i in 0 until tableSize) {
                if (state.value.numbers[i * tableSize + currentColumn] != null) {
                    iCellsFound = true
                    break
                }
            }
        }
        return iCellsFound
    }

    fun isRowNotEmpty():Boolean{
        var jCellsFound = false
        for (j in 0 until tableSize) {
            if (state.value.numbers[currentRow * tableSize + j] != null) {
                jCellsFound = true
                break
            }
        }
        return jCellsFound
    }

    fun processGameOver(){
        _state.value = _state.value.copy(
            titleId = R.string.scores_gameover,
        )
        if (state.value.sum > 0){
            wins += 1
        }
        else if (state.value.sum < 0){
            wins = 0
        }
        Timber.d("Game over sum=${state.value.sum} wins=$wins")
        gameOver = true
    }

//    fun checkGameOver() {
//        var iCellsFound = false
//        if (currentColumn >= 0) {
//            for (i in 0 until tableSize) {
//                if (state.value.numbers[i * tableSize + currentColumn] != null) {
//                    iCellsFound = true
//                    break
//                }
//            }
//        }
//
//        var jCellsFound = false
//        for (j in 0 until tableSize) {
//            if (state.value.numbers[currentRow * tableSize + j] != null) {
//                jCellsFound = true
//                break
//            }
//        }
//        Timber.d("iCellsFound=$iCellsFound jCellsFound=$jCellsFound")
//        if (!iCellsFound || !jCellsFound) {
//            _state.value = _state.value.copy(
//                titleId = R.string.scores_gameover,
//            )
//            if (state.value.sum > 0){
//                wins += 1
//            }
//            else if (state.value.sum < 0){
//                wins = 0
//            }
//            Timber.d("Game over sum=${state.value.sum} wins=$wins")
//            gameOver = true
//        }
//    }

}