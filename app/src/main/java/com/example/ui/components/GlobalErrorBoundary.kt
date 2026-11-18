package com.example.ui.components

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.CobaltBlue
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.LikeRose
import com.example.ui.theme.LocalLiquidGlassColors
import com.example.ui.theme.SuccessMint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Immutable
data class ToastMessage(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val isError: Boolean = false
)

@Immutable
data class AppRuntimeError(
    val title: String,
    val message: String,
    val technicalDetails: String? = null,
    val canRetry: Boolean = true
)

object GlobalErrorHandler {
    private const val TAG = "DevSocialErrorBoundary"

    private val _currentError = MutableStateFlow<AppRuntimeError?>(null)
    val currentError: StateFlow<AppRuntimeError?> = _currentError.asStateFlow()

    private var isInstalled = false
    private var previousHandler: Thread.UncaughtExceptionHandler? = null

    fun install() {
        if (isInstalled) return
        isInstalled = true
        previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching { Log.e(TAG, "Uncaught exception on thread ${thread.name}", throwable) }
            val isMainThread = thread.name.equals("main", ignoreCase = true)
            reportError(
                title = "Unexpected Application Error",
                message = "DEV SOCIAL caught an unexpected issue so your workspace stays protected.",
                throwable = throwable
            )
            if (isMainThread) {
                previousHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    fun reportError(
        title: String = "Something went wrong",
        message: String = "An unexpected error interrupted this screen. You can retry or restore the demo workspace.",
        throwable: Throwable? = null,
        canRetry: Boolean = true
    ) {
        if (throwable != null) {
            runCatching { Log.e(TAG, "$title: $message", throwable) }
        }
        _currentError.value = AppRuntimeError(
            title = title,
            message = message,
            technicalDetails = throwable?.let { "${it::class.java.simpleName}: ${it.message ?: "No details"}" },
            canRetry = canRetry
        )
    }

    fun clearError() {
        _currentError.value = null
    }
}

@Composable
fun GlobalErrorBoundary(
    onRetry: () -> Unit,
    onRecoverAndReset: () -> Unit,
    content: @Composable () -> Unit
) {
    val activeError by GlobalErrorHandler.currentError.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        content()

        AnimatedVisibility(
            visible = activeError != null,
            enter = fadeIn() + slideInVertically { it / 6 },
            exit = fadeOut() + slideOutVertically { it / 6 }
        ) {
            activeError?.let { error ->
                BackHandler {
                    GlobalErrorHandler.clearError()
                }
                ErrorBoundaryFallbackOverlay(
                    error = error,
                    onRetry = {
                        GlobalErrorHandler.clearError()
                        onRetry()
                    },
                    onRecoverAndReset = {
                        GlobalErrorHandler.clearError()
                        onRecoverAndReset()
                    },
                    onDismiss = { GlobalErrorHandler.clearError() }
                )
            }
        }
    }
}

@Composable
private fun ErrorBoundaryFallbackOverlay(
    error: AppRuntimeError,
    onRetry: () -> Unit,
    onRecoverAndReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val glass = LocalLiquidGlassColors.current
    var showDetails by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = if (glass.isDark) 0.72f else 0.45f))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 460.dp)
                .testTag("global_error_boundary_card"),
            shape = RoundedCornerShape(30.dp),
            strong = true
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(LikeRose.copy(alpha = 0.16f))
                        .border(1.5.dp, LikeRose.copy(alpha = 0.55f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WarningAmber,
                        contentDescription = "Runtime error alert",
                        tint = LikeRose,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = error.title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = error.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                if (!error.technicalDetails.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { showDetails = !showDetails }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (showDetails) "Hide details" else "View diagnostic details",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = if (showDetails) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    AnimatedVisibility(visible = showDetails) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                                .border(1.dp, glass.glassBorderBottom, RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = error.technicalDetails,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                if (error.canRetry) {
                    Button(
                        onClick = onRetry,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = CobaltBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("error_boundary_retry_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Try Again")
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                OutlinedButton(
                    onClick = onRecoverAndReset,
                    shape = CircleShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("error_boundary_reset_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.RestartAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset Workspace & Continue")
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Dismiss",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
fun LiquidGlassToastHost(
    toast: ToastMessage?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalLiquidGlassColors.current
    AnimatedVisibility(
        visible = toast != null,
        enter = fadeIn() + slideInVertically { -it },
        exit = fadeOut() + slideOutVertically { -it },
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        toast?.let { current ->
            val accentColor = if (current.isError) LikeRose else SuccessMint
            val borderBrush = if (current.isError) {
                Brush.linearGradient(listOf(LikeRose.copy(alpha = 0.75f), glass.glassBorderBottom))
            } else {
                Brush.linearGradient(listOf(CobaltBlue.copy(alpha = 0.7f), ElectricViolet.copy(alpha = 0.5f)))
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                Row(
                    modifier = Modifier
                        .widthIn(max = 520.dp)
                        .fillMaxWidth()
                        .testTag(if (current.isError) "error_toast_banner" else "info_toast_banner")
                        .clip(RoundedCornerShape(22.dp))
                        .background(glass.glassSurfaceStrong)
                        .border(1.2.dp, borderBrush, RoundedCornerShape(22.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (current.isError) Icons.Rounded.ErrorOutline else Icons.Rounded.CheckCircle,
                            contentDescription = if (current.isError) "Error notification" else "Status notification",
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = current.text,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Dismiss notification",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
