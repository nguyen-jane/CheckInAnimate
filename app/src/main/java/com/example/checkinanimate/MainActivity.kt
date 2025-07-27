package com.example.checkinanimate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import com.example.checkinanimate.ui.theme.CheckInAnimateTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CheckInAnimateTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Checkin(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun Checkin(modifier: Modifier = Modifier) {
    var reactState by remember { mutableStateOf(ReactPosition.Start) }
    var textPosition by remember { mutableStateOf(IntOffset.Zero) }
    var reactPosition by remember { mutableStateOf(IntOffset.Zero) }
    val offsetAnimation: Dp by animateDpAsState(
        if (reactState == ReactPosition.Start) {
            0.dp
        } else {
            with(LocalDensity.current) { (textPosition.y - reactPosition.y).toDp() }
        }
    )
    val offset = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    val localDensity = LocalDensity.current

    val path = android.graphics.Path().apply {
        moveTo(0f, 0f)
        cubicTo(100f, 200f, 300f, 0f, 400f, 200f)
    }
    val pathProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 3000, easing = LinearEasing)
    )
    val pathMeasure = remember { android.graphics.PathMeasure() }
    val currentPosition = remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(pathProgress) {
        pathMeasure.setPath(path, false) // Set the path once
        val pos = FloatArray(2)
        pathMeasure.getPosTan(pathMeasure.length * pathProgress, pos, null)
        currentPosition.value = Offset(pos[0], pos[1])
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(500.dp, 500.dp)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(300.dp, 300.dp)
                    .background(Color.Yellow)
                    .clip(RoundedCornerShape(300.dp))
            )
            Text(
                text = "I want to eat sushi",
                modifier = Modifier.onGloballyPositioned { coordinate ->
                    textPosition = coordinate.positionInRoot().round()
                })
        }
        Box(
            modifier = Modifier.size(200.dp, 200.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .clip(CircleShape)
                    .size(100.dp),
                onClick = {
//                    reactState = when (reactState) {
//                        ReactPosition.Start -> ReactPosition.Finish
//                        ReactPosition.Finish -> ReactPosition.Start
//                    }
                    scope.launch {
                        // First animation
                        offset.animateTo(
                            targetValue = with(localDensity) { (textPosition.y - reactPosition.y).toDp() }.value,
                            animationSpec = tween(1000)
                        )
                        // Second animation after the first
                        alpha.animateTo(
                            0f,
                            animationSpec = tween(500)
                        )
                        offset.snapTo(0f)
                        alpha.snapTo(1f)
                    }
                },
            ) {
                Image(
                    painter = painterResource(id = R.drawable.sushi),
                    contentDescription = null,
                    modifier = Modifier.size(50.dp, 50.dp)
                )
            }

            Image(
                painter = painterResource(id = R.drawable.sushi),
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp, 50.dp)
                    .onGloballyPositioned { coordinate ->
                        reactPosition = coordinate.positionInRoot().round()
                    }
                    .alpha(alpha = alpha.value)
                    .absoluteOffset(x = currentPosition.value.x.dp, y = currentPosition.value.y.dp)
            )
        }
        Text(
            "Text Position: x=${textPosition.x}, y=${textPosition.y} \n" +
                    "React Position: x=${reactPosition.x}, y=${reactPosition.y})"
        )
    }
}

enum class ReactPosition {
    Start, Finish
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CheckInAnimateTheme {
        Checkin()
    }
}
