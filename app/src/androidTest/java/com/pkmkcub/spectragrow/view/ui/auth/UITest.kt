package com.pkmkcub.spectragrow.view.ui.auth

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.graphics.Bitmap
import android.net.TrafficStats
import android.os.Debug
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiSelector
import com.pkmkcub.spectragrow.MainActivity
import com.pkmkcub.spectragrow.R
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers.containsString
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.system.measureTimeMillis

@RunWith(AndroidJUnit4::class)
@LargeTest
class UITest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setup() {
        Intents.init()
    }

    @Test
    fun loginFlow() {
        // Start monitoring memory, CPU, and bandwidth
        val initialMemory = Debug.getNativeHeapAllocatedSize()
        val initialCpuTime = Debug.threadCpuTimeNanos()
        val initialTx = TrafficStats.getTotalTxBytes()
        val initialRx = TrafficStats.getTotalRxBytes()
        var peakMemoryUsage = initialMemory

        // Measure the test runtime in milliseconds
        val elapsedTimeMillis: Long = measureTimeMillis {
            // Check if the Home Fragment is displayed or not
            val isHomeDisplayed = try {
                onView(withId(R.id.fragment_home)).check(matches(isDisplayed()))
                true // Home Fragment is displayed
            } catch (e: NoMatchingViewException) {
                false // Home Fragment is not displayed
            }

            if (isHomeDisplayed) {
                // If Home Fragment is displayed, click the logout button
                onView(withId(R.id.logout_btn)).perform(click())

                // Verify that the onboarding fragment is displayed after logout
                onView(withId(R.id.onboarding_fragment)).check(matches(isDisplayed()))
            } else {
                // Verify that the onboarding fragment is directly displayed
                onView(withId(R.id.onboarding_fragment)).check(matches(isDisplayed()))
            }

            // Perform swipe actions to navigate through the onboarding screens
            onView(withId(R.id.onboarding_fragment)).perform(swipeLeft())
            onView(withId(R.id.onboarding_fragment)).perform(swipeLeft())

            // Verify that the card view is displayed after completing the onboarding
            onView(withId(R.id.card_start_app)).check(matches(isDisplayed()))

            // Click the "Masuk" button on the card view
            onView(withId(R.id.log_btn)).perform(click())

            // Verify that the login fragment is displayed
            onView(withId(R.id.login_fragment)).check(matches(isDisplayed()))

            // Enter email into the email field
            onView(withId(R.id.et_email))
                .perform(typeText("adminpunya@gmail.com"), closeSoftKeyboard())

            // Enter password into the password field
            onView(withId(R.id.et_password))
                .perform(typeText("punyaadmin"), closeSoftKeyboard())

            // Verify that the terms and conditions checkbox is displayed and unchecked
            onView(withId(R.id.snk_check))
                .check(matches(isDisplayed()))
                .check(matches(isNotChecked()))

            // Check the terms and conditions checkbox before proceeding
            onView(withId(R.id.snk_check)).perform(click())

            // Click the login button in the login fragment
            onView(withId(R.id.btn_login)).perform(click())

            // Continuously monitor the memory and CPU usage during the test
            val currentMemory = Debug.getNativeHeapAllocatedSize()
            if (currentMemory > peakMemoryUsage) {
                peakMemoryUsage = currentMemory
            }
        }

        // Final values after the test run
        val finalMemory = Debug.getNativeHeapAllocatedSize()
        val finalCpuTime = Debug.threadCpuTimeNanos()
        val finalTx = TrafficStats.getTotalTxBytes()
        val finalRx = TrafficStats.getTotalRxBytes()

        // Calculate memory usage in megabytes (MB)
        val memoryUsedMb = (finalMemory - initialMemory) / (1024 * 1024.0)
        val peakMemoryUsageMb = peakMemoryUsage / (1024 * 1024.0)

        // Calculate CPU usage percentage
        val cpuTimeUsedMs = (finalCpuTime - initialCpuTime) / 1_000_000.0
        val cpuUsagePercentage = (cpuTimeUsedMs / elapsedTimeMillis) * 100

        // Log the performance data
        Log.d("PerformanceTestLogin", "Peak Memory Usage: ${String.format("%.2f", peakMemoryUsageMb)} MB")
        Log.d("PerformanceTestLogin", "Memory Difference: ${String.format("%.2f", memoryUsedMb)} MB")
        Log.d("PerformanceTestLogin", "Peak CPU Usage: ${String.format("%.2f", cpuUsagePercentage)}%")
        Log.d("PerformanceTestLogin", "Data Sent: ${(finalTx - initialTx) / (1024.0)} KB")
        Log.d("PerformanceTestLogin", "Data Received: ${(finalRx - initialRx) / (1024.0)} KB")
        Log.d("PerformanceTestLogin", "Test Runtime: ${elapsedTimeMillis / 1000.0} seconds")
    }

    @Test
    fun mainFlow() {
        // Start monitoring memory, CPU, and bandwidth
        val initialMemory = Debug.getNativeHeapAllocatedSize()
        val initialCpuTime = Debug.threadCpuTimeNanos()
        val initialTx = TrafficStats.getTotalTxBytes()
        val initialRx = TrafficStats.getTotalRxBytes()
        var peakMemoryUsage = initialMemory

        val elapsedTimeMillis: Long = measureTimeMillis {
            // Check if the Home Fragment is displayed, otherwise perform the login flow
            val isHomeDisplayed = try {
                onView(withId(R.id.fragment_home)).check(matches(isDisplayed()))
                true // Home Fragment is displayed
            } catch (e: NoMatchingViewException) {
                false // Home Fragment is not displayed
            }

            if (!isHomeDisplayed) {
                loginFlow()
            } else {
                onView(withId(R.id.fragment_home)).check(matches(isDisplayed()))
            }

            // Wait for the RecyclerView to be populated with data from Firestore
            onView(isRoot()).perform(waitFor(5000)) // Wait for 5 seconds for data loading

            // Scroll to the plant with title "Bawang Merah" in the RecyclerView and click it
            onView(withId(R.id.rv_search))
                .perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    hasDescendant(withText(containsString("Bawang Merah")))
                ))

            onView(withId(R.id.rv_search))
                .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Bawang Merah")), click()
                ))

            // Enter the search text "Kopi" in the search bar
            onView(withId(R.id.et_search))
                .perform(typeText("Kopi"), closeSoftKeyboard())

            // Wait for the search filter to apply
            onView(isRoot()).perform(waitFor(2000)) // Wait for 2 seconds for the filter to apply

            // Scroll to the plant with the title "Kopi" in the RecyclerView and click it
            onView(withId(R.id.rv_search))
                .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Kopi")), click()
                ))

            // Verify that the clicked plant item is not showing default text "Lorem ipsum"
            onView(withId(R.id.item_title_id))
                .check(matches(not(withText("Lorem ipsum"))))

            // Continuously monitor the memory and CPU usage during the test
            val currentMemory = Debug.getNativeHeapAllocatedSize()
            if (currentMemory > peakMemoryUsage) {
                peakMemoryUsage = currentMemory
            }
        }

        // Final values after the test run
        val finalMemory = Debug.getNativeHeapAllocatedSize()
        val finalCpuTime = Debug.threadCpuTimeNanos()
        val finalTx = TrafficStats.getTotalTxBytes()
        val finalRx = TrafficStats.getTotalRxBytes()

        // Calculate memory usage in megabytes (MB)
        val memoryUsedMb = (finalMemory - initialMemory) / (1024 * 1024.0)
        val peakMemoryUsageMb = peakMemoryUsage / (1024 * 1024.0)

        // Calculate CPU usage percentage
        val cpuTimeUsedMs = (finalCpuTime - initialCpuTime) / 1_000_000.0
        val cpuUsagePercentage = (cpuTimeUsedMs / elapsedTimeMillis) * 100

        // Log the performance data
        Log.d("PerformanceTestMain", "Peak Memory Usage: ${String.format("%.2f", peakMemoryUsageMb)} MB")
        Log.d("PerformanceTestMain", "Memory Difference: ${String.format("%.2f", memoryUsedMb)} MB")
        Log.d("PerformanceTestMain", "Peak CPU Usage: ${String.format("%.2f", cpuUsagePercentage)}%")
        Log.d("PerformanceTestMain", "Data Sent: ${(finalTx - initialTx) / (1024.0)} KB")
        Log.d("PerformanceTestMain", "Data Received: ${(finalRx - initialRx) / (1024.0)} KB")
        Log.d("PerformanceTestMain", "Test Runtime: ${elapsedTimeMillis / 1000.0} seconds")
    }

    @Test
    fun mapsFlow() {
        val initialMemory = Debug.getNativeHeapAllocatedSize()
        val initialCpuTime = Debug.threadCpuTimeNanos()
        val initialTx = TrafficStats.getTotalTxBytes()
        val initialRx = TrafficStats.getTotalRxBytes()
        var peakMemoryUsage = initialMemory

        val elapsedTimeMillis: Long = measureTimeMillis {
            // Check if the Home Fragment is displayed, otherwise perform the login flow
            val isHomeDisplayed = try {
                onView(withId(R.id.fragment_home)).check(matches(isDisplayed()))
                true // Home Fragment is displayed
            } catch (e: NoMatchingViewException) {
                false // Home Fragment is not displayed
            }

            if (!isHomeDisplayed) {
                loginFlow()  // Assuming loginFlow() is a helper function to perform login
            } else {
                onView(withId(R.id.fragment_home)).check(matches(isDisplayed()))
            }

            // Wait for RecyclerView to be populated with data from Firestore
            onView(isRoot()).perform(waitFor(2000)) // Wait for 2 seconds for data loading

            // Click the maps button in the toolbar to open MapsFragment
            onView(withId(R.id.maps_btn))
                .perform(click())

            onView(isRoot()).perform(waitFor(2000)) // Wait for 2 seconds for data loading

            // Handle permission request if needed
            grantPermissionIfNeeded()

            // Wait for MapsFragment to be displayed
            onView(withId(R.id.map))
                .check(matches(isDisplayed()))

            // Click on the AutocompleteSupportFragment to activate it
            onView(withId(R.id.auto_complete_fragment)).perform(click())

            // Use UI Automator to type "Malang" into the search field inside AutocompleteSupportFragment
            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            val searchText = device.findObject(UiSelector().className(EditText::class.java.name))

            // Set text for the search field
            searchText.setText("Universitas Brawijaya")

            // Press the action button to initiate the search
            device.pressEnter()

            onView(isRoot()).perform(waitFor(2000)) // Wait for suggestions to load

            // Find the first suggestion in the autocomplete dropdown and click it
            val firstSuggestion = device.findObject(UiSelector().className(TextView::class.java.name).instance(0))
            firstSuggestion.click()

            onView(isRoot()).perform(waitFor(5000)) // Wait for the map to update to the selected location

            // Simulate a long click on the map to select "Malang"
            onView(withId(R.id.map))
                .perform(longClick())

            // Check that the card details are displayed after the long click
            onView(withId(R.id.result_section))
                .check(matches(isDisplayed()))

            // Continuously monitor the memory and CPU usage during the test
            val currentMemory = Debug.getNativeHeapAllocatedSize()
            if (currentMemory > peakMemoryUsage) {
                peakMemoryUsage = currentMemory
            }
        }

        // Final values after the test run
        val finalMemory = Debug.getNativeHeapAllocatedSize()
        val finalCpuTime = Debug.threadCpuTimeNanos()
        val finalTx = TrafficStats.getTotalTxBytes()
        val finalRx = TrafficStats.getTotalRxBytes()

        // Calculate memory usage in megabytes (MB)
        val memoryUsedMb = (finalMemory - initialMemory) / (1024 * 1024.0)
        val peakMemoryUsageMb = peakMemoryUsage / (1024 * 1024.0)

        // Calculate CPU usage percentage
        val cpuTimeUsedMs = (finalCpuTime - initialCpuTime) / 1_000_000.0
        val cpuUsagePercentage = (cpuTimeUsedMs / elapsedTimeMillis) * 100

        // Log the performance data
        Log.d("PerformanceTestMain", "Peak Memory Usage: ${String.format("%.2f", peakMemoryUsageMb)} MB")
        Log.d("PerformanceTestMain", "Memory Difference: ${String.format("%.2f", memoryUsedMb)} MB")
        Log.d("PerformanceTestMain", "Peak CPU Usage: ${String.format("%.2f", cpuUsagePercentage)}%")
        Log.d("PerformanceTestMain", "Data Sent: ${(finalTx - initialTx) / (1024.0)} KB")
        Log.d("PerformanceTestMain", "Data Received: ${(finalRx - initialRx) / (1024.0)} KB")
        Log.d("PerformanceTestMain", "Test Runtime: ${elapsedTimeMillis / 1000.0} seconds")
    }

    @Test
    fun storyFlow() {
        val initialMemory = Debug.getNativeHeapAllocatedSize()
        val initialCpuTime = Debug.threadCpuTimeNanos()
        val initialTx = TrafficStats.getTotalTxBytes()
        val initialRx = TrafficStats.getTotalRxBytes()
        var peakMemoryUsage = initialMemory

        val elapsedTimeMillis: Long = measureTimeMillis {
            // Check if the Home Fragment is displayed, otherwise perform the login flow
            val isHomeDisplayed = try {
                onView(withId(R.id.fragment_home)).check(matches(isDisplayed()))
                true // Home Fragment is displayed
            } catch (e: NoMatchingViewException) {
                false // Home Fragment is not displayed
            }

            if (!isHomeDisplayed) {
                loginFlow()
            } else {
                onView(withId(R.id.fragment_home)).check(matches(isDisplayed()))
            }

            // Wait for the RecyclerView to be populated with data from Firestore
            onView(isRoot()).perform(waitFor(5000)) // Wait for 5 seconds for data loading

            // Click the 'Add Story' button in the toolbar to open the Story List Fragment
            onView(withId(R.id.add_story_btn))
                .perform(click())

            onView(withId(R.id.story_list_fragment))
                .check(matches(isDisplayed()))

            // Click the 'New Story' button to open the New Story Fragment
            onView(withId(R.id.new_story_btn))
                .perform(click())

            onView(withId(R.id.new_story_fragment))
                .check(matches(isDisplayed()))

            // Simulate taking a picture using the camera
            simulateCameraCapture()

            // Fill in the story title and description
            onView(withId(R.id.write_title))
                .perform(typeText("Sample Story Title"), closeSoftKeyboard())

            onView(withId(R.id.write_desc))
                .perform(typeText("This is a detailed description of the sample story."), closeSoftKeyboard())

            // Click the 'Add Story' button to submit the new story
            onView(withId(R.id.btn_add))
                .perform(click())

            // Wait for the RecyclerView to be populated with data from Firestore
            onView(isRoot()).perform(waitFor(3000)) // Wait for 3 seconds for data loading

            // Click the first item in the RecyclerView to open its details
            onView(withId(R.id.rv_main))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

            // Wait for the submission process to complete and Story Detail Fragment to appear
            onView(isRoot()).perform(waitFor(5000)) // Wait for submission and UI update

            onView(withId(R.id.story_detail_fragment))
                .check(matches(isDisplayed()))

            // Continuously monitor the memory and CPU usage during the test
            val currentMemory = Debug.getNativeHeapAllocatedSize()
            if (currentMemory > peakMemoryUsage) {
                peakMemoryUsage = currentMemory
            }
        }

        // Final values after the test run
        val finalMemory = Debug.getNativeHeapAllocatedSize()
        val finalCpuTime = Debug.threadCpuTimeNanos()
        val finalTx = TrafficStats.getTotalTxBytes()
        val finalRx = TrafficStats.getTotalRxBytes()

        // Calculate memory usage in megabytes (MB)
        val memoryUsedMb = (finalMemory - initialMemory) / (1024 * 1024.0)
        val peakMemoryUsageMb = peakMemoryUsage / (1024 * 1024.0)

        // Calculate CPU usage percentage
        val cpuTimeUsedMs = (finalCpuTime - initialCpuTime) / 1_000_000.0
        val cpuUsagePercentage = (cpuTimeUsedMs / elapsedTimeMillis) * 100

        // Log the performance data
        Log.d("PerformanceTestMain", "Peak Memory Usage: ${String.format("%.2f", peakMemoryUsageMb)} MB")
        Log.d("PerformanceTestMain", "Memory Difference: ${String.format("%.2f", memoryUsedMb)} MB")
        Log.d("PerformanceTestMain", "Peak CPU Usage: ${String.format("%.2f", cpuUsagePercentage)}%")
        Log.d("PerformanceTestMain", "Data Sent: ${(finalTx - initialTx) / (1024.0)} KB")
        Log.d("PerformanceTestMain", "Data Received: ${(finalRx - initialRx) / (1024.0)} KB")
        Log.d("PerformanceTestMain", "Test Runtime: ${elapsedTimeMillis / 1000.0} seconds")
    }

    private fun simulateCameraCapture() {
        // Create a mock Bitmap for the camera image
        val mockBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        Log.d("CameraTest", "Mock Bitmap created: $mockBitmap")

        // Create an Intent with the mock Bitmap as the result data
        val resultData = Intent()
        resultData.putExtra("data", mockBitmap) // Pass the Bitmap explicitly

        // Create a mock ActivityResult with the Intent data
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)

        // Set up an intent matcher to intercept the camera launch
        Intents.intending(IntentMatchers.hasAction("android.media.action.IMAGE_CAPTURE"))
            .respondWith(result)

        // Click on the camera button to trigger the intent
        onView(withId(R.id.btn_camera))
            .perform(click())
        Log.d("CameraTest", "Camera button clicked.")
    }

    private fun waitFor(millis: Long): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): org.hamcrest.Matcher<View> {
                return isRoot()
            }

            override fun getDescription(): String {
                return "Wait for $millis milliseconds."
            }

            override fun perform(uiController: androidx.test.espresso.UiController?, view: View?) {
                uiController?.loopMainThreadForAtLeast(millis)
            }
        }
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    private fun grantPermissionIfNeeded() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Introduce a small delay to ensure the dialog is displayed
        device.waitForIdle(2000)

        try {
            // Locate the "SAAT APLIKASI DIGUNAKAN" button and click it
            val allowButton: UiObject = device.findObject(UiSelector()
                .textContains("SAAT APLIKASI DIGUNAKAN")  // Try matching with partial text
                .className("android.widget.Button")) // Ensure it's a button

            if (allowButton.exists() && allowButton.isClickable) {
                allowButton.click()
            } else {
                // Retry with exact match if partial text doesn't work
                val exactAllowButton: UiObject = device.findObject(UiSelector()
                    .text("SAAT APLIKASI DIGUNAKAN")  // Exact match
                    .className("android.widget.Button"))

                if (exactAllowButton.exists() && exactAllowButton.isClickable) {
                    exactAllowButton.click()
                }
            }
        } catch (e: UiObjectNotFoundException) {
            // Handle the exception if the button is not found
            println("Permission dialog button not found: ${e.message}")
        }
    }
}
