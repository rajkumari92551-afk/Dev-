package com.example.data

import com.example.R
import com.example.model.AppNotification
import com.example.model.ChatMessage
import com.example.model.ChatThread
import com.example.model.Comment
import com.example.model.DevUser
import com.example.model.FriendRequest
import com.example.model.NotificationType
import com.example.model.ReelItem
import com.example.model.SocialPost
import com.example.model.StoryGroup
import com.example.model.StorySlide
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DevSocialRepository {

    private val _currentUser = MutableStateFlow(createDefaultCurrentUser())
    val currentUser: StateFlow<DevUser> = _currentUser.asStateFlow()

    private val _posts = MutableStateFlow(createInitialPosts())
    val posts: StateFlow<List<SocialPost>> = _posts.asStateFlow()

    private val _stories = MutableStateFlow(createInitialStories())
    val stories: StateFlow<List<StoryGroup>> = _stories.asStateFlow()

    private val _reels = MutableStateFlow(createInitialReels())
    val reels: StateFlow<List<ReelItem>> = _reels.asStateFlow()

    private val _friends = MutableStateFlow(createInitialFriends())
    val friends: StateFlow<List<DevUser>> = _friends.asStateFlow()

    private val _suggestedUsers = MutableStateFlow(createInitialSuggestedUsers())
    val suggestedUsers: StateFlow<List<DevUser>> = _suggestedUsers.asStateFlow()

    private val _friendRequests = MutableStateFlow(createInitialFriendRequests())
    val friendRequests: StateFlow<List<FriendRequest>> = _friendRequests.asStateFlow()

    private val _chatThreads = MutableStateFlow(createInitialChatThreads())
    val chatThreads: StateFlow<List<ChatThread>> = _chatThreads.asStateFlow()

    private val _notifications = MutableStateFlow(createInitialNotifications())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    fun syncCurrentUserProfile(displayName: String, bio: String) {
        val initials = displayName
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercaseChar().toString() }
            .ifEmpty { "AR" }

        _currentUser.update {
            it.copy(name = displayName, bio = bio, initials = initials)
        }
        val updatedMe = _currentUser.value
        _posts.update { list ->
            list.map { post ->
                if (post.author.id == updatedMe.id) post.copy(author = updatedMe) else post
            }
        }
    }

    fun togglePostLike(postId: String) {
        _posts.update { list ->
            list.map { post ->
                if (post.id == postId) {
                    val nextLiked = !post.isLiked
                    post.copy(
                        isLiked = nextLiked,
                        likesCount = (post.likesCount + if (nextLiked) 1 else -1).coerceAtLeast(0)
                    )
                } else {
                    post
                }
            }
        }
    }

    fun togglePostBookmark(postId: String) {
        _posts.update { list ->
            list.map { post ->
                if (post.id == postId) post.copy(isBookmarked = !post.isBookmarked) else post
            }
        }
    }

    fun addPostComment(postId: String, text: String) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        val me = _currentUser.value
        val newComment = Comment(
            id = "c_${System.currentTimeMillis()}",
            author = me,
            text = clean,
            timestamp = "Just now",
            likes = 0
        )
        _posts.update { list ->
            list.map { post ->
                if (post.id == postId) {
                    post.copy(comments = post.comments + newComment)
                } else {
                    post
                }
            }
        }
    }

    fun incrementPostShare(postId: String) {
        _posts.update { list ->
            list.map { post ->
                if (post.id == postId) post.copy(sharesCount = post.sharesCount + 1) else post
            }
        }
    }

    fun createPost(
        caption: String,
        category: String,
        tags: List<String>,
        location: String?,
        drawableRes: Int?,
        imageUriString: String?
    ): SocialPost {
        val me = _currentUser.value
        val newPost = SocialPost(
            id = "post_${System.currentTimeMillis()}",
            author = me,
            caption = caption.trim(),
            tags = tags.ifEmpty { listOf("#DevSocial") },
            category = category,
            drawableRes = drawableRes,
            imageUriString = imageUriString,
            artworkSeed = (System.currentTimeMillis() % 5).toInt(),
            location = location?.takeIf { it.isNotBlank() },
            timestamp = "Just now",
            likesCount = 1,
            isLiked = true,
            isBookmarked = false,
            comments = emptyList(),
            sharesCount = 0
        )
        _posts.update { listOf(newPost) + it }
        return newPost
    }

    fun markStoryViewed(storyGroupId: String) {
        _stories.update { groups ->
            groups.map { group ->
                if (group.id == storyGroupId) group.copy(isViewed = true) else group
            }
        }
    }

    fun toggleReelLike(reelId: String) {
        _reels.update { list ->
            list.map { reel ->
                if (reel.id == reelId) {
                    val nextLiked = !reel.isLiked
                    reel.copy(
                        isLiked = nextLiked,
                        likesCount = (reel.likesCount + if (nextLiked) 1 else -1).coerceAtLeast(0)
                    )
                } else {
                    reel
                }
            }
        }
    }

    fun toggleReelBookmark(reelId: String) {
        _reels.update { list ->
            list.map { reel ->
                if (reel.id == reelId) reel.copy(isBookmarked = !reel.isBookmarked) else reel
            }
        }
    }

    fun addReelComment(reelId: String, text: String) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        val newComment = Comment(
            id = "rc_${System.currentTimeMillis()}",
            author = _currentUser.value,
            text = clean,
            timestamp = "Just now"
        )
        _reels.update { list ->
            list.map { reel ->
                if (reel.id == reelId) reel.copy(comments = reel.comments + newComment) else reel
            }
        }
    }

    fun incrementReelShare(reelId: String) {
        _reels.update { list ->
            list.map { reel ->
                if (reel.id == reelId) reel.copy(sharesCount = reel.sharesCount + 1) else reel
            }
        }
    }

    fun acceptFriendRequest(requestId: String) {
        val request = _friendRequests.value.find { it.id == requestId } ?: return
        _friendRequests.update { list -> list.filterNot { it.id == requestId } }
        val acceptedUser = request.user.copy(isFriend = true, requestSent = false)
        _friends.update { list ->
            if (list.any { it.id == acceptedUser.id }) list else listOf(acceptedUser) + list
        }
        _currentUser.update {
            it.copy(
                followersCount = it.followersCount + 1,
                followingCount = it.followingCount + 1
            )
        }
        _notifications.update { list ->
            list.filterNot { it.relatedRequestId == requestId }
        }
        // Ensure a chat thread exists for the newly added friend
        if (_chatThreads.value.none { it.participant.id == acceptedUser.id }) {
            val welcome = ChatThread(
                id = "thread_${acceptedUser.id}",
                participant = acceptedUser,
                messages = listOf(
                    ChatMessage(
                        id = "m_init_${System.currentTimeMillis()}",
                        senderId = acceptedUser.id,
                        text = "Thanks for connecting on DEV SOCIAL! Excited to collaborate.",
                        timestamp = "Just now"
                    )
                ),
                unreadCount = 1
            )
            _chatThreads.update { listOf(welcome) + it }
        }
    }

    fun declineFriendRequest(requestId: String) {
        _friendRequests.update { list -> list.filterNot { it.id == requestId } }
        _notifications.update { list ->
            list.filterNot { it.relatedRequestId == requestId }
        }
    }

    fun toggleSuggestedFriendRequest(userId: String) {
        _suggestedUsers.update { list ->
            list.map { user ->
                if (user.id == userId) user.copy(requestSent = !user.requestSent) else user
            }
        }
    }

    fun removeFriend(userId: String) {
        _friends.update { list -> list.filterNot { it.id == userId } }
        _currentUser.update {
            it.copy(followingCount = (it.followingCount - 1).coerceAtLeast(0))
        }
    }

    fun openOrCreateThread(user: DevUser): String {
        val existing = _chatThreads.value.find { it.participant.id == user.id }
        if (existing != null) {
            markThreadRead(existing.id)
            return existing.id
        }
        val newThread = ChatThread(
            id = "thread_${user.id}",
            participant = user,
            messages = emptyList(),
            unreadCount = 0
        )
        _chatThreads.update { listOf(newThread) + it }
        return newThread.id
    }

    fun markThreadRead(threadId: String) {
        _chatThreads.update { list ->
            list.map { thread ->
                if (thread.id == threadId) thread.copy(unreadCount = 0) else thread
            }
        }
    }

    fun sendMessage(threadId: String, text: String) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        val meId = _currentUser.value.id
        val msg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            senderId = meId,
            text = clean,
            timestamp = "Now"
        )
        _chatThreads.update { threads ->
            threads.map { thread ->
                if (thread.id == threadId) {
                    thread.copy(messages = thread.messages + msg, unreadCount = 0)
                } else {
                    thread
                }
            }
        }
    }

    fun sendSimulatedReply(threadId: String, replyText: String) {
        _chatThreads.update { threads ->
            threads.map { thread ->
                if (thread.id == threadId) {
                    val reply = ChatMessage(
                        id = "reply_${System.currentTimeMillis()}",
                        senderId = thread.participant.id,
                        text = replyText,
                        timestamp = "Now"
                    )
                    thread.copy(messages = thread.messages + reply)
                } else {
                    thread
                }
            }
        }
    }

    fun markNotificationRead(notificationId: String) {
        _notifications.update { list ->
            list.map { if (it.id == notificationId) it.copy(isRead = true) else it }
        }
    }

    fun markAllNotificationsRead() {
        _notifications.update { list ->
            list.map { it.copy(isRead = true) }
        }
    }

    fun resetDemoData() {
        _currentUser.value = createDefaultCurrentUser()
        _posts.value = createInitialPosts()
        _stories.value = createInitialStories()
        _reels.value = createInitialReels()
        _friends.value = createInitialFriends()
        _suggestedUsers.value = createInitialSuggestedUsers()
        _friendRequests.value = createInitialFriendRequests()
        _chatThreads.value = createInitialChatThreads()
        _notifications.value = createInitialNotifications()
    }

    companion object {
        private val maya = DevUser(
            id = "u_maya",
            name = "Maya Lin",
            handle = "@mayalin.ui",
            role = "Design Systems Lead",
            bio = "Refining translucent surfaces, micro-typography, and fluid spatial gestures.",
            initials = "ML",
            avatarStartHex = 0xFF2563EB,
            avatarEndHex = 0xFF38BDF8,
            isVerified = true,
            isOnline = true,
            followersCount = 14200,
            followingCount = 418,
            skills = listOf("Compose", "Design Systems", "Motion"),
            isFriend = true
        )

        private val marcus = DevUser(
            id = "u_marcus",
            name = "Marcus Vance",
            handle = "@mvance_kt",
            role = "Kotlin & Motion Engineer",
            bio = "Building 120fps physics springs and zero-jank feeds for Android.",
            initials = "MV",
            avatarStartHex = 0xFF7C3AED,
            avatarEndHex = 0xFFA855F7,
            isVerified = true,
            isOnline = true,
            followersCount = 8940,
            followingCount = 312,
            skills = listOf("Kotlin", "Coroutines", "Graphics"),
            isFriend = true
        )

        private val sora = DevUser(
            id = "u_sora",
            name = "Sora Takahashi",
            handle = "@sora_tokyo",
            role = "Shader & Spatial Artist",
            bio = "Night photography, AGSL glass shaders, and Tokyo cyber-architecture.",
            initials = "ST",
            avatarStartHex = 0xFF0284C7,
            avatarEndHex = 0xFF7C3AED,
            isVerified = true,
            isOnline = false,
            followersCount = 21500,
            followingCount = 264,
            skills = listOf("AGSL", "Shaders", "Photography"),
            isFriend = true
        )

        private val elena = DevUser(
            id = "u_elena",
            name = "Elena Rostova",
            handle = "@elena_arch",
            role = "Distributed Systems Architect",
            bio = "Zero-trust backends, clean telemetry, and late-night espresso.",
            initials = "ER",
            avatarStartHex = 0xFF4F46E5,
            avatarEndHex = 0xFFEC4899,
            isVerified = true,
            isOnline = true,
            followersCount = 6720,
            followingCount = 195,
            skills = listOf("Architecture", "Security", "Rust"),
            isFriend = true
        )

        private val liam = DevUser(
            id = "u_liam",
            name = "Liam O'Connor",
            handle = "@liam_builds",
            role = "Indie Product Maker",
            bio = "Shipping tactile mobile apps from Dublin. Coffee-powered dev logs.",
            initials = "LO",
            avatarStartHex = 0xFF0EA5E9,
            avatarEndHex = 0xFF10B981,
            isVerified = false,
            isOnline = true,
            followersCount = 3410,
            followingCount = 480,
            skills = listOf("Product", "Android", "UX"),
            isFriend = false
        )

        private val priya = DevUser(
            id = "u_priya",
            name = "Priya Nair",
            handle = "@priya_ml",
            role = "On-Device AI Engineer",
            bio = "CameraX pipelines, vision models, and accessible mobile tooling.",
            initials = "PN",
            avatarStartHex = 0xFF8B5CF6,
            avatarEndHex = 0xFF38BDF8,
            isVerified = true,
            isOnline = false,
            followersCount = 11300,
            followingCount = 289,
            skills = listOf("ML Kit", "CameraX", "Kotlin"),
            isFriend = false
        )

        private val noah = DevUser(
            id = "u_noah",
            name = "Noah Kim",
            handle = "@noah_oss",
            role = "Open Source Maintainer",
            bio = "Maintaining Compose navigation helpers & adaptive layout kits.",
            initials = "NK",
            avatarStartHex = 0xFF3B82F6,
            avatarEndHex = 0xFF6366F1,
            isVerified = true,
            isOnline = true,
            followersCount = 5280,
            followingCount = 340,
            skills = listOf("Open Source", "Adaptive UI", "Testing"),
            isFriend = false
        )

        private fun createDefaultCurrentUser(): DevUser = DevUser(
            id = "u_me",
            name = "Alex Rivera",
            handle = "@arivera_dev",
            role = "Mobile UI Architect",
            bio = "Mobile UI Architect crafting Liquid Glass interfaces, Kotlin Multiplatform tools, and spatial motion.",
            initials = "AR",
            avatarStartHex = 0xFF2563EB,
            avatarEndHex = 0xFF7C3AED,
            isVerified = true,
            isOnline = true,
            followersCount = 2840,
            followingCount = 312,
            skills = listOf("Jetpack Compose", "Liquid Glass", "Kotlin", "UI Motion")
        )

        private fun createInitialStories(): List<StoryGroup> = listOf(
            StoryGroup(
                id = "sg_maya",
                user = maya,
                isViewed = false,
                slides = listOf(
                    StorySlide(
                        id = "ss_1",
                        title = "Liquid Glass Tokens",
                        caption = "Testing specular top-edge highlights across light & dark frost layers.",
                        timestamp = "18m ago",
                        drawableRes = R.drawable.img_post_architecture,
                        gradientStartHex = 0xFF2563EB,
                        gradientEndHex = 0xFF7C3AED
                    ),
                    StorySlide(
                        id = "ss_2",
                        title = "Component Audit",
                        caption = "Every pill button now keeps a crisp 48dp touch target without bulk.",
                        timestamp = "12m ago",
                        drawableRes = R.drawable.img_post_workspace,
                        gradientStartHex = 0xFF38BDF8,
                        gradientEndHex = 0xFF4F46E5
                    )
                )
            ),
            StoryGroup(
                id = "sg_sora",
                user = sora,
                isViewed = false,
                slides = listOf(
                    StorySlide(
                        id = "ss_3",
                        title = "Shibuya After Rain",
                        caption = "Captured reference reflections for our new cobalt-violet shader pass.",
                        timestamp = "42m ago",
                        drawableRes = R.drawable.img_reel_neon_city,
                        gradientStartHex = 0xFF0284C7,
                        gradientEndHex = 0xFFA855F7
                    )
                )
            ),
            StoryGroup(
                id = "sg_marcus",
                user = marcus,
                isViewed = false,
                slides = listOf(
                    StorySlide(
                        id = "ss_4",
                        title = "Night Build Session",
                        caption = "Dialed in spring damping ratios for the bottom glass dock.",
                        timestamp = "1h ago",
                        drawableRes = R.drawable.img_post_workspace,
                        gradientStartHex = 0xFF7C3AED,
                        gradientEndHex = 0xFF2563EB
                    )
                )
            ),
            StoryGroup(
                id = "sg_elena",
                user = elena,
                isViewed = true,
                slides = listOf(
                    StorySlide(
                        id = "ss_5",
                        title = "Morning Architecture",
                        caption = "Zero-trust schema reviews + single-origin pour-over.",
                        timestamp = "3h ago",
                        drawableRes = R.drawable.img_post_coffee_code,
                        gradientStartHex = 0xFF4F46E5,
                        gradientEndHex = 0xFF0EA5E9
                    )
                )
            )
        )

        private fun createInitialPosts(): List<SocialPost> = listOf(
            SocialPost(
                id = "post_1",
                author = maya,
                caption = "Shipping our new Liquid Glass component language today. Balancing frosted translucency with razor-sharp typography makes dark and light modes feel weightless.",
                tags = listOf("#LiquidGlass", "#Compose", "#UIUX"),
                category = "UI/UX",
                drawableRes = R.drawable.img_post_architecture,
                location = "San Francisco, CA",
                timestamp = "14m ago",
                likesCount = 342,
                isLiked = false,
                isBookmarked = true,
                sharesCount = 29,
                comments = listOf(
                    Comment("c1", marcus, "Those top-edge specular borders look unreal in motion.", "9m ago", 14),
                    Comment("c2", sora, "The blue-to-violet refraction balance is spot on!", "4m ago", 7)
                )
            ),
            SocialPost(
                id = "post_2",
                author = marcus,
                caption = "Twilight desk setup dialed in for a long Jetpack Compose sprint. Dual-tone cobalt & violet bias lighting keeps eye strain low while profiling frame times.",
                tags = listOf("#DevSetup", "#Kotlin", "#AndroidDev"),
                category = "Kotlin",
                drawableRes = R.drawable.img_post_workspace,
                location = "Seattle, WA",
                timestamp = "1h ago",
                likesCount = 518,
                isLiked = true,
                isBookmarked = false,
                sharesCount = 46,
                comments = listOf(
                    Comment("c3", elena, "Clean cable routing. What switches are on that board?", "41m ago", 9)
                )
            ),
            SocialPost(
                id = "post_3",
                author = elena,
                caption = "Morning ritual before sketching distributed sync protocols: oat latte, blank grid paper, and zero Slack notifications until 10 AM.",
                tags = listOf("#Systems", "#DeepWork", "#CoffeeCode"),
                category = "Systems",
                drawableRes = R.drawable.img_post_coffee_code,
                location = "Zurich, CH",
                timestamp = "3h ago",
                likesCount = 276,
                isLiked = false,
                isBookmarked = false,
                sharesCount = 18,
                comments = listOf(
                    Comment("c4", maya, "Analog grid notebooks still beat every diagram tool for v1 drafts.", "2h ago", 12)
                )
            ),
            SocialPost(
                id = "post_4",
                author = sora,
                caption = "Midnight rain reflections in Tokyo—using this palette directly as inspiration for our animated Reels backdrop shaders.",
                tags = listOf("#Photography", "#Shaders", "#Tokyo"),
                category = "Photography",
                drawableRes = R.drawable.img_reel_neon_city,
                location = "Shibuya, Tokyo",
                timestamp = "5h ago",
                likesCount = 894,
                isLiked = true,
                isBookmarked = true,
                sharesCount = 104,
                comments = listOf(
                    Comment("c5", priya, "The cyan bounce light off the pavement is gorgeous.", "4h ago", 21)
                )
            ),
            SocialPost(
                id = "post_5",
                author = createDefaultCurrentUser(),
                caption = "Prototyping DEV SOCIAL's adaptive floating dock in Compose. Keeping every primary action within effortless thumb reach.",
                tags = listOf("#DevSocial", "#OpenSource", "#MobileFirst"),
                category = "Open Source",
                artworkSeed = 2,
                location = "Austin, TX",
                timestamp = "1d ago",
                likesCount = 195,
                isLiked = false,
                isBookmarked = false,
                sharesCount = 15,
                comments = listOf(
                    Comment("c6", noah, "Excited to try the adaptive tablet rail layout too!", "20h ago", 6)
                )
            )
        )

        private fun createInitialReels(): List<ReelItem> = listOf(
            ReelItem(
                id = "reel_1",
                author = sora,
                title = "Neon Rain Shader Loop",
                caption = "Real-time specular reflections & cobalt refraction in 60 lines of Compose Canvas.",
                audioTrack = "Midnight Prism • Synthwave Demo",
                tags = listOf("#Shaders", "#LiquidGlass", "#Tokyo"),
                drawableRes = R.drawable.img_reel_neon_city,
                gradientStartHex = 0xFF0284C7,
                gradientEndHex = 0xFF7C3AED,
                durationSeconds = 16,
                likesCount = 1420,
                sharesCount = 88,
                comments = listOf(
                    Comment("rc1", maya, "Need this breakdown in our next design sync!", "25m ago", 19)
                )
            ),
            ReelItem(
                id = "reel_2",
                author = marcus,
                title = "Spring Physics Micro-Motion",
                caption = "Why overdamped springs kill perceived speed — tuning stiffness for tactile cards.",
                audioTrack = "Pulse Grid • Lo-Fi Studio",
                tags = listOf("#Kotlin", "#Motion", "#AndroidDev"),
                drawableRes = R.drawable.img_post_workspace,
                gradientStartHex = 0xFF2563EB,
                gradientEndHex = 0xFFA855F7,
                durationSeconds = 22,
                likesCount = 964,
                sharesCount = 54,
                comments = listOf(
                    Comment("rc2", elena, "Subtle overshoot on card release feels so crisp.", "1h ago", 11)
                )
            ),
            ReelItem(
                id = "reel_3",
                author = maya,
                title = "Glass Pavilion Light Study",
                caption = "Translucent elevation hierarchy: letting background color breathe through UI panels.",
                audioTrack = "Aurora Drift • Ambient",
                tags = listOf("#DesignSystems", "#GlassUI", "#Architecture"),
                drawableRes = R.drawable.img_post_architecture,
                gradientStartHex = 0xFF4F46E5,
                gradientEndHex = 0xFF38BDF8,
                durationSeconds = 19,
                likesCount = 1185,
                sharesCount = 72,
                comments = listOf()
            )
        )

        private fun createInitialFriends(): List<DevUser> = listOf(maya, marcus, sora, elena)

        private fun createInitialSuggestedUsers(): List<DevUser> = listOf(noah, priya, liam)

        private fun createInitialFriendRequests(): List<FriendRequest> = listOf(
            FriendRequest(
                id = "req_1",
                user = liam,
                mutualCount = 6,
                note = "Saw your Liquid Glass dock demo—would love to swap Compose tips!",
                timestamp = "25m ago"
            ),
            FriendRequest(
                id = "req_2",
                user = priya,
                mutualCount = 11,
                note = "Collaborating on CameraX + Compose media workflows.",
                timestamp = "2h ago"
            )
        )

        private fun createInitialChatThreads(): List<ChatThread> = listOf(
            ChatThread(
                id = "thread_maya",
                participant = maya,
                unreadCount = 2,
                messages = listOf(
                    ChatMessage("m1", "u_maya", "Hey Alex! Did you test the new frost alpha values on dark wallpaper?", "10:14 AM"),
                    ChatMessage("m2", "u_me", "Yes! Raising the top border highlight to 32% white makes it pop cleanly.", "10:16 AM"),
                    ChatMessage("m3", "u_maya", "Amazing. Let's ship that preset in the theme switcher!", "10:18 AM"),
                    ChatMessage("m4", "u_maya", "Also check the story ring gradient when you get a minute.", "10:19 AM")
                )
            ),
            ChatThread(
                id = "thread_marcus",
                participant = marcus,
                unreadCount = 0,
                messages = listOf(
                    ChatMessage("m5", "u_marcus", "Pushed the vertical pager snap tweaks for Reels.", "Yesterday"),
                    ChatMessage("m6", "u_me", "Tested it on my phone—silky smooth!", "Yesterday")
                )
            ),
            ChatThread(
                id = "thread_sora",
                participant = sora,
                unreadCount = 1,
                messages = listOf(
                    ChatMessage("m7", "u_sora", "Sent over the high-res Tokyo neon frame for the Reels feed.", "2d ago")
                )
            )
        )

        private fun createInitialNotifications(): List<AppNotification> = listOf(
            AppNotification(
                id = "n1",
                type = NotificationType.FRIEND_REQUEST,
                actor = liam,
                message = "sent you a friend request.",
                timestamp = "25m ago",
                isRead = false,
                relatedRequestId = "req_1"
            ),
            AppNotification(
                id = "n2",
                type = NotificationType.COMMENT,
                actor = marcus,
                message = "commented on Maya's Liquid Glass post: \"Those top-edge specular borders look unreal\"",
                timestamp = "9m ago",
                isRead = false,
                relatedPostId = "post_1"
            ),
            AppNotification(
                id = "n3",
                type = NotificationType.LIKE,
                actor = sora,
                message = "liked your adaptive dock prototype post.",
                timestamp = "1h ago",
                isRead = false,
                relatedPostId = "post_5"
            ),
            AppNotification(
                id = "n4",
                type = NotificationType.FRIEND_REQUEST,
                actor = priya,
                message = "sent you a friend request.",
                timestamp = "2h ago",
                isRead = true,
                relatedRequestId = "req_2"
            ),
            AppNotification(
                id = "n5",
                type = NotificationType.MENTION,
                actor = elena,
                message = "mentioned you in #Systems: \"@arivera_dev check this sync pattern!\"",
                timestamp = "4h ago",
                isRead = true,
                relatedPostId = "post_3"
            )
        )
    }
}
