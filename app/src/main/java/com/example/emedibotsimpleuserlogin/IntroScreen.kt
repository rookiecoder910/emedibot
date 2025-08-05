package com.example.emedibotsimpleuserlogin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun IntroScreen(onIntroFinished: () -> Unit) {
    val context = LocalContext.current
    val transitionState = remember { MutableTransitionState(false).apply { targetState = true } }

    // Automatically navigate after delay
    LaunchedEffect(Unit) {
        delay(2500)  // 2.5 seconds
        onIntroFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visibleState = transitionState,
            enter = fadeIn(animationSpec = tween(1000)) + scaleIn(initialScale = 0.7f),
            exit = fadeOut(animationSpec = tween(500))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(id = R.drawable.emedibot),
                    contentDescription = "Logo",
                    modifier = Modifier.size(120.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Welcome to eMediBot",
                    style = MaterialTheme.typography.headlineSmall.copy(color = Color.White)
                )
            }
        }
    }
}
