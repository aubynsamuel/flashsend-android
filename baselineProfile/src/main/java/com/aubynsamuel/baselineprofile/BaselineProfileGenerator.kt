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
    }
}
