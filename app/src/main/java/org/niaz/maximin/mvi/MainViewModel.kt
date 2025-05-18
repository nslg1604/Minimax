package org.niaz.maximin.mvi

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
import org.niaz.maximin.R
import org.niaz.maximin.data.MyConst
import org.niaz.maximin.utils.Computer
import org.niaz.maximin.utils.MyPrefs
import timber.log.Timber
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    @Inject lateinit var myPrefs: MyPrefs

    val NOT_DEFINED = -1

    private val _state = MutableStateFlow(NumbersState())
    val state: StateFlow<NumbersState> = _state.asStateFlow()

    var gameOver = false
    var tableSize = 3
    var currentRow: Int = NOT_DEFINED
    var currentColumn: Int = NOT_DEFINED
    var tapAllowed = true
    var gameStarting = true
    var currentLevel = 0
    var wins = 0

    fun processIntent(intent: NumbersIntent) {
        when (intent) {
            is NumbersIntent.LoadRandomNumbers -> loadRandomNumbers()
            is NumbersIntent.NumberClicked -> myClick(intent.row, intent.col)
            is NumbersIntent.InvalidClick -> playSound(R.raw.beep)
        }
    }

    fun initGame(newGame:Boolean){
        if (newGame){
            currentLevel = 0
            wins = 0
        }
        else {
            currentLevel = myPrefs.read(myPrefs.MAX_LEVEL)?.toInt() ?: 0
            if (currentLevel >= MyConst.LEVELS.size){
                currentLevel = MyConst.LEVELS.size - 1
            }
        }
        Timber.d("InitGame - currentLevel=$currentLevel")
    }

    private fun loadRandomNumbers() {
        Timber.d("Loading currentLevel=$currentLevel wins=$wins")
        gameOver = false
        tapAllowed = true
        currentRow = NOT_DEFINED;
        currentColumn = NOT_DEFINED;
        if (wins >= MyConst.MAX_WINS){
            if (currentLevel < MyConst.LEVELS.size - 1) {
                currentLevel += 1
                myPrefs.write(myPrefs.MAX_LEVEL, currentLevel.toString())
                wins = 0
            }
            else {
                Timber.d("GAME OVER - no more MyConst.LEVELS")
                _state.value = _state.value.copy(
                    levelsCompleted = true
                )
            }
        }
        tableSize = MyConst.LEVELS[currentLevel].tableSize

        viewModelScope.launch(Dispatchers.IO) {
            var numbers: MutableList<Int> = ArrayList()
            for (num in 0 until tableSize * tableSize) {
                if (MyConst.DEBUG_NUMBERS) {
                    numbers.add(num)
                } else {
                    numbers.add(Random.nextInt(1, MyConst.LEVELS[currentLevel].maxNumber + 1))
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

        Timber.d("numberClick - updated sum: ${_state.value.sum}")
    }

    private fun thinking() {
        Timber.d("MainViewModel - thinking")
        _state.value = _state.value.copy(
            isThinking = true
        )


        viewModelScope.launch(Dispatchers.IO) {
            delay(2000)
            val computer = Computer(MyConst.LEVELS[currentLevel])
            computer.initTable(numbers = _state.value.numbers);
            computer.calcRecursive(
                currentRow,
                currentColumn,
                - _state.value.sum
            )
            currentColumn = computer.jSelected

            Timber.d("!!! MainViewModel - thinking result currentCol=" + currentColumn)
            withContext(Dispatchers.Main) {
                computerClick(currentRow, currentColumn)
                _state.value = _state.value.copy(
                    isThinking = false
                )
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
        Timber.d("processGameOver sum=${state.value.sum} wins=$wins")
        gameOver = true
    }

}