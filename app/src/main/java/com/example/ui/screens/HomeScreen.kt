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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
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
import com.example.ui.DevSocialUiState
import com.example.ui.components.DevAvatar
import com.example.ui.components.DevSocialLogo
import com.example.ui.components.GlassIconBadgeButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassPill
import com.example.ui.components.LiquidGlassTopBar
import com.example.ui.components.PostCard
import com.example.ui.theme.CobaltBlue
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.LocalLiquidGlassColors

@Composable
fun HomeScreen(
    state: DevSocialUiState,
    bottomPadding: androidx.compose.ui.unit.Dp,
    onOpenFriends: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenMessenger: () -> Unit,
    onOpenStory: (String) -> Unit,
    onOpenCreate: () -> Unit,
    onRefreshFeed: (Boolean) -> Unit,
    onDismissError: () -> Unit,
    onToggleLike: (String) -> Unit,
    onOpenComments: (String) -> Unit,
    onOpenShare: (String) -> Unit,
    onToggleBookmark: (String) -> Unit,
    onOpenProfile: () -> Unit
) {
    val glass = LocalLiquidGlassColors.current
    var selectedFilter by remember { mutableStateOf("For You") }
    val filters = listOf("For You", "Following", "UI/UX", "Kotlin", "Systems")

    val filteredPosts = remember(state.posts, selectedFilter, state.friends, state.currentUser) {
        when (selectedFilter) {
            "For You" -> state.posts
            "Following" -> {
                val friendIds = state.friends.map { it.id }.toSet() + state.currentUser.id
                state.posts.filter { it.author.id in friendIds }
            }
            else -> state.posts.filter { it.category.equals(selectedFilter, ignoreCase = true) }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Frosted Glass Top Navigation Header
        LiquidGlassTopBar {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DevSocialLogo()

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassIconBadgeButton(
                        icon = Icons.Rounded.Group,
                        contentDescription = "Friends and requests",
                        badgeCount = state.friendRequestsCount,
                        testTag = "top_friends_button",
                        onClick = onOpenFriends
                    )
                    GlassIconBadgeButton(
                        icon = Icons.Rounded.Notifications,
                        contentDescription = "Notifications",
                        badgeCount = state.unreadNotificationsCount,
                        testTag = "top_notifications_button",
                        onClick = onOpenNotifications
                    )
                    GlassIconBadgeButton(
                        icon = Icons.Rounded.Forum,
                        contentDescription = "Messenger",
                        badgeCount = state.unreadMessagesCount,
                        testTag = "top_messenger_button",
                        onClick = onOpenMessenger
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 6.dp,
                bottom = bottomPadding + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stories Strip
            item(key = "stories_strip") {
                LiquidGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 640.dp),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 14.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Stories",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${state.stories.count { !it.isViewed }} new",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Your Story / Quick Create
                            item(key = "your_story") {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("your_story_button")
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable(onClick = onOpenCreate)
                                        .padding(4.dp)
                                ) {
                                    Box(contentAlignment = Alignment.BottomEnd) {
                                        DevAvatar(user = state.currentUser, size = 58.dp)
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.linearGradient(listOf(CobaltBlue, ElectricViolet))
                                                )
                                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Add,
                                                contentDescription = "Add story or post",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "You",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            items(state.stories, key = { it.id }) { storyGroup ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("story_item_${storyGroup.id}")
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { onOpenStory(storyGroup.id) }
                                        .padding(4.dp)
                                ) {
                                    DevAvatar(
                                        user = storyGroup.user,
                                        size = 58.dp,
                                        showStoryRing = true,
                                        storyViewed = storyGroup.isViewed
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = storyGroup.user.name.substringBefore(" "),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (storyGroup.isViewed) {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Filter Pills + Refresh Action
            item(key = "feed_controls") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 640.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filters) { filter ->
                            val selected = selectedFilter == filter
                            LiquidGlassPill(
                                selected = selected,
                                onClick = { selectedFilter = filter },
                                modifier = Modifier.testTag("feed_filter_$filter")
                            ) {
                                Text(
                                    text = filter,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(48.dp)
                            .testTag("refresh_feed_button")
                            .clip(CircleShape)
                            .background(glass.glassSurfaceStrong)
                            .border(1.dp, glass.glassBorderTop, CircleShape)
                            .clickable { onRefreshFeed(false) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.isFeedRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Refresh feed",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Loading Shimmer Banner
            if (state.isFeedRefreshing) {
                item(key = "loading_state") {
                    LiquidGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 640.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.4.dp
                            )
                            Text(
                                text = "Syncing latest creator posts...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Error Handling Banner
            if (state.feedError != null) {
                item(key = "error_state") {
                    LiquidGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 640.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = state.feedError,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = onDismissError) {
                                Text("Dismiss")
                            }
                            Button(
                                onClick = { onRefreshFeed(false) },
                                colors = ButtonDefaults.buttonColors(containerColor = CobaltBlue)
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }

            // Posts Feed
            if (filteredPosts.isEmpty()) {
                item(key = "empty_feed") {
                    LiquidGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 640.dp),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No posts in $selectedFilter yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Switch filters or publish your own build update.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            LiquidGlassPill(selected = true, onClick = onOpenCreate) {
                                Text(
                                    text = "Create Post",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            } else {
                items(filteredPosts, key = { it.id }) { post ->
                    PostCard(
                        post = post,
                        onLikeClick = { onToggleLike(post.id) },
                        onCommentClick = { onOpenComments(post.id) },
                        onShareClick = { onOpenShare(post.id) },
                        onBookmarkClick = { onToggleBookmark(post.id) },
                        onAuthorClick = {
                            if (post.author.id == state.currentUser.id) {
                                onOpenProfile()
                            } else {
                                onOpenFriends()
                            }
                        },
                        modifier = Modifier.widthIn(max = 640.dp)
                    )
                }
            }
        }
    }
}
