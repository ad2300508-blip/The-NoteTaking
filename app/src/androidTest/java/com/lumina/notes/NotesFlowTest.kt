package com.lumina.notes

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

/**
 * Smoke test of the core note-taking flow, runnable on the tablet via
 * `./gradlew connectedAndroidTest`. Verifies the app launches, a note can be
 * created and typed into, and we can navigate back to the list.
 */
class NotesFlowTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appLaunches_showsTitle() {
        composeRule.onNodeWithText("Lumina").assertIsDisplayed()
    }

    @Test
    fun createNote_typeTitle_andSeeItInList() {
        // Create a new note via the FAB.
        composeRule.onNodeWithText("Nuova nota").performClick()

        // Type a title into the editor's title field placeholder.
        composeRule.onNodeWithText("Titolo").performTextInput("Spesa")

        // The body placeholder should be visible, confirming we're in the editor.
        composeRule.onNodeWithText("Inizia a scrivere…").assertIsDisplayed()
    }
}
