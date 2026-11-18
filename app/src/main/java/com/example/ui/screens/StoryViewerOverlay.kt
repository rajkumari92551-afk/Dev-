package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.StoryGroup
import com.example.ui.components.DevAvatar
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.PostMediaVisual
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.CobaltBlue
import com.example.ui.theme.ElectricViolet
import kotlinx.coroutines.delay

@Composable
fun StoryViewerOverlay(
    stories: List<StoryGroup>,
    initialGroupId: String,
    onMarkViewed: (String) -> Unit,
    onReply: (StoryGroup, String) -> Unit,
    onClose: () -> Unit
) {
    BackHandler(onBack = onClose)

    var groupIndex by remember(initialGroupId) {
        mutableIntStateOf(stories.indexOfFirst { it.id == initialGroupId }.coerceAtLeast(0))
    }
    var slideIndex by remember(groupIndex) { mutableIntStateOf(0) }
    var progress by remember(groupIndex, slideIndex) { mutableFloatStateOf(0f) }
    var isPaused by remember { mutableStateOf(false) }
    var replyDraft by remember { mutableStateOf("") }

    val currentGroup = stories.getOrNull(groupIndex) ?: run {
        onClose()
        return
    }
    val currentSlide = currentGroup.slides.getOrNull(slideIndex) ?: currentGroup.slides.first()

    LaunchedEffect(currentGroup.id) {
        onMarkViewed(currentGroup.id)
    }

    LaunchedEffect(groupIndex, slideIndex, isPaused, replyDraft.isNotEmpty()) {
        while (progress < 1f) {
            delay(50)
            if (!isPaused && replyDraft.isEmpty()) {
                progress += 0.012f
            }
        }
        // Advance to next slide or next user's story
        if (slideIndex + 1 < currentGroup.slides.size) {
            slideIndex += 1
            progress = 0f
        } else if (groupIndex + 1 < stories.size) {
            groupIndex += 1
            slideIndex = 0
            progress = 0f
        } else {
            onClose()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF040712))
    ) {
        // Backdrop image / gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(currentSlide.gradientStartHex),
                            Color(currentSlide.gradientEndHex)
                        )
                    )
                )
        ) {
            PostMediaVisual(
                drawableRes = currentSlide.drawableRes,
                imageUriString = null,
                artworkSeed = slideIndex,
                contentDescription = currentSlide.title,
                modifier = Modifier.fillMaxSize()
            )
            // Scrims for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.55f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.72f)
                            )
                        )
                    )
            )
        }

        // Tap navigation zones (left previous, right next, long press pause)
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .pointerInput(groupIndex, slideIndex) {
                        detectTapGestures(
                            onPress = {
                                isPaused = true
                                tryAwaitRelease()
                                isPaused = false
                            },
                            onTap = {
                                if (slideIndex > 0) {
                                    slideIndex -= 1
                                    progress = 0f
                                } else if (groupIndex > 0) {
                                    groupIndex -= 1
                                    slideIndex = 0
                                    progress = 0f
                                } else {
                                    progress = 0f
                                }
                            }
                        )
                    }
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .pointerInput(groupIndex, slideIndex) {
                        detectTapGestures(
                            onPress = {
                                isPaused = true
                                tryAwaitRelease()
                                isPaused = false
                            },
                            onTap = {
                                if (slideIndex + 1 < currentGroup.slides.size) {
                                    slideIndex += 1
                                    progress = 0f
                                } else if (groupIndex + 1 < stories.size) {
                                    groupIndex += 1
                                    slideIndex = 0
                                    progress = 0f
                                } else {
                                    onClose()
                                }
                            }
                        )
                    }
            )
        }

        // Top Progress + Author Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                currentGroup.slides.forEachIndexed { idx, _ ->
                    val segProgress = when {
                        idx < slideIndex -> 1f
                        idx == slideIndex -> progress.coerceIn(0f, 1f)
                        else -> 0f
                    }
                    LinearProgressIndicator(
                        progress = { segProgress },
                        modifier = Modifier
                            .weight(1f)
                            .height(3.5.dp)
                            .clip(CircleShape),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.28f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DevAvatar(user = currentGroup.user, size = 40.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = currentGroup.user.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White
                        )
                        if (currentGroup.user.isVerified) VerifiedBadge()
                    }
                    Text(
                        text = currentSlide.timestamp,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.78f)
                    )
                }

                if (isPaused) {
                    Icon(
                        imageVector = Icons.Rounded.Pause,
                        contentDescription = "Story paused",
                        tint = Color.White,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(20.dp)
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .testTag("close_story_button")
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.16f))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close story",
                        tint = Color.White
                    )
                }
            }
        }

        // Bottom Story Caption + Quick Reply Bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(16.dp)
        ) {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = currentSlide.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentSlide.caption,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.92f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = replyDraft,
                    onValueChange = { replyDraft = it },
                    placeholder = {
                        Text(
                            text = "Reply to ${currentGroup.user.name.substringBefore(" ")}...",
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(26.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White.copy(alpha = 0.8f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
                        focusedContainerColor = Color.Black.copy(alpha = 0.35f),
                        unfocusedContainerColor = Color.Black.copy(alpha = 0.28f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("story_reply_input")
                )

                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(50.dp)
                        .testTag("send_story_reply_button")
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(CobaltBlue, ElectricViolet)))
                        .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                        .clickable(enabled = replyDraft.isNotBlank()) {
                            onReply(currentGroup, replyDraft)
                            replyDraft = ""
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "Send story reply",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
