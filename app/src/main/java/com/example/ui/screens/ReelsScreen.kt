package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.ReelItem
import com.example.ui.DevSocialUiState
import com.example.ui.components.DevAvatar
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.PostMediaVisual
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.CobaltBlue
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.LikeRose
import com.example.ui.theme.SkyCyan
import kotlinx.coroutines.delay
import kotlin.math.sin

@Composable
fun ReelsScreen(
    state: DevSocialUiState,
    bottomPadding: Dp,
    onToggleLike: (String) -> Unit,
    onOpenComments: (String) -> Unit,
    onToggleBookmark: (String) -> Unit,
    onShareReel: (String) -> Unit
) {
    val reels = state.reels
    val pagerState = rememberPagerState(pageCount = { reels.size })

    VerticalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .testTag("reels_pager")
    ) { page ->
        val reel = reels[page]
        val isCurrentPage = pagerState.currentPage == page
        ReelPage(
            reel = reel,
            isActivePage = isCurrentPage,
            bottomPadding = bottomPadding,
            onToggleLike = { onToggleLike(reel.id) },
            onOpenComments = { onOpenComments(reel.id) },
            onToggleBookmark = { onToggleBookmark(reel.id) },
            onShareReel = { onShareReel(reel.id) }
        )
    }
}

@Composable
private fun ReelPage(
    reel: ReelItem,
    isActivePage: Boolean,
    bottomPadding: Dp,
    onToggleLike: () -> Unit,
    onOpenComments: () -> Unit,
    onToggleBookmark: () -> Unit,
    onShareReel: () -> Unit
) {
    var isPlaying by remember(reel.id) { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var progress by remember(reel.id) { mutableFloatStateOf(0f) }

    LaunchedEffect(isActivePage, isPlaying) {
        if (isActivePage && isPlaying) {
            while (true) {
                delay(60)
                progress += 60f / (reel.durationSeconds * 1000f)
                if (progress >= 1f) progress = 0f
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "reel_motion")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reel_page_${reel.id}")
            .clickable { isPlaying = !isPlaying }
    ) {
        // Backdrop photo + gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(reel.gradientStartHex), Color(reel.gradientEndHex))
                    )
                )
        ) {
            PostMediaVisual(
                drawableRes = reel.drawableRes,
                imageUriString = null,
                artworkSeed = reel.id.hashCode(),
                contentDescription = reel.title,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Real-time animated Liquid Glass refraction waves when playing
        Canvas(modifier = Modifier.fillMaxSize()) {
            val activePhase = if (isPlaying && isActivePage) wavePhase else 0.8f
            val w = size.width
            val h = size.height
            for (layer in 0..2) {
                val path = Path()
                val baseY = h * (0.42f + layer * 0.08f)
                path.moveTo(0f, baseY)
                var x = 0f
                while (x <= w) {
                    val y = baseY + sin((x / w) * 6f + activePhase + layer) * (24f + layer * 10f)
                    path.lineTo(x, y)
                    x += 24f
                }
                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.14f - layer * 0.03f),
                    style = Stroke(width = 2.5.dp.toPx())
                )
            }
        }

        // Readability gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.42f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.78f)
                        )
                    )
                )
        )

        // Top Header Overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LiquidGlassCard(shape = CircleShape) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.GraphicEq,
                        contentDescription = null,
                        tint = SkyCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "DEV REELS",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(48.dp)
                        .testTag("reel_play_pause_button_${reel.id}")
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.42f))
                        .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                        .clickable { isPlaying = !isPlaying },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (isPlaying) "Pause reel" else "Play reel",
                        tint = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(48.dp)
                        .testTag("reel_mute_button_${reel.id}")
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.42f))
                        .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                        .clickable { isMuted = !isMuted },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Rounded.VolumeOff else Icons.Rounded.VolumeUp,
                        contentDescription = if (isMuted) "Unmute audio" else "Mute audio",
                        tint = Color.White
                    )
                }
            }
        }

        // Center Pause Indicator
        AnimatedVisibility(
            visible = !isPlaying,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f))
                    .border(1.5.dp, Color.White.copy(alpha = 0.65f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "Paused - tap to play",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        // Bottom Metadata + Right Action Rail
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = bottomPadding + 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Creator Details
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        DevAvatar(user = reel.author, size = 40.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    text = reel.author.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White
                                )
                                if (reel.author.isVerified) VerifiedBadge()
                            }
                            Text(
                                text = reel.author.handle,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.78f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = reel.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = reel.caption,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.92f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = reel.tags.joinToString(" "),
                        style = MaterialTheme.typography.labelMedium,
                        color = SkyCyan
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.16f))
                            .border(0.8.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isMuted) "Audio muted • ${reel.audioTrack}" else "♪ ${reel.audioTrack}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }

                // Right Floating Action Rail
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ReelActionButton(
                        icon = if (reel.isLiked) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
                        label = reel.likesCount.toString(),
                        tint = if (reel.isLiked) LikeRose else Color.White,
                        contentDescription = "Like reel",
                        testTag = "reel_like_${reel.id}",
                        onClick = onToggleLike
                    )
                    ReelActionButton(
                        icon = Icons.Outlined.ChatBubbleOutline,
                        label = reel.comments.size.toString(),
                        tint = Color.White,
                        contentDescription = "Comment on reel",
                        testTag = "reel_comment_${reel.id}",
                        onClick = onOpenComments
                    )
                    ReelActionButton(
                        icon = if (reel.isBookmarked) Icons.Rounded.Bookmark else Icons.Outlined.BookmarkBorder,
                        label = "Save",
                        tint = if (reel.isBookmarked) SkyCyan else Color.White,
                        contentDescription = "Save reel",
                        testTag = "reel_save_${reel.id}",
                        onClick = onToggleBookmark
                    )
                    ReelActionButton(
                        icon = Icons.Outlined.Share,
                        label = reel.sharesCount.toString(),
                        tint = Color.White,
                        contentDescription = "Share reel",
                        testTag = "reel_share_${reel.id}",
                        onClick = onShareReel
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Reel Playback Progress Bar
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.5.dp)
                    .clip(CircleShape),
                color = SkyCyan,
                trackColor = Color.White.copy(alpha = 0.25f)
            )
        }
    }
}

@Composable
private fun ReelActionButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    contentDescription: String,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .testTag(testTag)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            CobaltBlue.copy(alpha = 0.35f),
                            ElectricViolet.copy(alpha = 0.35f)
                        )
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.42f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White
        )
    }
}
