package com.task.ibiproductsapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.task.ibiproductsapp.ui.theme.IbiProductsAppTheme
import com.task.ibiproductsapp.util.DataStoreHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var dataStoreHelper: DataStoreHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkMode by dataStoreHelper.isDarkMode().collectAsStateWithLifecycle(initialValue = false)
            val isLoggedIn by dataStoreHelper.isLoggedIn().collectAsStateWithLifecycle(initialValue = false)

            IbiProductsAppTheme(darkTheme = isDarkMode) {
                AppNavGraph(isLoggedIn = isLoggedIn)
            }
        }
    }
}

