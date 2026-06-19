package com.task.ibiproductsapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.task.ibiproductsapp.ui.theme.IbiProductsAppTheme
import com.task.ibiproductsapp.util.DataStoreHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var dataStoreHelper: DataStoreHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        var isReady by mutableStateOf(false)
        var isLoggedIn by mutableStateOf(false)

        splashScreen.setKeepOnScreenCondition { !isReady }

        lifecycleScope.launch {
            isLoggedIn = dataStoreHelper.isLoggedIn().first()
            isReady = true
        }

        enableEdgeToEdge()

        setContent {
            if (isReady) {
                val isDarkMode by dataStoreHelper.isDarkMode()
                    .collectAsStateWithLifecycle(initialValue = false)

                IbiProductsAppTheme(darkTheme = isDarkMode) {
                    AppNavGraph(isLoggedIn = isLoggedIn)
                }
            }
        }
    }
}

