package com.example.camxapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.view.MotionEvent
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Activity2Test {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA)

    @Before
    fun setUp() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun testActivity2_isDisplayed() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, Activity2::class.java).apply {
            putExtra("container", "TEST1234567")
        }

        ActivityScenario.launch<Activity2>(intent).use {
            Espresso.onView(withId(R.id.viewFinder)).check(matches(isDisplayed()))
            Espresso.onView(withId(R.id.image_capture_button)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun testActivity2_clickPhotoButton() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, Activity2::class.java).apply {
            putExtra("container", "TEST_SINGLE")
        }

        ActivityScenario.launch<Activity2>(intent).use {
            Espresso.onView(withId(R.id.image_capture_button)).perform(click())
            // Espresso will now automatically wait for the idling resource to be 0
        }
    }

    @Test
    fun testActivity2_longClickBurstButton() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, Activity2::class.java).apply {
            putExtra("container", "TEST_BURST")
        }

        ActivityScenario.launch<Activity2>(intent).use {
            Espresso.onView(withId(R.id.image_capture_button)).perform(longClick())
            // Espresso waits for all 5 photos to be saved
        }
    }

    @Test
    fun testActivity2_pinchToZoom() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, Activity2::class.java).apply {
            putExtra("container", "TEST_ZOOM")
        }

        ActivityScenario.launch<Activity2>(intent).use {
            Espresso.onView(withId(R.id.viewFinder)).perform(simulatePinchOut())
        }
    }

    private fun simulatePinchOut(): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> = isDisplayed()
            override fun getDescription(): String = "Simulate pinch-out gesture"
            override fun perform(uiController: UiController, view: View) {
                val middleX = view.width / 2f
                val middleY = view.height / 2f
                val eventTime = android.os.SystemClock.uptimeMillis()
                val startPointers = arrayOf(
                    MotionEvent.PointerProperties().apply { id = 0; toolType = MotionEvent.TOOL_TYPE_FINGER },
                    MotionEvent.PointerProperties().apply { id = 1; toolType = MotionEvent.TOOL_TYPE_FINGER }
                )
                val startCoords = arrayOf(
                    MotionEvent.PointerCoords().apply { x = middleX - 10f; y = middleY },
                    MotionEvent.PointerCoords().apply { x = middleX + 10f; y = middleY }
                )
                val downEvent = MotionEvent.obtain(eventTime, eventTime, MotionEvent.ACTION_DOWN, 1, startPointers, startCoords, 0, 0, 1f, 1f, 0, 0, 0, 0)
                view.dispatchTouchEvent(downEvent)
                val pointerDownEvent = MotionEvent.obtain(eventTime, eventTime, MotionEvent.ACTION_POINTER_DOWN or (1 shl MotionEvent.ACTION_POINTER_INDEX_SHIFT), 2, startPointers, startCoords, 0, 0, 1f, 1f, 0, 0, 0, 0)
                view.dispatchTouchEvent(pointerDownEvent)
                for (i in 1..10) {
                    val moveCoords = arrayOf(
                        MotionEvent.PointerCoords().apply { x = middleX - 10f - (i * 20f); y = middleY },
                        MotionEvent.PointerCoords().apply { x = middleX + 10f + (i * 20f); y = middleY }
                    )
                    val moveEvent = MotionEvent.obtain(eventTime, eventTime + (i * 10), MotionEvent.ACTION_MOVE, 2, startPointers, moveCoords, 0, 0, 1f, 1f, 0, 0, 0, 0)
                    view.dispatchTouchEvent(moveEvent)
                    uiController.loopMainThreadForAtLeast(10)
                }
                val upEvent = MotionEvent.obtain(eventTime, eventTime + 200, MotionEvent.ACTION_UP, 1, startPointers, startCoords, 0, 0, 1f, 1f, 0, 0, 0, 0)
                view.dispatchTouchEvent(upEvent)
            }
        }
    }
}
