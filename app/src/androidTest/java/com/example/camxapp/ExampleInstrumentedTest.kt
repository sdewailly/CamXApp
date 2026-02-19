package com.example.camxapp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun testInvalidContainerNumber_showsError() {
        // Use replaceText to clear the default "XXXX1234567" value
        onView(withId(R.id.container))
            .perform(replaceText("123"), closeSoftKeyboard())

        // Click the button
        onView(withId(R.id.start_activity))
            .perform(click())

        // Check if the error is displayed on the EditText
        onView(withId(R.id.container))
            .check(matches(hasErrorText("Must start with 4 capital letters followed by exactly 7 digits")))
    }

    @Test
    fun testValidContainerNumber_startsActivity2() {
        val validNumber = "ABCD1234567"

        // Use replaceText to clear the default "XXXX1234567" value
        onView(withId(R.id.container))
            .perform(replaceText(validNumber), closeSoftKeyboard())

        // Click the button
        onView(withId(R.id.start_activity))
            .perform(click())

        // Verify that Activity2 was started with the correct extra
        intended(allOf(
            hasComponent(Activity2::class.java.name),
            hasExtra("container", validNumber)
        ))
    }
}
