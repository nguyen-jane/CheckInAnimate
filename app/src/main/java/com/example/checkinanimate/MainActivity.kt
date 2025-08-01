package com.example.checkinanimate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
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
    var textPosition by remember { mutableStateOf(IntOffset.Zero) }
    var reactPosition by remember { mutableStateOf(IntOffset.Zero) }
    val animatedDistance = remember { Animatable(0f) }
    val offset = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    val localDensity = LocalDensity.current

    Box(modifier = Modifier.fillMaxSize()) {
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
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp),
            contentAlignment = Alignment.Center,
        ) {
            val composePath = Path().apply {
                moveTo(reactPosition.x.toFloat() + 150, reactPosition.y.toFloat() + 150)
                cubicTo(
                    reactPosition.x.toFloat() + 700,
                    reactPosition.y.toFloat() - 500,
                    textPosition.x.toFloat() + 700,
                    textPosition.y.toFloat() - 500,
                    textPosition.x.toFloat(),
                    textPosition.y.toFloat()
                )
            }
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawPath(
                    path = composePath,
                    color = Color.Red,
                    style = Stroke(width = 5f)
                )
            }

            val animatedPathMeasure = PathMeasure()
            animatedPathMeasure.setPath(composePath, false) // Set the path once
            val animatedPathLength = animatedPathMeasure.length
            val position = animatedPathMeasure.getPosition(animatedDistance.value)
            Image(
                painter = painterResource(id = R.drawable.sushi),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .size(50.dp, 50.dp)
//                    .absoluteOffset { IntOffset(position.x.roundToInt(), -position.y.roundToInt()) }
                    //.absoluteOffset(y = offset.value.dp)
                    .absoluteOffset(
                        x = with(localDensity) { (position.x - (reactPosition.x + 150)).toDp() },
                        y = with(localDensity) { (position.y - (reactPosition.y + 150)).toDp() }
                    )
                    .graphicsLayer(alpha = alpha.value)
            )

            Button(
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .clip(CircleShape)
                    .size(100.dp)
                    .align(Alignment.BottomCenter)
                    .onGloballyPositioned { coordinate ->
                        reactPosition = coordinate.positionInRoot().round()
                    },
                onClick = {
                    scope.launch {
                        // First animation
                        animatedDistance.animateTo(
                            targetValue = animatedPathLength,
                            animationSpec = tween(durationMillis = 5000, easing = LinearEasing)
                        )
                        // straight line
//                        offset.animateTo(
//                            targetValue = with(localDensity) { (textPosition.y - reactPosition.y).toDp() }.value,
//                            animationSpec = tween(1000, easing = LinearEasing)
//                        )
                        // Second animation after the first
                        alpha.animateTo(
                            0f,
                            animationSpec = tween(100, easing = LinearEasing)
                        )
                        animatedDistance.snapTo(0f)
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

            Text(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(top = 100.dp),
                text = "ImageX ${position.x - (reactPosition.x + 150)}" +
                        " ImageY ${position.y - (reactPosition.y + 150)}"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CheckInAnimateTheme {
        Checkin()
    }
}
