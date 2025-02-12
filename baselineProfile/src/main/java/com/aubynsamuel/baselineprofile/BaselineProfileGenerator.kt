package com.aubynsamuel.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generateBaselineProfile() = baselineProfileRule.collect(
        packageName = "com.aubynsamuel.flashsend"
    ) {
        pressHome()
        startActivityAndWait()

//        // Simulate scrolling in LazyColumn
//        device.waitForIdle()
//        device.swipe(500, 1500, 500, 500, 10)  // Scroll down
//        device.waitForIdle()
//        device.swipe(500, 500, 500, 1500, 10)  // Scroll up

        device.waitForIdle()
        device.swipe(500, 1500, 500, 500, 10)  // Scroll down
        device.waitForIdle()
        device.swipe(500, 500, 500, 1500, 10)  // Scroll up

        // Navigate to a chat conversation
        // Replace with a tap coordinate or UI Automator selector as needed:
        device.click(300, 600)
        startActivityAndWait()  // Wait for the conversation to load

        // Simulate scrolling through the conversation
        device.waitForIdle()
        device.swipe(500, 1200, 500, 400, 10)
        device.waitForIdle()

        // Navigate back to the chat list
        device.pressBack()
        device.waitForIdle()

    }
}
