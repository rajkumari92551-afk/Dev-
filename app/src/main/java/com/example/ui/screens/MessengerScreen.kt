package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.ChatThread
import com.example.ui.DevSocialUiState
import com.example.ui.components.DevAvatar
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassPill
import com.example.ui.components.LiquidGlassTopBar
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.CobaltBlue
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.LocalLiquidGlassColors

@Composable
fun MessengerInboxScreen(
    state: DevSocialUiState,
    onBack: () -> Unit,
    onOpenThread: (String) -> Unit
) {
    val glass = LocalLiquidGlassColors.current
    var searchQuery by remember { mutableStateOf("") }

    val filteredThreads = remember(state.chatThreads, searchQuery) {
        val q = searchQuery.trim()
        if (q.isEmpty()) state.chatThreads else state.chatThreads.filter {
            it.participant.name.contains(q, ignoreCase = true) ||
                it.participant.handle.contains(q, ignoreCase = true) ||
                it.messages.any { msg -> msg.text.contains(q, ignoreCase = true) }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LiquidGlassTopBar {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("messenger_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Messenger",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${state.unreadMessagesCount} unread messages",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search conversations...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search chats"
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(22.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CobaltBlue,
                        unfocusedBorderColor = glass.glassBorderBottom,
                        focusedContainerColor = glass.glassSurfaceStrong,
                        unfocusedContainerColor = glass.glassSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 640.dp)
                )
            }

            // Active Now Ribbon
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 640.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(state.friends, key = { it.id }) { friend ->
                        val existingThread = state.chatThreads.find { it.participant.id == friend.id }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    if (existingThread != null) onOpenThread(existingThread.id)
                                }
                                .padding(6.dp)
                        ) {
                            DevAvatar(user = friend, size = 54.dp, showOnlineIndicator = true)
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                text = friend.name.substringBefore(" "),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            items(filteredThreads, key = { it.id }) { thread ->
                ThreadRowCard(
                    thread = thread,
                    onClick = { onOpenThread(thread.id) }
                )
            }
        }
    }
}

@Composable
private fun ThreadRowCard(
    thread: ChatThread,
    onClick: () -> Unit
) {
    val lastMsg = thread.messages.lastOrNull()
    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 640.dp)
            .testTag("chat_thread_${thread.id}"),
        shape = RoundedCornerShape(24.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DevAvatar(
                user = thread.participant,
                size = 52.dp,
                showOnlineIndicator = true
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = thread.participant.name,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = if (thread.unreadCount > 0) FontWeight.Bold else FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (thread.participant.isVerified) VerifiedBadge()
                    }
                    Text(
                        text = lastMsg?.timestamp ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = lastMsg?.text ?: "Say hello on DEV SOCIAL!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (thread.unreadCount > 0) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (thread.unreadCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(CobaltBlue, ElectricViolet))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = thread.unreadCount.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatThreadScreen(
    thread: ChatThread,
    currentUserId: String,
    onBack: () -> Unit,
    onMarkRead: (String) -> Unit,
    onSendMessage: (String, String) -> Unit
) {
    val glass = LocalLiquidGlassColors.current
    var draft by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val quickReplies = listOf("Looks super crisp!", "Let's ship it", "Can we sync today?")

    LaunchedEffect(thread.id) {
        onMarkRead(thread.id)
    }

    LaunchedEffect(thread.messages.size) {
        if (thread.messages.isNotEmpty()) {
            listState.animateScrollToItem(thread.messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        LiquidGlassTopBar {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("chat_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back to Messenger"
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                DevAvatar(
                    user = thread.participant,
                    size = 40.dp,
                    showOnlineIndicator = true
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = thread.participant.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (thread.participant.isVerified) VerifiedBadge()
                    }
                    Text(
                        text = if (thread.participant.isOnline) "Active now • ${thread.participant.role}" else thread.participant.role,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (thread.messages.isEmpty()) {
                item {
                    LiquidGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 540.dp),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Text(
                            text = "Start a private Liquid Glass chat with ${thread.participant.name}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }
            }

            items(thread.messages, key = { it.id }) { msg ->
                val isMine = msg.senderId == currentUserId
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 640.dp),
                    contentAlignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Column(
                        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
                        modifier = Modifier.widthIn(max = 300.dp)
                    ) {
                        val bubbleShape = RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = if (isMine) 20.dp else 6.dp,
                            bottomEnd = if (isMine) 6.dp else 20.dp
                        )

                        Box(
                            modifier = Modifier
                                .clip(bubbleShape)
                                .then(
                                    if (isMine) {
                                        Modifier.background(
                                            Brush.linearGradient(listOf(CobaltBlue, ElectricViolet))
                                        )
                                    } else {
                                        Modifier
                                            .background(glass.incomingBubble)
                                            .border(1.dp, glass.glassBorderTop, bubbleShape)
                                    }
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isMine) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = msg.timestamp,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Quick Reply Chips + Input Composer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickReplies) { quick ->
                    LiquidGlassPill(
                        selected = false,
                        onClick = { onSendMessage(thread.id, quick) }
                    ) {
                        Text(
                            text = quick,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp),
                shape = RoundedCornerShape(28.dp),
                strong = true
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        placeholder = { Text("Message ${thread.participant.name.substringBefore(" ")}...") },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_message_input")
                    )

                    Box(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(46.dp)
                            .testTag("send_chat_message_button")
                            .clip(CircleShape)
                            .background(
                                if (draft.isNotBlank()) {
                                    Brush.linearGradient(listOf(CobaltBlue, ElectricViolet))
                                } else {
                                    Brush.linearGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    )
                                }
                            )
                            .clickable(enabled = draft.isNotBlank()) {
                                onSendMessage(thread.id, draft)
                                draft = ""
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = "Send message",
                            tint = if (draft.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
