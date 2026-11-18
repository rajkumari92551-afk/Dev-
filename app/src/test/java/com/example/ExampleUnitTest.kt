package com.example

import com.example.data.DevSocialRepository
import com.example.ui.components.GlobalErrorHandler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun repository_supports_likes_comments_posts_friends_and_chat() {
        val repo = DevSocialRepository()
        val firstPost = repo.posts.value.first()
        val initialLikes = firstPost.likesCount

        repo.togglePostLike(firstPost.id)
        assertEquals(initialLikes + 1, repo.posts.value.first().likesCount)

        repo.addPostComment(firstPost.id, "Clean Liquid Glass UI!")
        assertTrue(repo.posts.value.first().comments.any { it.text == "Clean Liquid Glass UI!" })

        val created = repo.createPost(
            caption = "Testing new post creation",
            category = "Kotlin",
            tags = listOf("#DevSocial"),
            location = "Austin, TX",
            drawableRes = null,
            imageUriString = null
        )
        assertEquals(created.id, repo.posts.value.first().id)

        val request = repo.friendRequests.value.first()
        repo.acceptFriendRequest(request.id)
        assertFalse(repo.friendRequests.value.any { it.id == request.id })
        assertTrue(repo.friends.value.any { it.id == request.user.id })

        val threadId = repo.openOrCreateThread(repo.friends.value.first())
        repo.sendMessage(threadId, "Hello from unit test")
        assertTrue(
            repo.chatThreads.value.first { it.id == threadId }.messages.any {
                it.text == "Hello from unit test"
            }
        )
    }

    @Test
    fun global_error_handler_reports_and_clears_runtime_errors() {
        GlobalErrorHandler.clearError()
        assertNull(GlobalErrorHandler.currentError.value)

        GlobalErrorHandler.reportError(
            title = "Network Sync Interrupted",
            message = "Unable to sync feed right now.",
            throwable = IllegalStateException("Timeout")
        )

        val current = GlobalErrorHandler.currentError.value
        assertNotNull(current)
        assertEquals("Network Sync Interrupted", current?.title)
        assertTrue(current?.technicalDetails?.contains("IllegalStateException") == true)

        GlobalErrorHandler.clearError()
        assertNull(GlobalErrorHandler.currentError.value)
    }
}
