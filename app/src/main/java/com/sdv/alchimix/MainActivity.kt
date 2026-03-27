package com.sdv.alchimix

import android.os.Bundle
import com.sdv.alchimix.View.SplashScreenView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sdv.alchimix.ui.theme.AlchiMixTheme
import com.sdv.alchimix.view.AlchiMixScreen
import com.sdv.alchimix.viewmodel.CocktailViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlchiMixTheme {
                val viewModel: CocktailViewModel = viewModel()

                val isAppReady by viewModel.isAppReady.collectAsStateWithLifecycle()
                val progress by viewModel.initProgress.collectAsStateWithLifecycle()

                if (isAppReady) {
                    AlchiMixScreen(viewModel = viewModel)
                } else {
                    SplashScreenView(progress = progress)
                }
            }
        }
    }
}
