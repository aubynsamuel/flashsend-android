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

        // Log a message (optional) to indicate manual interaction phase
        println("Manual interaction phase started. You have 60 seconds to interact with the app...")

        // Pause execution to allow manual interactions (60 seconds in this case)
        Thread.sleep(20000)

        // Optionally, add a log to indicate the end of manual interaction phase
        println("Manual interaction phase ended.")
    }
}
