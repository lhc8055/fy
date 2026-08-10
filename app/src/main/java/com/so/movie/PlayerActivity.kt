package com.so.movie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.so.movie.ui.theme.SOMovieTheme

class PlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SOMovieTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PlayerActivityContent()
                }
            }
        }
    }
}

@Composable
fun PlayerActivityContent() {
    // 独立播放器页面占位
}

@Preview(showBackground = true)
@Composable
fun PlayerActivityPreview() {
    SOMovieTheme {
        PlayerActivityContent()
    }
}
