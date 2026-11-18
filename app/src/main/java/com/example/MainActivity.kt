package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.DevSocialViewModel
import com.example.ui.components.BottomNavTab
import com.example.ui.components.CommentsBottomSheet
import com.example.ui.components.GlobalErrorBoundary
import com.example.ui.components.GlobalErrorHandler
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.LiquidGlassBottomDock
import com.example.ui.components.LiquidGlassToastHost
import com.example.ui.components.PostDetailDialog
import com.example.ui.components.SharePostSheet
import com.example.ui.components.ToastMessage
import com.example.ui.screens.ChatThreadScreen
import com.example.ui.screens.CreatePostScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.FriendsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MessengerInboxScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ReelsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StoryViewerOverlay
import com.example.ui.theme.DevSocialTheme
import com.example.ui.theme.LocalLiquidGlassColors
import kotlinx.coroutines.delay

private const val ROUTE_HOME = "home"
private const val ROUTE_EXPLORE = "explore"
private const val ROUTE_CREATE = "create"
private const val ROUTE_REELS = "reels"
private const val ROUTE_PROFILE = "profile"
private const val ROUTE_FRIENDS = "friends"
private const val ROUTE_MESSENGER = "messenger"
private const val ROUTE_CHAT_PREFIX = "chat"
private const val ROUTE_CHAT = "$ROUTE_CHAT_PREFIX/{threadId}"
private const val ROUTE_NOTIFICATIONS = "notifications"
private const val ROUTE_SETTINGS = "settings"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlobalErrorHandler.install()
        enableEdgeToEdge()
        setContent {
            DevSocialApp()
        }
    }
}

@Composable
fun DevSocialApp(viewModel: DevSocialViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var activeToast by remember { mutableStateOf<ToastMessage?>(null) }
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: ROUTE_HOME

    LaunchedEffect(Unit) {
        viewModel.toastMessages.collect { toast ->
            activeToast = toast
        }
    }

    LaunchedEffect(activeToast?.id) {
        if (activeToast != null) {
            delay(3_200)
            activeToast = null
        }
    }

    val topLevelRoutes = remember {
        setOf(ROUTE_HOME, ROUTE_EXPLORE, ROUTE_CREATE, ROUTE_REELS, ROUTE_PROFILE)
    }
    val showPrimaryNav = currentRoute in topLevelRoutes

    val navigateToTab: (BottomNavTab) -> Unit = { tab ->
        navController.navigate(tab.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    DevSocialTheme(
        themeMode = state.settings.themeMode,
        highGlassIntensity = state.settings.highGlassIntensity
    ) {
        val glass = LocalLiquidGlassColors.current
        LiquidGlassBackground {
            GlobalErrorBoundary(
                onRetry = { viewModel.refreshFeed() },
                onRecoverAndReset = {
                    viewModel.resetDemoData()
                    navigateToTab(BottomNavTab.HOME)
                }
            ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useNavigationRail = maxWidth >= 840.dp
                val bottomDockPadding = if (showPrimaryNav && !useNavigationRail) 96.dp else 16.dp

                Row(modifier = Modifier.fillMaxSize()) {
                    if (showPrimaryNav && useNavigationRail) {
                        NavigationRail(
                            containerColor = glass.glassSurfaceStrong,
                            modifier = Modifier
                                .fillMaxHeight()
                                .windowInsetsPadding(WindowInsets.statusBars)
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))
                            BottomNavTab.entries.forEach { tab ->
                                val selected = currentRoute == tab.route
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateToTab(tab) },
                                    icon = {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tab.label
                                        )
                                    },
                                    label = { Text(tab.label) },
                                    modifier = Modifier.testTag("rail_${tab.route}")
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                        NavHost(
                            navController = navController,
                            startDestination = ROUTE_HOME,
                            enterTransition = { fadeIn() },
                            exitTransition = { fadeOut() }
                        ) {
                            composable(ROUTE_HOME) {
                                HomeScreen(
                                    state = state,
                                    bottomPadding = bottomDockPadding,
                                    onOpenFriends = { navController.navigate(ROUTE_FRIENDS) },
                                    onOpenNotifications = { navController.navigate(ROUTE_NOTIFICATIONS) },
                                    onOpenMessenger = { navController.navigate(ROUTE_MESSENGER) },
                                    onOpenStory = { storyId -> viewModel.openStory(storyId) },
                                    onOpenCreate = { navigateToTab(BottomNavTab.CREATE) },
                                    onRefreshFeed = { simError -> viewModel.refreshFeed(simError) },
                                    onDismissError = { viewModel.dismissFeedError() },
                                    onToggleLike = { postId -> viewModel.togglePostLike(postId) },
                                    onOpenComments = { postId -> viewModel.openPostComments(postId) },
                                    onOpenShare = { postId -> viewModel.openShareSheet(postId) },
                                    onToggleBookmark = { postId -> viewModel.togglePostBookmark(postId) },
                                    onOpenProfile = { navigateToTab(BottomNavTab.PROFILE) }
                                )
                            }

                            composable(ROUTE_EXPLORE) {
                                ExploreScreen(
                                    state = state,
                                    bottomPadding = bottomDockPadding,
                                    onOpenPostDetail = { postId -> viewModel.openPostDetail(postId) },
                                    onOpenFriends = { navController.navigate(ROUTE_FRIENDS) }
                                )
                            }

                            composable(ROUTE_CREATE) {
                                CreatePostScreen(
                                    state = state,
                                    bottomPadding = bottomDockPadding,
                                    onPublishPost = { caption, category, tags, location, drawableRes, uriString, onDone ->
                                        viewModel.createPost(
                                            caption = caption,
                                            category = category,
                                            tags = tags,
                                            location = location,
                                            drawableRes = drawableRes,
                                            imageUriString = uriString,
                                            onComplete = onDone
                                        )
                                    },
                                    onPublishedNavigateHome = { navigateToTab(BottomNavTab.HOME) },
                                    onPickPhotoError = { msg -> viewModel.showErrorToast(msg) }
                                )
                            }

                            composable(ROUTE_REELS) {
                                ReelsScreen(
                                    state = state,
                                    bottomPadding = bottomDockPadding,
                                    onToggleLike = { reelId -> viewModel.toggleReelLike(reelId) },
                                    onOpenComments = { reelId -> viewModel.openReelComments(reelId) },
                                    onToggleBookmark = { reelId -> viewModel.toggleReelBookmark(reelId) },
                                    onShareReel = { reelId -> viewModel.recordReelShare(reelId) }
                                )
                            }

                            composable(ROUTE_PROFILE) {
                                ProfileScreen(
                                    state = state,
                                    bottomPadding = bottomDockPadding,
                                    onOpenSettings = { navController.navigate(ROUTE_SETTINGS) },
                                    onOpenFriends = { navController.navigate(ROUTE_FRIENDS) },
                                    onOpenPostDetail = { postId -> viewModel.openPostDetail(postId) },
                                    onUpdateProfile = { name, bio -> viewModel.updateProfile(name, bio) }
                                )
                            }

                            composable(ROUTE_FRIENDS) {
                                FriendsScreen(
                                    state = state,
                                    onBack = { navController.popBackStack() },
                                    onAcceptRequest = { id -> viewModel.acceptFriendRequest(id) },
                                    onDeclineRequest = { id -> viewModel.declineFriendRequest(id) },
                                    onMessageFriend = { friend ->
                                        val threadId = viewModel.openOrCreateThread(friend)
                                        if (threadId != null) {
                                            navController.navigate("$ROUTE_CHAT_PREFIX/$threadId")
                                        }
                                    },
                                    onRemoveFriend = { id -> viewModel.removeFriend(id) },
                                    onToggleSuggestedRequest = { id -> viewModel.toggleSuggestedFriendRequest(id) }
                                )
                            }

                            composable(ROUTE_MESSENGER) {
                                MessengerInboxScreen(
                                    state = state,
                                    onBack = { navController.popBackStack() },
                                    onOpenThread = { threadId ->
                                        navController.navigate("$ROUTE_CHAT_PREFIX/$threadId")
                                    }
                                )
                            }

                            composable(
                                route = ROUTE_CHAT,
                                arguments = listOf(navArgument("threadId") { type = NavType.StringType })
                            ) { entry ->
                                val threadId = entry.arguments?.getString("threadId") ?: ""
                                val thread = state.chatThreads.find { it.id == threadId }
                                if (thread != null) {
                                    ChatThreadScreen(
                                        thread = thread,
                                        currentUserId = state.currentUser.id,
                                        onBack = { navController.popBackStack() },
                                        onMarkRead = { id -> viewModel.markThreadRead(id) },
                                        onSendMessage = { id, text -> viewModel.sendChatMessage(id, text) }
                                    )
                                }
                            }

                            composable(ROUTE_NOTIFICATIONS) {
                                NotificationsScreen(
                                    state = state,
                                    onBack = { navController.popBackStack() },
                                    onMarkRead = { id -> viewModel.markNotificationRead(id) },
                                    onMarkAllRead = { viewModel.markAllNotificationsRead() },
                                    onAcceptRequest = { reqId -> viewModel.acceptFriendRequest(reqId) },
                                    onDeclineRequest = { reqId -> viewModel.declineFriendRequest(reqId) },
                                    onOpenPost = { postId -> viewModel.openPostDetail(postId) }
                                )
                            }

                            composable(ROUTE_SETTINGS) {
                                SettingsScreen(
                                    state = state,
                                    onBack = { navController.popBackStack() },
                                    onSetThemeMode = { mode -> viewModel.setThemeMode(mode) },
                                    onSetGlassIntensity = { enabled -> viewModel.setHighGlassIntensity(enabled) },
                                    onSetPrivateAccount = { enabled -> viewModel.setPrivateAccount(enabled) },
                                    onSetShowOnlineStatus = { enabled -> viewModel.setShowOnlineStatus(enabled) },
                                    onSetNotifyLikes = { enabled -> viewModel.setNotifyLikes(enabled) },
                                    onSetNotifyComments = { enabled -> viewModel.setNotifyComments(enabled) },
                                    onSetNotifyRequests = { enabled -> viewModel.setNotifyRequests(enabled) },
                                    onResetDemoData = { viewModel.resetDemoData() },
                                    onTestErrorBoundary = { viewModel.triggerSimulatedErrorBoundaryTest() }
                                )
                            }
                        }

                        // Floating Liquid Glass Bottom Dock on Compact Screens
                        androidx.compose.animation.AnimatedVisibility(
                            visible = showPrimaryNav && !useNavigationRail,
                            enter = slideInVertically { it } + fadeIn(),
                            exit = slideOutVertically { it } + fadeOut(),
                            modifier = Modifier.align(Alignment.BottomCenter)
                        ) {
                            LiquidGlassBottomDock(
                                currentRoute = currentRoute,
                                onNavigate = navigateToTab
                            )
                        }

                        // Floating Liquid Glass Error / Status Toast Notification
                        LiquidGlassToastHost(
                            toast = activeToast,
                            onDismiss = { activeToast = null },
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                }

                // Story Viewer Full-Screen Overlay
                AnimatedContent(
                    targetState = state.activeStoryGroupId,
                    label = "story_viewer_transition"
                ) { storyGroupId ->
                    if (storyGroupId != null && state.stories.isNotEmpty()) {
                        StoryViewerOverlay(
                            stories = state.stories,
                            initialGroupId = storyGroupId,
                            onMarkViewed = { id -> viewModel.markStoryViewed(id) },
                            onReply = { group, text -> viewModel.replyToStory(group, text) },
                            onClose = { viewModel.closeStory() }
                        )
                    }
                }
            }

            // Post Comments Sheet
            state.activeCommentsPostId?.let { postId ->
                val post = state.posts.find { it.id == postId }
                if (post != null) {
                    CommentsBottomSheet(
                        title = "${post.author.name}'s Post",
                        comments = post.comments,
                        onAddComment = { text -> viewModel.addPostComment(post.id, text) },
                        onDismiss = { viewModel.closePostComments() }
                    )
                }
            }

            // Reel Comments Sheet
            state.activeCommentsReelId?.let { reelId ->
                val reel = state.reels.find { it.id == reelId }
                if (reel != null) {
                    CommentsBottomSheet(
                        title = "${reel.author.name}'s Reel",
                        comments = reel.comments,
                        onAddComment = { text -> viewModel.addReelComment(reel.id, text) },
                        onDismiss = { viewModel.closeReelComments() }
                    )
                }
            }

            // Share Post Sheet
            state.activeSharePostId?.let { postId ->
                val post = state.posts.find { it.id == postId }
                if (post != null) {
                    SharePostSheet(
                        post = post,
                        friends = state.friends,
                        onSendToFriend = { friend -> viewModel.sendPostToFriend(post, friend) },
                        onSystemShareCompleted = { label -> viewModel.recordPostShare(post.id, label) },
                        onShareError = { msg -> viewModel.showErrorToast(msg) },
                        onDismiss = { viewModel.closeShareSheet() }
                    )
                }
            }

            // Post Detail Dialog (from Explore / Profile / Notifications)
            state.activeDetailPostId?.let { postId ->
                val post = state.posts.find { it.id == postId }
                if (post != null) {
                    PostDetailDialog(
                        post = post,
                        onLikeClick = { viewModel.togglePostLike(post.id) },
                        onCommentClick = {
                            viewModel.closePostDetail()
                            viewModel.openPostComments(post.id)
                        },
                        onShareClick = {
                            viewModel.closePostDetail()
                            viewModel.openShareSheet(post.id)
                        },
                        onBookmarkClick = { viewModel.togglePostBookmark(post.id) },
                        onDismiss = { viewModel.closePostDetail() }
                    )
                }
            }
            }
        }
    }
}
