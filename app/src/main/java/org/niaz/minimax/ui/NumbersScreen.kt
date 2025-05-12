package org.niaz.minimax.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.niaz.minimax.R
import org.niaz.minimax.mvi.MainViewModel
import org.niaz.minimax.mvi.NumbersIntent
import timber.log.Timber
import androidx.compose.runtime.key
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.core.app.NotificationCompat.Style
import org.niaz.minimax.data.MyData

@Composable
fun NumbersScreen(viewModel: MainViewModel) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.app_name),
            fontSize = 30.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = stringResource(R.string.level, MyData.level, MyData.achievement),
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp),
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )

        Text(
            text = buildAnnotatedString {
                append(stringResource(state.titleId).replace(":", ": "))
                withStyle(
                    style = SpanStyle(
                        color = if (state.sum > 0) colorResource(R.color.positive)
                        else if (state.sum < 0) colorResource(R.color.negative)
                        else colorResource(R.color.zero),
                        fontWeight = FontWeight.Bold
                    ),
                ) {
                    append(state.sum.toString())
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp),
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        NumbersTable(
            viewModel,
            numbers = state.numbers,
            onNumberClick = { row, col ->
                viewModel.processIntent(NumbersIntent.NumberClicked(row, col))
            }
        )
        if (viewModel.gameStarting) {
            viewModel.playSound(R.raw.loading)
            viewModel.gameStarting = false
        }

        Spacer(modifier = Modifier.height(24.dp))
        ContinueButton(viewModel)
    }
}

@Composable
fun NumbersTable(
    viewModel: MainViewModel,
    numbers: List<Int?>,
    onNumberClick: (Int, Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        for (i in 0 until viewModel.tableSize) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                for (j in 0 until viewModel.tableSize) {
                    key(numbers.toString()) {
                        NumberCell(
                            viewModel,
                            number = numbers[i * viewModel.tableSize + j],
                            onNumberClick = { onNumberClick(i, j) },
                            row = i,
                            col = j
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NumberCell(
    viewModel: MainViewModel,
    number: Int?,
    onNumberClick: () -> Unit,
    row: Int,
    col: Int
) {
    val animationType = remember { (row * 5 + col) % 3 }
    val visibleState = remember { MutableTransitionState(true) }
    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(56.dp)
            .aspectRatio(1f)
            .clip(CircleShape)
            .border(
                width = 2.dp,
                color = if ((viewModel.tapAllowed && col == viewModel.currentColumn) ||
                    (!viewModel.tapAllowed && row == viewModel.currentRow)
                ) colorResource(R.color.column)
                else colorResource(
                    R.color.normal
                ),
                shape = CircleShape
            )
            .background(Color.White)
            .clickable(enabled = viewModel.tapAllowed) {
                if ((viewModel.currentRow < 0 ||
                            viewModel.currentColumn == col) && number != null// && !isClicked
                ) {
                    Timber.d("CLICK")
                    visibleState.targetState = false
                    onNumberClick()
                } else {
                    Timber.d("IGNORE")
                    viewModel.processIntent(NumbersIntent.InvalidClick)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (number != null) {
            AnimatedVisibility(
                visibleState = visibleState,
                exit = when (animationType) {
                    0 -> fadeOut(animationSpec = tween(500)) + scaleOut(animationSpec = tween(500))
                    1 -> slideOutVertically(animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeOut()
                    else -> slideOutHorizontally(animationSpec = tween(500)) + fadeOut(
                        animationSpec = tween(
                            300
                        )
                    )
                }
            ) {
                Text(
                    text = number.toString(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ContinueButton(
    viewModel: MainViewModel
) {
    Box(
        modifier = Modifier
            .clickable(
                onClick = {
                    viewModel.playSound(R.raw.loading)
                    viewModel.processIntent(NumbersIntent.LoadRandomNumbers)
                })
            .border(
                width = 1.dp,
                color = Color.Gray,
                shape = RoundedCornerShape(percent = 50)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.finish),
            color = Color.Black
        )
    }
}
