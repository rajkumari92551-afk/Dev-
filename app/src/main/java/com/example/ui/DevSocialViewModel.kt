package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DevSocialRepository
import com.example.data.SettingsStore
import com.example.model.AppNotification
import com.example.model.ChatThread
import com.example.model.DevUser
import com.example.model.FriendRequest
import com.example.model.ReelItem
import com.example.model.SocialPost
import com.example.model.StoryGroup
import com.example.model.ThemeMode
import com.example.model.UserSettings
import com.example.ui.components.GlobalErrorHandler
import com.example.ui.components.ToastMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DevSocialUiState(
    val currentUser: DevUser,
    val posts: List<SocialPost>,
    val stories: List<StoryGroup>,
    val reels: List<ReelItem>,
    val friends: List<DevUser>,
    val suggestedUsers: List<DevUser>,
    val friendRequests: List<FriendRequest>,
    val chatThreads: List<ChatThread>,
    val notifications: List<AppNotification>,
    val settings: UserSettings,
    val isFeedRefreshing: Boolean = false,
    val feedError: String? = null,
    val activeStoryGroupId: String? = null,
    val activeCommentsPostId: String? = null,
    val activeCommentsReelId: String? = null,
    val activeSharePostId: String? = null,
    val activeDetailPostId: String? = null
) {
    val unreadNotificationsCount: Int
        get() = notifications.count { !it.isRead } + friendRequests.size

    val unreadMessagesCount: Int
        get() = chatThreads.sumOf { it.unreadCount }

    val friendRequestsCount: Int
        get() = friendRequests.size
}

class DevSocialViewModel(application: Application) : AndroidViewModel(application) {

    private companion object {
        const val TAG = "DevSocialViewModel"
    }

    private val settingsStore = SettingsStore(application.applicationContext)
    private val repository = DevSocialRepository()

    private val isFeedRefreshing = MutableStateFlow(false)
    private val feedError = MutableStateFlow<String?>(null)
    private val activeStoryGroupId = MutableStateFlow<String?>(null)
    private val activeCommentsPostId = MutableStateFlow<String?>(null)
    private val activeCommentsReelId = MutableStateFlow<String?>(null)
    private val activeSharePostId = MutableStateFlow<String?>(null)
    private val activeDetailPostId = MutableStateFlow<String?>(null)

    private val _toastMessages = MutableSharedFlow<ToastMessage>(extraBufferCapacity = 8)
    val toastMessages: SharedFlow<ToastMessage> = _toastMessages.asSharedFlow()

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "Uncaught coroutine exception", throwable)
        emitErrorToast("Something went wrong. Please try again.")
    }

    init {
        launchSafely("Couldn't load saved settings. Using defaults.") {
            settingsStore.settingsFlow.collect { settings ->
                repository.syncCurrentUserProfile(settings.displayName, settings.bio)
            }
        }
    }

    private val coreDataFlow = combine(
        repository.currentUser,
        repository.posts,
        repository.stories,
        repository.reels,
        repository.friends
    ) { currentUser, posts, stories, reels, friends ->
        CoreDataBundle(currentUser, posts, stories, reels, friends)
    }

    private val socialMetaFlow = combine(
        repository.suggestedUsers,
        repository.friendRequests,
        repository.chatThreads,
        repository.notifications,
        settingsStore.settingsFlow
    ) { suggested, requests, threads, notifications, settings ->
        SocialMetaBundle(suggested, requests, threads, notifications, settings)
    }

    private val modalStateFlow = combine(
        isFeedRefreshing,
        feedError,
        activeStoryGroupId,
        activeCommentsPostId,
        activeCommentsReelId
    ) { refreshing, error, storyId, commentPostId, commentReelId ->
        ModalBundle(refreshing, error, storyId, commentPostId, commentReelId)
    }

    val uiState: StateFlow<DevSocialUiState> = combine(
        coreDataFlow,
        socialMetaFlow,
        modalStateFlow,
        activeSharePostId,
        activeDetailPostId
    ) { core, meta, modals, sharePostId, detailPostId ->
        DevSocialUiState(
            currentUser = core.currentUser,
            posts = core.posts,
            stories = core.stories,
            reels = core.reels,
            friends = core.friends,
            suggestedUsers = meta.suggestedUsers,
            friendRequests = meta.friendRequests,
            chatThreads = meta.chatThreads,
            notifications = meta.notifications,
            settings = meta.settings,
            isFeedRefreshing = modals.isFeedRefreshing,
            feedError = modals.feedError,
            activeStoryGroupId = modals.activeStoryGroupId,
            activeCommentsPostId = modals.activeCommentsPostId,
            activeCommentsReelId = modals.activeCommentsReelId,
            activeSharePostId = sharePostId,
            activeDetailPostId = detailPostId
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DevSocialUiState(
            currentUser = repository.currentUser.value,
            posts = repository.posts.value,
            stories = repository.stories.value,
            reels = repository.reels.value,
            friends = repository.friends.value,
            suggestedUsers = repository.suggestedUsers.value,
            friendRequests = repository.friendRequests.value,
            chatThreads = repository.chatThreads.value,
            notifications = repository.notifications.value,
            settings = UserSettings()
        )
    )

    fun refreshFeed(simulateTransientError: Boolean = false) {
        if (isFeedRefreshing.value) return
        launchSafely(
            errorMessage = "Couldn't refresh feed right now. Please try again.",
            onFinally = { isFeedRefreshing.value = false }
        ) {
            isFeedRefreshing.value = true
            feedError.value = null
            delay(650)
            if (simulateTransientError) {
                val msg = "Demo sync paused. Tap retry to reload your Liquid Glass feed."
                feedError.value = msg
                emitErrorToast(msg)
            } else {
                emitInfoToast("Feed refreshed")
            }
        }
    }

    fun dismissFeedError() {
        feedError.value = null
    }

    fun togglePostLike(postId: String) {
        runSafely("Couldn't update like. Please try again.") {
            repository.togglePostLike(postId)
        }
    }

    fun togglePostBookmark(postId: String) {
        runSafely("Couldn't update bookmark. Please try again.") {
            repository.togglePostBookmark(postId)
            val isNowSaved = repository.posts.value.find { it.id == postId }?.isBookmarked == true
            emitInfoToast(if (isNowSaved) "Saved to bookmarks" else "Removed from bookmarks")
        }
    }

    fun openPostComments(postId: String) {
        activeCommentsReelId.value = null
        activeCommentsPostId.value = postId
    }

    fun closePostComments() {
        activeCommentsPostId.value = null
    }

    fun addPostComment(postId: String, text: String) {
        runSafely("Couldn't post your comment. Please try again.") {
            require(text.isNotBlank()) { "Comment cannot be empty" }
            repository.addPostComment(postId, text)
        }
    }

    fun openShareSheet(postId: String) {
        activeSharePostId.value = postId
    }

    fun closeShareSheet() {
        activeSharePostId.value = null
    }

    fun recordPostShare(postId: String, targetLabel: String? = null) {
        runSafely("Couldn't complete share action.") {
            repository.incrementPostShare(postId)
            activeSharePostId.value = null
            if (targetLabel != null) {
                emitInfoToast("Shared with $targetLabel")
            } else {
                emitInfoToast("Post shared")
            }
        }
    }

    fun sendPostToFriend(post: SocialPost, friend: DevUser) {
        runSafely("Couldn't send post to ${friend.name}.") {
            val threadId = repository.openOrCreateThread(friend)
            val preview = post.caption.take(72) + if (post.caption.length > 72) "..." else ""
            repository.sendMessage(threadId, "Shared a post by ${post.author.handle}: \"$preview\"")
            repository.incrementPostShare(post.id)
            activeSharePostId.value = null
            emitInfoToast("Sent post to ${friend.name}")
        }
    }

    fun openPostDetail(postId: String) {
        activeDetailPostId.value = postId
    }

    fun closePostDetail() {
        activeDetailPostId.value = null
    }

    fun createPost(
        caption: String,
        category: String,
        tags: List<String>,
        location: String?,
        drawableRes: Int?,
        imageUriString: String?,
        onComplete: () -> Unit
    ) {
        launchSafely(
            errorMessage = "Couldn't publish your post. Please check your caption and try again.",
            onError = { onComplete() }
        ) {
            require(caption.trim().length >= 3) { "Caption must be at least 3 characters." }
            delay(450)
            repository.createPost(caption, category, tags, location, drawableRes, imageUriString)
            emitInfoToast("Published to DEV SOCIAL feed")
            onComplete()
        }
    }

    fun openStory(storyGroupId: String) {
        runSafely("Couldn't open story.") {
            activeStoryGroupId.value = storyGroupId
            repository.markStoryViewed(storyGroupId)
        }
    }

    fun markStoryViewed(storyGroupId: String) {
        runSafely("Couldn't update story state.") {
            repository.markStoryViewed(storyGroupId)
        }
    }

    fun closeStory() {
        activeStoryGroupId.value = null
    }

    fun replyToStory(storyGroup: StoryGroup, replyText: String) {
        runSafely("Couldn't send story reply.") {
            val clean = replyText.trim()
            if (clean.isEmpty()) return@runSafely
            val threadId = repository.openOrCreateThread(storyGroup.user)
            repository.sendMessage(threadId, "Replied to your story: $clean")
            emitInfoToast("Reply sent to ${storyGroup.user.name}")
        }
    }

    fun toggleReelLike(reelId: String) {
        runSafely("Couldn't update reel like.") {
            repository.toggleReelLike(reelId)
        }
    }

    fun toggleReelBookmark(reelId: String) {
        runSafely("Couldn't update saved reels.") {
            repository.toggleReelBookmark(reelId)
            emitInfoToast("Updated saved reels")
        }
    }

    fun openReelComments(reelId: String) {
        activeCommentsPostId.value = null
        activeCommentsReelId.value = reelId
    }

    fun closeReelComments() {
        activeCommentsReelId.value = null
    }

    fun addReelComment(reelId: String, text: String) {
        runSafely("Couldn't add comment to reel.") {
            repository.addReelComment(reelId, text)
        }
    }

    fun recordReelShare(reelId: String) {
        runSafely("Couldn't share reel.") {
            repository.incrementReelShare(reelId)
            emitInfoToast("Reel link copied to clipboard")
        }
    }

    fun acceptFriendRequest(requestId: String) {
        runSafely("Couldn't accept friend request. Please try again.") {
            val name = repository.friendRequests.value.find { it.id == requestId }?.user?.name
            repository.acceptFriendRequest(requestId)
            emitInfoToast(if (name != null) "Connected with $name" else "Friend request accepted")
        }
    }

    fun declineFriendRequest(requestId: String) {
        runSafely("Couldn't dismiss friend request.") {
            repository.declineFriendRequest(requestId)
            emitInfoToast("Friend request dismissed")
        }
    }

    fun toggleSuggestedFriendRequest(userId: String) {
        runSafely("Couldn't update connection request.") {
            repository.toggleSuggestedFriendRequest(userId)
        }
    }

    fun removeFriend(userId: String) {
        runSafely("Couldn't update friend list.") {
            repository.removeFriend(userId)
            emitInfoToast("Removed from circle")
        }
    }

    fun openOrCreateThread(user: DevUser): String? {
        return try {
            repository.openOrCreateThread(user)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open chat thread for ${user.id}", e)
            emitErrorToast("Couldn't open chat with ${user.name}.")
            null
        }
    }

    fun markThreadRead(threadId: String) {
        runSafely("Couldn't mark conversation read.") {
            repository.markThreadRead(threadId)
        }
    }

    fun sendChatMessage(threadId: String, text: String) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        launchSafely("Message failed to send. Please try again.") {
            repository.sendMessage(threadId, clean)
            delay(900)
            val reply = when {
                clean.contains("glass", ignoreCase = true) || clean.contains("ui", ignoreCase = true) ->
                    "Agreed! The blue-purple specular edge reads super cleanly in dark mode too."
                clean.contains("code", ignoreCase = true) || clean.contains("compose", ignoreCase = true) ->
                    "Nice—send over the snippet whenever you're ready and I'll review it."
                clean.contains("?", ignoreCase = true) ->
                    "Count me in! Let's sync up right after the evening build."
                else ->
                    "Love that! Appreciate the update on DEV SOCIAL."
            }
            repository.sendSimulatedReply(threadId, reply)
        }
    }

    fun markNotificationRead(notificationId: String) {
        runSafely("Couldn't update notification.") {
            repository.markNotificationRead(notificationId)
        }
    }

    fun markAllNotificationsRead() {
        runSafely("Couldn't clear unread notifications.") {
            repository.markAllNotificationsRead()
            emitInfoToast("All notifications marked read")
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        launchSafely("Couldn't save theme preference.") {
            settingsStore.setThemeMode(mode)
                .onFailure { emitErrorToast("Couldn't save theme setting.") }
        }
    }

    fun setHighGlassIntensity(enabled: Boolean) {
        launchSafely("Couldn't save glass intensity setting.") {
            settingsStore.setHighGlassIntensity(enabled)
                .onFailure { emitErrorToast("Couldn't save glass preference.") }
        }
    }

    fun setPrivateAccount(enabled: Boolean) {
        launchSafely("Couldn't update privacy preference.") {
            settingsStore.setPrivateAccount(enabled)
                .onFailure { emitErrorToast("Couldn't save privacy setting.") }
        }
    }

    fun setShowOnlineStatus(enabled: Boolean) {
        launchSafely("Couldn't update active status setting.") {
            settingsStore.setShowOnlineStatus(enabled)
                .onFailure { emitErrorToast("Couldn't save active status setting.") }
        }
    }

    fun setNotifyLikes(enabled: Boolean) {
        launchSafely("Couldn't save notification preference.") {
            settingsStore.setNotifyLikes(enabled)
                .onFailure { emitErrorToast("Couldn't save notification setting.") }
        }
    }

    fun setNotifyComments(enabled: Boolean) {
        launchSafely("Couldn't save notification preference.") {
            settingsStore.setNotifyComments(enabled)
                .onFailure { emitErrorToast("Couldn't save notification setting.") }
        }
    }

    fun setNotifyRequests(enabled: Boolean) {
        launchSafely("Couldn't save notification preference.") {
            settingsStore.setNotifyRequests(enabled)
                .onFailure { emitErrorToast("Couldn't save notification setting.") }
        }
    }

    fun updateProfile(displayName: String, bio: String) {
        launchSafely("Couldn't update your profile.") {
            settingsStore.updateProfile(displayName, bio)
                .onSuccess { emitInfoToast("Profile updated") }
                .onFailure { emitErrorToast("Couldn't save profile changes.") }
        }
    }

    fun resetDemoData() {
        launchSafely("Couldn't reset demo workspace.") {
            settingsStore.resetAll()
                .onFailure { emitErrorToast("Couldn't clear saved preferences.") }
            repository.resetDemoData()
            emitInfoToast("Demo workspace reset")
        }
    }

    fun triggerSimulatedErrorBoundaryTest() {
        GlobalErrorHandler.reportError(
            title = "Simulated Runtime Error",
            message = "This demonstrates DEV SOCIAL's Global Error Boundary catching an unexpected exception without losing your app shell.",
            throwable = IllegalStateException("Demo error boundary test triggered from Settings"),
            canRetry = true
        )
    }

    fun showErrorToast(message: String) = emitErrorToast(message)

    private fun launchSafely(
        errorMessage: String,
        onError: (() -> Unit)? = null,
        onFinally: (() -> Unit)? = null,
        block: suspend () -> Unit
    ) {
        viewModelScope.launch(coroutineExceptionHandler) {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, errorMessage, e)
                emitErrorToast(errorMessage)
                onError?.invoke()
            } finally {
                onFinally?.invoke()
            }
        }
    }

    private inline fun runSafely(errorMessage: String, block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            Log.e(TAG, errorMessage, e)
            emitErrorToast(errorMessage)
        }
    }

    private fun emitInfoToast(message: String) {
        viewModelScope.launch {
            _toastMessages.emit(ToastMessage(text = message, isError = false))
        }
    }

    private fun emitErrorToast(message: String) {
        viewModelScope.launch {
            _toastMessages.emit(ToastMessage(text = message, isError = true))
        }
    }

    private data class CoreDataBundle(
        val currentUser: DevUser,
        val posts: List<SocialPost>,
        val stories: List<StoryGroup>,
        val reels: List<ReelItem>,
        val friends: List<DevUser>
    )

    private data class SocialMetaBundle(
        val suggestedUsers: List<DevUser>,
        val friendRequests: List<FriendRequest>,
        val chatThreads: List<ChatThread>,
        val notifications: List<AppNotification>,
        val settings: UserSettings
    )

    private data class ModalBundle(
        val isFeedRefreshing: Boolean,
        val feedError: String?,
        val activeStoryGroupId: String?,
        val activeCommentsPostId: String?,
        val activeCommentsReelId: String?
    )
}
