package com.zachupstone.walkanywhere.ui.onboarding

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.zachupstone.walkanywhere.R
import com.zachupstone.walkanywhere.ui.theme.Cyan
import com.zachupstone.walkanywhere.ui.theme.Lime
import com.zachupstone.walkanywhere.ui.theme.WalkAnywhereTheme

data class CarouselItem(
    val id: Int,
    @DrawableRes val imageResId: Int,
    val contentDescription: String,
    val title: String,
    val message: String
)

@Composable
fun OnboardingContainer(closeOnboarding: () -> Unit) {
    val items = remember {
        listOf(
            CarouselItem(0, R.drawable.main, "main", "Getting Started", "Navigate to the routes page"),
            CarouselItem(1, R.drawable.routes, "create", "Create A Route", "Press the + icon to start creating a route"),
            CarouselItem(2, R.drawable.routes, "favourite", "Favourite A Route", "Press the heart icon to select a route to walk"),
        )
    }

    val currentOnboardingScreen = remember { mutableStateOf(0) }

    Dialog(onDismissRequest = closeOnboarding) {
        Surface(shape = RoundedCornerShape(16.dp), tonalElevation = 6.dp) {
            Column(Modifier.padding(12.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        items[currentOnboardingScreen.value].title,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(closeOnboarding) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                Box(Modifier.fillMaxHeight(0.8f)) {
                    Column {
                        Spacer(Modifier.height(4.dp))

                        CarouselScreen(items[currentOnboardingScreen.value])

                        Spacer(Modifier.height(4.dp))
                    }
                    Row(Modifier.fillMaxSize()) {
                        Spacer(Modifier
                            .fillMaxHeight()
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if(currentOnboardingScreen.value > 0) {
                                    currentOnboardingScreen.value--
                                }
                            }
                        )
                        Spacer(Modifier
                            .fillMaxHeight()
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if(currentOnboardingScreen.value < items.size - 1) {
                                    currentOnboardingScreen.value++
                                }
                            }
                        )
                    }
                }

                Row {
                    Spacer(Modifier.weight(1f))
                    if (currentOnboardingScreen.value >= items.size - 1) {
                        Button(
                            onClick = closeOnboarding
                        ) {
                            Text("Close")
                        }
                    } else {
                        Button(
                            onClick = { currentOnboardingScreen.value++ }
                        ) {
                            Text("Next")
                        }
                    }
                    Spacer(Modifier.weight(1f))
                }

                Row(
                    Modifier
                        .padding(8.dp)
                ) {
                    Spacer(Modifier.weight(1f))
                    Row(Modifier.padding(start = 6.dp)) {
                        for (i in 0..<3) {
                            if (i == currentOnboardingScreen.value) {
                                Icon(
                                    imageVector = Icons.Filled.Circle,
                                    contentDescription = "Solid Circle",
                                    tint = Lime,
                                    modifier = Modifier.padding(end = 6.dp)
                                        .size(12.dp)
                                        .wrapContentHeight(align = Alignment.CenterVertically)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Circle,
                                    contentDescription = "Outlined Circle",
                                    tint = Color.Black,
                                    modifier = Modifier.padding(end = 6.dp)
                                        .size(12.dp)
                                        .wrapContentHeight(align = Alignment.CenterVertically)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun CarouselScreen(item: CarouselItem) {
    Column {
        Row(Modifier.padding(10.dp)) {
            Spacer(Modifier.weight(1f))
            Image(
                modifier = Modifier.fillMaxHeight(0.9f),
                painter = painterResource(id = item.imageResId),
                contentDescription = item.contentDescription,
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.weight(1f))
        }

        Text(
            item.message,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(2.dp, 10.dp),
            textAlign = TextAlign.Center
        )
    }

}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    WalkAnywhereTheme {
        OnboardingContainer {}
    }
}