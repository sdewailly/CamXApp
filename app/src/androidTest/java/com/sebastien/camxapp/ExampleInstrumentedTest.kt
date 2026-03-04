package com.sebastien.camxapp

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
        onView(withId(R.id.container))
            .perform(replaceText("123"), closeSoftKeyboard())

        onView(withId(R.id.start_activity))
            .perform(click())

        onView(withId(R.id.container))
            .check(matches(hasErrorText("Must start with 4 capital letters followed by exactly 7 digits")))
    }

    @Test
    fun testValidContainerNumber_startsActivity2() {
        val validNumber = "ABCD1234567"

        onView(withId(R.id.container))
            .perform(replaceText(validNumber), closeSoftKeyboard())

        onView(withId(R.id.start_activity))
            .perform(click())

        intended(allOf(
            hasComponent(Activity2::class.java.name),
            hasExtra("container", validNumber)
        ))
    }
}
