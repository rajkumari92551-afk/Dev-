package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.DevUser
import com.example.ui.theme.CobaltBlue
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.LocalLiquidGlassColors
import com.example.ui.theme.OrchidGlow
import com.example.ui.theme.SkyCyan
import com.example.ui.theme.SuccessMint
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LiquidGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val glass = LocalLiquidGlassColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_mesh")
    val drift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14_000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mesh_drift"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Blurred luminous ambient mesh orbs behind glass panels
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .blur(if (glass.highGlassIntensity) 68.dp else 44.dp)
        ) {
            val w = size.width
            val h = size.height
            val orbAlpha = if (glass.isDark) 0.44f else 0.38f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glass.orbBlue.copy(alpha = orbAlpha), Color.Transparent),
                    center = Offset(w * (0.18f + 0.12f * drift), h * 0.14f),
                    radius = w * 0.65f
                ),
                radius = w * 0.65f,
                center = Offset(w * (0.18f + 0.12f * drift), h * 0.14f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glass.orbPurple.copy(alpha = orbAlpha), Color.Transparent),
                    center = Offset(w * (0.86f - 0.14f * drift), h * 0.34f),
                    radius = w * 0.72f
                ),
                radius = w * 0.72f,
                center = Offset(w * (0.86f - 0.14f * drift), h * 0.34f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glass.orbCyan.copy(alpha = orbAlpha * 0.8f), Color.Transparent),
                    center = Offset(w * (0.28f + 0.18f * (1f - drift)), h * 0.78f),
                    radius = w * 0.62f
                ),
                radius = w * 0.62f,
                center = Offset(w * (0.28f + 0.18f * (1f - drift)), h * 0.78f)
            )
        }

        content()
    }
}

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(26.dp),
    strong: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val glass = LocalLiquidGlassColors.current
    val fillColor = if (strong) glass.glassSurfaceStrong else glass.glassSurface
    val borderBrush = Brush.linearGradient(
        colors = listOf(
            glass.glassBorderTop,
            glass.glassBorderBottom,
            glass.glassBorderTop.copy(alpha = 0.25f)
        )
    )

    val surfaceModifier = modifier
        .clip(shape)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)

    Surface(
        modifier = surfaceModifier,
        shape = shape,
        color = fillColor,
        border = BorderStroke(1.1.dp, borderBrush),
        shadowElevation = if (glass.isDark) 8.dp else 4.dp
    ) {
        Box(
            modifier = Modifier.drawBehind {
                // Top inner glass specular reflection
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            glass.glassSheen,
                            Color.Transparent
                        )
                    ),
                    start = Offset(size.width * 0.1f, 1.5f),
                    end = Offset(size.width * 0.9f, 1.5f),
                    strokeWidth = 2.2f
                )
            }
        ) {
            content()
        }
    }
}

@Composable
fun LiquidGlassPill(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val glass = LocalLiquidGlassColors.current
    val bgBrush = if (selected) {
        Brush.horizontalGradient(listOf(CobaltBlue, ElectricViolet))
    } else {
        Brush.horizontalGradient(listOf(glass.glassSurfaceStrong, glass.glassSurface))
    }
    val borderColor = if (selected) {
        Color.White.copy(alpha = 0.55f)
    } else {
        glass.glassBorderTop
    }

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(CircleShape)
            .background(bgBrush)
            .border(1.dp, borderColor, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun LiquidGlassTopBar(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val glass = LocalLiquidGlassColors.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .align(Alignment.Center),
            shape = RoundedCornerShape(24.dp),
            strong = true
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                glass.glassSheen.copy(alpha = 0.16f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun DevSocialLogo(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Original interlocking prism bracket emblem for DEV SOCIAL
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(CobaltBlue, ElectricViolet, SkyCyan)
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(11.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(20.dp)) {
                val stroke = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
                // Left angle <
                drawLine(
                    color = Color.White,
                    start = Offset(size.width * 0.40f, size.height * 0.25f),
                    end = Offset(size.width * 0.15f, size.height * 0.50f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color.White,
                    start = Offset(size.width * 0.15f, size.height * 0.50f),
                    end = Offset(size.width * 0.40f, size.height * 0.75f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
                // Right angle >
                drawLine(
                    color = Color.White,
                    start = Offset(size.width * 0.60f, size.height * 0.25f),
                    end = Offset(size.width * 0.85f, size.height * 0.50f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color.White,
                    start = Offset(size.width * 0.85f, size.height * 0.50f),
                    end = Offset(size.width * 0.60f, size.height * 0.75f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
                // Center glowing pulse dot
                drawCircle(
                    color = Color.White,
                    radius = 2.2.dp.toPx(),
                    center = Offset(size.width * 0.5f, size.height * 0.5f)
                )
            }
        }

        Column {
            Text(
                text = "DEV SOCIAL",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "LIQUID NETWORK",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.4.sp,
                    fontSize = 9.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun GlassIconBadgeButton(
    icon: ImageVector,
    contentDescription: String,
    badgeCount: Int = 0,
    testTag: String,
    onClick: () -> Unit
) {
    val glass = LocalLiquidGlassColors.current
    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .size(48.dp)
            .testTag(testTag)
            .clip(CircleShape)
            .background(glass.glassSurface)
            .border(1.dp, glass.glassBorderTop, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(21.dp)
        )
        if (badgeCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-5).dp, y = 5.dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(CobaltBlue, ElectricViolet)))
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun DevAvatar(
    user: DevUser,
    size: Dp = 44.dp,
    showStoryRing: Boolean = false,
    storyViewed: Boolean = false,
    showOnlineIndicator: Boolean = false,
    modifier: Modifier = Modifier
) {
    val ringPadding = if (showStoryRing) 3.dp else 0.dp
    val ringBrush = when {
        !showStoryRing -> Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
        storyViewed -> Brush.linearGradient(
            listOf(
                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )
        else -> Brush.linearGradient(listOf(SkyCyan, CobaltBlue, OrchidGlow))
    }

    Box(
        modifier = modifier
            .size(size + ringPadding * 2)
            .then(
                if (showStoryRing) Modifier.border(2.2.dp, ringBrush, CircleShape) else Modifier
            )
            .padding(ringPadding)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(Color(user.avatarStartHex), Color(user.avatarEndHex))
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.initials,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.34f).sp
                ),
                color = Color.White
            )
        }

        if (showOnlineIndicator && user.isOnline) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size((size * 0.28f).coerceAtLeast(10.dp))
                    .clip(CircleShape)
                    .background(SuccessMint)
                    .border(1.8.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
        }
    }
}

@Composable
fun VerifiedBadge(modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.Rounded.Verified,
        contentDescription = "Verified developer",
        tint = SkyCyan,
        modifier = modifier.size(15.dp)
    )
}

@Composable
fun PostMediaVisual(
    drawableRes: Int?,
    imageUriString: String?,
    artworkSeed: Int,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    when {
        imageUriString != null -> {
            AsyncImage(
                model = imageUriString,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = modifier
            )
        }
        drawableRes != null -> {
            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = modifier
            )
        }
        else -> {
            // Procedural Liquid Glass geometric artwork for posts without photos
            Canvas(
                modifier = modifier
                    .semantics { this.contentDescription = contentDescription }
                    .background(
                        Brush.linearGradient(
                            when (artworkSeed % 3) {
                                0 -> listOf(Color(0xFF1E3A8A), Color(0xFF6D28D9))
                                1 -> listOf(Color(0xFF0284C7), Color(0xFF4F46E5))
                                else -> listOf(Color(0xFF312E81), Color(0xFF9333EA))
                            }
                        )
                    )
            ) {
                val w = size.width
                val h = size.height
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(SkyCyan.copy(alpha = 0.55f), Color.Transparent)
                    ),
                    radius = w * 0.45f,
                    center = Offset(w * 0.25f, h * 0.3f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(OrchidGlow.copy(alpha = 0.55f), Color.Transparent)
                    ),
                    radius = w * 0.42f,
                    center = Offset(w * 0.78f, h * 0.72f)
                )
                // Floating translucent glass planes
                for (i in 0..2) {
                    val offsetFactor = i * 0.14f
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.13f - i * 0.02f),
                        topLeft = Offset(w * (0.14f + offsetFactor), h * (0.18f + offsetFactor * 0.6f)),
                        size = Size(w * 0.56f, h * 0.52f),
                        cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
                // Orbiting code nodes
                val cx = w * 0.5f
                val cy = h * 0.5f
                for (i in 0..5) {
                    val angle = i * (Math.PI / 3.0)
                    val px = cx + (w * 0.24f * cos(angle)).toFloat()
                    val py = cy + (h * 0.24f * sin(angle)).toFloat()
                    drawCircle(
                        color = Color.White.copy(alpha = 0.75f),
                        radius = 4.dp.toPx(),
                        center = Offset(px, py)
                    )
                }
            }
        }
    }
}

enum class BottomNavTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val testTag: String
) {
    HOME("home", "Home", Icons.Rounded.Home, "nav_home"),
    EXPLORE("explore", "Explore", Icons.Rounded.Explore, "nav_explore"),
    CREATE("create", "Create", Icons.Rounded.Add, "nav_create"),
    REELS("reels", "Reels", Icons.Rounded.PlayCircle, "nav_reels"),
    PROFILE("profile", "Profile", Icons.Rounded.Person, "nav_profile")
}

@Composable
fun LiquidGlassBottomDock(
    currentRoute: String,
    onNavigate: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalLiquidGlassColors.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp),
            shape = RoundedCornerShape(30.dp),
            strong = true
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavTab.entries.forEach { tab ->
                    val isSelected = currentRoute == tab.route
                    if (tab == BottomNavTab.CREATE) {
                        val scale by animateFloatAsState(
                            targetValue = if (isSelected) 1.06f else 1f,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "create_scale"
                        )
                        Box(
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .scale(scale)
                                .height(46.dp)
                                .width(56.dp)
                                .testTag(tab.testTag)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(CobaltBlue, ElectricViolet)
                                    )
                                )
                                .border(1.2.dp, Color.White.copy(alpha = 0.65f), RoundedCornerShape(20.dp))
                                .clickable { onNavigate(tab) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    } else {
                        val tint by animateColorAsState(
                            targetValue = if (isSelected) CobaltBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                            animationSpec = tween(200),
                            label = "tab_tint"
                        )
                        val activeBgColor = if (isSelected) {
                            if (glass.isDark) Color.White.copy(alpha = 0.14f) else CobaltBlue.copy(alpha = 0.12f)
                        } else {
                            Color.Transparent
                        }

                        Column(
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag(tab.testTag)
                                .clip(RoundedCornerShape(20.dp))
                                .background(activeBgColor)
                                .clickable { onNavigate(tab) }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = if (isSelected && glass.isDark) SkyCyan else tint,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected && glass.isDark) SkyCyan else tint,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
