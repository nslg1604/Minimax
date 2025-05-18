package org.niaz.maximin.ui

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import org.niaz.maximin.mvi.NumbersIntent
import dagger.hilt.android.AndroidEntryPoint
import org.niaz.maximin.data.MyConst
import org.niaz.maximin.mvi.MainViewModel
import org.niaz.maximin.ui.theme.MyTheme
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.initGame(intent.getBooleanExtra(MyConst.KEY_NEW_GAME, false))

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        Timber.d("MainActivity created")

        setContent {
            MyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NumbersScreen(viewModel)
                }
            }
        }

        if (savedInstanceState == null) {
            viewModel.processIntent(NumbersIntent.LoadRandomNumbers)
        }
    }

}