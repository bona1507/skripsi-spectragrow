package com.pkmkcub.spectragrow

import android.net.TrafficStats
import android.os.Debug
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.system.measureTimeMillis

@RunWith(AndroidJUnit4::class)
class UITestFlow {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun test1() {
        val initialMemory = Debug.getNativeHeapAllocatedSize()

        // Mengambil jumlah byte yang telah dikirim dan diterima pada jaringan
        val initialTx = TrafficStats.getTotalTxBytes()
        val initialRx = TrafficStats.getTotalRxBytes()

        // Variabel untuk mencatat penggunaan memori puncak selama pengujian
        var peakMemoryUsage = initialMemory

        // Variabel untuk mencatat puncak penggunaan CPU selama pengujian
        val initialCpuTime = Debug.threadCpuTimeNanos()
        var peakCpuUsage = 0L

        // Handler untuk pengecekan penggunaan memori
        val handler = Handler(Looper.getMainLooper())
        val memoryCheckRunnable = object : Runnable {
            override fun run() {
                // Mengambil penggunaan memori saat ini
                val currentMemory = Debug.getNativeHeapAllocatedSize()
                // Memperbarui penggunaan memori puncak jika melebihi nilai sebelumnya
                if (currentMemory > peakMemoryUsage) {
                    peakMemoryUsage = currentMemory
                }
                // Mengulangi pengecekan setiap 100 ms
                handler.postDelayed(this, 100)
            }
        }

        // Runnable untuk pengecekan penggunaan CPU
        val cpuCheckRunnable = object : Runnable {
            override fun run() {
                // Mengambil penggunaan CPU saat ini
                val currentCpuTime = Debug.threadCpuTimeNanos()

                // Menghitung penggunaan CPU (selisih antara CPU time)
                val cpuUsage = (currentCpuTime - initialCpuTime) / 1_000_000 // dalam ms

                // Memperbarui penggunaan CPU puncak jika lebih tinggi
                if (cpuUsage > peakCpuUsage) {
                    peakCpuUsage = cpuUsage
                }
                // Mengulangi pengecekan setiap 100 ms
                handler.postDelayed(this, 100)
            }
        }

        // Mulai pengecekan penggunaan memori dan CPU
        handler.post(memoryCheckRunnable)
        handler.post(cpuCheckRunnable)

        val elapsedTimeMillis: Long = measureTimeMillis {
            // Step 1: Verify the onboarding screen is displayed
            rule.onNodeWithTag("OnboardingPager").assertIsDisplayed()

            // Step 2: Swipe twice to the left to reach the auth card
            rule.onNodeWithTag("OnboardingPager").performTouchInput {
                swipeLeft()
            }
            rule.onNodeWithTag("OnboardingPager").performTouchInput {
                swipeLeft()
            }

            // Step 3: Click the login button to navigate to the login screen
            rule.onNodeWithTag("LoginButton").assertIsDisplayed()
            rule.onNodeWithTag("LoginButton").performClick()

            // Step 4: Perform login action
            rule.onNodeWithText("Surel").assertIsDisplayed()
            rule.onNodeWithText("Surel").performTextInput("adminpunya@gmail.com")
            rule.onNodeWithText("Kata sandi").assertIsDisplayed()
            rule.onNodeWithText("Kata sandi").performTextInput("punyaadmin")
            rule.onNodeWithTag("TermsCheckbox").assertIsDisplayed()
            rule.onNodeWithTag("TermsCheckbox").performClick()
            rule.onNodeWithTag("LoginSubmitButton").assertIsDisplayed()
            rule.onNodeWithTag("LoginSubmitButton").performClick()

            val isHomeScreenDisplayed = try {
                rule.onNodeWithTag("HomeScreenTopappbar").assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }

            if (!isHomeScreenDisplayed) {
                rule.waitUntil(timeoutMillis = 30000) {
                    try {
                        rule.onNodeWithTag("HomeScreenTopappbar").assertExists()
                        true
                    } catch (e: AssertionError) {
                        false
                    }
                }
            }
        }

        // Menghentikan pengecekan penggunaan memori dan CPU
        handler.removeCallbacks(memoryCheckRunnable)
        handler.removeCallbacks(cpuCheckRunnable)

        // Mengambil total byte yang dikirim dan diterima di akhir pengujian
        val finalTx = TrafficStats.getTotalTxBytes()
        val finalRx = TrafficStats.getTotalRxBytes()

        // Mencatat metrik performa seperti memori puncak, data yang dikirim/diterima, dan waktu eksekusi
        logPerformanceMetrics(
            testName = "PerformanceTestLogin",
            peakMemoryUsage = peakMemoryUsage,
            peakCpuUsage = peakCpuUsage,
            initialTx = initialTx,
            finalTx = finalTx,
            initialRx = initialRx,
            finalRx = finalRx,
            elapsedTimeMillis = elapsedTimeMillis
        )
    }


    @Test
    fun test2() {
        val initialMemory = Debug.getNativeHeapAllocatedSize()

        // Mengambil jumlah byte yang telah dikirim dan diterima pada jaringan
        val initialTx = TrafficStats.getTotalTxBytes()
        val initialRx = TrafficStats.getTotalRxBytes()

        // Variabel untuk mencatat penggunaan memori puncak selama pengujian
        var peakMemoryUsage = initialMemory

        // Variabel untuk mencatat puncak penggunaan CPU selama pengujian
        val initialCpuTime = Debug.threadCpuTimeNanos()
        var peakCpuUsage = 0L

        // Handler untuk pengecekan penggunaan memori
        val handler = Handler(Looper.getMainLooper())
        val memoryCheckRunnable = object : Runnable {
            override fun run() {
                // Mengambil penggunaan memori saat ini
                val currentMemory = Debug.getNativeHeapAllocatedSize()
                // Memperbarui penggunaan memori puncak jika melebihi nilai sebelumnya
                if (currentMemory > peakMemoryUsage) {
                    peakMemoryUsage = currentMemory
                }
                // Mengulangi pengecekan setiap 100 ms
                handler.postDelayed(this, 100)
            }
        }

        // Runnable untuk pengecekan penggunaan CPU
        val cpuCheckRunnable = object : Runnable {
            override fun run() {
                // Mengambil penggunaan CPU saat ini
                val currentCpuTime = Debug.threadCpuTimeNanos()

                // Menghitung penggunaan CPU (selisih antara CPU time)
                val cpuUsage = (currentCpuTime - initialCpuTime) / 1_000_000 // dalam ms

                // Memperbarui penggunaan CPU puncak jika lebih tinggi
                if (cpuUsage > peakCpuUsage) {
                    peakCpuUsage = cpuUsage
                }
                // Mengulangi pengecekan setiap 100 ms
                handler.postDelayed(this, 100)
            }
        }

        // Mulai pengecekan penggunaan memori dan CPU
        handler.post(memoryCheckRunnable)
        handler.post(cpuCheckRunnable)

        val elapsedTimeMillis: Long = measureTimeMillis {
            // Check if we are on the home screen by verifying the presence of the top app bar
            val isHomeScreenDisplayed = try {
                rule.onNodeWithTag("HomeScreenTopappbar").assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }

            if (!isHomeScreenDisplayed) {
                // Perform login
                this.test1()

                // Wait for the navigation to the home screen to complete
                rule.waitUntil(timeoutMillis = 30000) {
                    try {
                        rule.onNodeWithTag("HomeScreenTopappbar").assertExists()
                        true
                    } catch (e: AssertionError) {
                        false
                    }
                }
            }

            // Verify the home screen is displayed after login
            rule.onNodeWithTag("HomeScreenTopappbar").assertIsDisplayed()
            rule.onNodeWithTag("SearchBar").assertIsDisplayed().performTextInput("jagung")
            rule.waitUntil(timeoutMillis = 10000) {
                try {
                    rule.onNodeWithText("Jagung").assertExists()
                    true
                } catch (e: AssertionError) {
                    false
                }
            }
            rule.onNodeWithText("Jagung").assertIsDisplayed().performClick()

            // Step 2: Type "kopi" in the search bar
            rule.onNodeWithTag("SearchBar").assertIsDisplayed().performTextClearance()
            rule.onNodeWithTag("SearchBar").performTextInput("kopi")
            rule.onNodeWithText("Kopi").assertIsDisplayed().performClick()
        }

        // Menghentikan pengecekan penggunaan memori dan CPU
        handler.removeCallbacks(memoryCheckRunnable)
        handler.removeCallbacks(cpuCheckRunnable)

        // Mengambil total byte yang dikirim dan diterima di akhir pengujian
        val finalTx = TrafficStats.getTotalTxBytes()
        val finalRx = TrafficStats.getTotalRxBytes()

        // Mencatat metrik performa seperti memori puncak, data yang dikirim/diterima, dan waktu eksekusi
        logPerformanceMetrics(
            testName = "PerformanceTestHome",
            peakMemoryUsage = peakMemoryUsage,
            peakCpuUsage = peakCpuUsage,
            initialTx = initialTx,
            finalTx = finalTx,
            initialRx = initialRx,
            finalRx = finalRx,
            elapsedTimeMillis = elapsedTimeMillis
        )
    }

    @Test
    fun test3() {
        val initialMemory = Debug.getNativeHeapAllocatedSize()

        // Mengambil jumlah byte yang telah dikirim dan diterima pada jaringan
        val initialTx = TrafficStats.getTotalTxBytes()
        val initialRx = TrafficStats.getTotalRxBytes()

        // Variabel untuk mencatat penggunaan memori puncak selama pengujian
        var peakMemoryUsage = initialMemory

        // Variabel untuk mencatat puncak penggunaan CPU selama pengujian
        val initialCpuTime = Debug.threadCpuTimeNanos()
        var peakCpuUsage = 0L

        // Handler untuk pengecekan penggunaan memori
        val handler = Handler(Looper.getMainLooper())
        val memoryCheckRunnable = object : Runnable {
            override fun run() {
                // Mengambil penggunaan memori saat ini
                val currentMemory = Debug.getNativeHeapAllocatedSize()
                // Memperbarui penggunaan memori puncak jika melebihi nilai sebelumnya
                if (currentMemory > peakMemoryUsage) {
                    peakMemoryUsage = currentMemory
                }
                // Mengulangi pengecekan setiap 100 ms
                handler.postDelayed(this, 100)
            }
        }

        // Runnable untuk pengecekan penggunaan CPU
        val cpuCheckRunnable = object : Runnable {
            override fun run() {
                // Mengambil penggunaan CPU saat ini
                val currentCpuTime = Debug.threadCpuTimeNanos()

                // Menghitung penggunaan CPU (selisih antara CPU time)
                val cpuUsage = (currentCpuTime - initialCpuTime) / 1_000_000 // dalam ms

                // Memperbarui penggunaan CPU puncak jika lebih tinggi
                if (cpuUsage > peakCpuUsage) {
                    peakCpuUsage = cpuUsage
                }
                // Mengulangi pengecekan setiap 100 ms
                handler.postDelayed(this, 100)
            }
        }

        // Mulai pengecekan penggunaan memori dan CPU
        handler.post(memoryCheckRunnable)
        handler.post(cpuCheckRunnable)

        val elapsedTimeMillis: Long = measureTimeMillis {
            // Check if we are on the home screen by verifying the presence of the top app bar
            val isHomeScreenDisplayed = try {
                rule.onNodeWithTag("HomeScreenTopappbar").assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }

            if (!isHomeScreenDisplayed) {
                // Perform login
                this.test1()

                // Wait for the navigation to the home screen to complete
                rule.waitUntil(timeoutMillis = 30000) {
                    try {
                        rule.onNodeWithTag("HomeScreenTopappbar").assertExists()
                        true
                    } catch (e: AssertionError) {
                        false
                    }
                }
            }

            // Verify the home screen is displayed after login
            rule.onNodeWithTag("HomeScreenTopappbar").assertIsDisplayed()

            // Navigate to the Maps screen
            rule.onNodeWithTag("MapsButton").assertIsDisplayed().performClick()

            rule.onNodeWithTag("SearchBar").assertExists().performTextInput("Universitas Brawijaya, Jalan Veteran Malang, Ketawanggede, Kota Malang, Jawa Timur, Indonesia")
            // Assert if the search bar is displayed after long click
            rule.waitUntil(timeoutMillis = 30000) {
                try {
                    rule.onNodeWithText("Universitas Brawijaya, Jalan Veteran, Ketawanggede, Malang, Jawa Timur, Indonesia").assertIsDisplayed()
                    true
                } catch (e: AssertionError) {
                    false
                }
            }
            rule.onNodeWithText("Universitas Brawijaya, Jalan Veteran, Ketawanggede, Malang, Jawa Timur, Indonesia").performClick()
            // Wait until the map is displayed
            rule.waitUntil(timeoutMillis = 10000) {
                try {
                    rule.onNodeWithTag("Maps").assertIsDisplayed()
                    true
                } catch (e: AssertionError) {
                    false
                }
            }

            rule.onNodeWithTag("Maps").performClick()

            rule.waitUntil(timeoutMillis = 10000) {
                try {
                    // Validate if the map marker is shown (markerPosition should be updated)
                    rule.onNodeWithTag("ResultCard").assertIsDisplayed()
                    true
                } catch (e: AssertionError) {
                    false
                }
            }
        }

        // Menghentikan pengecekan penggunaan memori dan CPU
        handler.removeCallbacks(memoryCheckRunnable)
        handler.removeCallbacks(cpuCheckRunnable)

        // Mengambil total byte yang dikirim dan diterima di akhir pengujian
        val finalTx = TrafficStats.getTotalTxBytes()
        val finalRx = TrafficStats.getTotalRxBytes()

        // Mencatat metrik performa seperti memori puncak, data yang dikirim/diterima, dan waktu eksekusi
        logPerformanceMetrics(
            testName = "PerformanceTestMaps",
            peakMemoryUsage = peakMemoryUsage,
            peakCpuUsage = peakCpuUsage,
            initialTx = initialTx,
            finalTx = finalTx,
            initialRx = initialRx,
            finalRx = finalRx,
            elapsedTimeMillis = elapsedTimeMillis
        )
    }

    @Test
    fun test4() {
        val initialMemory = Debug.getNativeHeapAllocatedSize()

        // Mengambil jumlah byte yang telah dikirim dan diterima pada jaringan
        val initialTx = TrafficStats.getTotalTxBytes()
        val initialRx = TrafficStats.getTotalRxBytes()

        // Variabel untuk mencatat penggunaan memori puncak selama pengujian
        var peakMemoryUsage = initialMemory

        // Variabel untuk mencatat puncak penggunaan CPU selama pengujian
        val initialCpuTime = Debug.threadCpuTimeNanos()
        var peakCpuUsage = 0L

        // Handler untuk pengecekan penggunaan memori
        val handler = Handler(Looper.getMainLooper())
        val memoryCheckRunnable = object : Runnable {
            override fun run() {
                // Mengambil penggunaan memori saat ini
                val currentMemory = Debug.getNativeHeapAllocatedSize()
                // Memperbarui penggunaan memori puncak jika melebihi nilai sebelumnya
                if (currentMemory > peakMemoryUsage) {
                    peakMemoryUsage = currentMemory
                }
                // Mengulangi pengecekan setiap 100 ms
                handler.postDelayed(this, 100)
            }
        }

        // Runnable untuk pengecekan penggunaan CPU
        val cpuCheckRunnable = object : Runnable {
            override fun run() {
                // Mengambil penggunaan CPU saat ini
                val currentCpuTime = Debug.threadCpuTimeNanos()

                // Menghitung penggunaan CPU (selisih antara CPU time)
                val cpuUsage = (currentCpuTime - initialCpuTime) / 1_000_000 // dalam ms

                // Memperbarui penggunaan CPU puncak jika lebih tinggi
                if (cpuUsage > peakCpuUsage) {
                    peakCpuUsage = cpuUsage
                }
                // Mengulangi pengecekan setiap 100 ms
                handler.postDelayed(this, 100)
            }
        }

        // Mulai pengecekan penggunaan memori dan CPU
        handler.post(memoryCheckRunnable)
        handler.post(cpuCheckRunnable)

        val elapsedTimeMillis: Long = measureTimeMillis {
            // Step 1: Ensure user is on the Home Screen
            val isHomeScreenDisplayed = try {
                rule.onNodeWithTag("HomeScreenTopappbar").assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }

            if (!isHomeScreenDisplayed) {
                // Perform login if not already on the Home Screen
                this.test1()

                // Wait until navigation to Home Screen is complete
                rule.waitUntil(timeoutMillis = 30000) {
                    try {
                        rule.onNodeWithTag("HomeScreenTopappbar").assertExists()
                        true
                    } catch (e: AssertionError) {
                        false
                    }
                }
            }

            // Verify the Home Screen is displayed
            rule.onNodeWithTag("HomeScreenTopappbar").assertIsDisplayed()

            // Step 2: Navigate to the List Story Screen
            rule.onNodeWithTag("StoryButton").assertIsDisplayed().performClick()
            rule.onNodeWithTag("AddStoryFAB").assertIsDisplayed()

            // Step 3: Open the Add Story Screen
            rule.onNodeWithTag("AddStoryFAB").performClick()

            // Simulate adding a story
            val testTitle = "Test Story Title"
            val testDescription = "This is a test story description."
            rule.onNodeWithText("Enter title").assertIsDisplayed().performTextInput(testTitle)
            rule.onNodeWithText("Enter description").assertIsDisplayed().performTextInput(testDescription)

            // Simulate the camera capture
            rule.onNodeWithTag("ButtonCamera").performClick()
            rule.waitUntil(timeoutMillis = 5000) {
                try {
                    rule.onNodeWithTag("ButtonTakePicture").assertExists()
                    true
                } catch (e: AssertionError) {
                    false
                }
            }
            rule.onNodeWithTag("ButtonTakePicture").performClick()

            // Wait for image capture simulation
            rule.waitUntil(timeoutMillis = 5000) {
                // Verify that an image is selected (mock URI check or confirmation text)
                try {
                    rule.onNodeWithText("No Image Selected").assertDoesNotExist()
                    rule.onNodeWithText("Upload Story").assertExists()
                    true
                } catch (e: AssertionError) {
                    false
                }
            }

            // Submit the story
            rule.onNodeWithText("Upload Story").performClick()

            // Step 4: Wait until upload is complete and navigate back to the List Story Screen
            rule.waitUntil(timeoutMillis = 10000) {
                try {
                    rule.onNodeWithTag("ListStory").assertExists()
                    true
                } catch (e: AssertionError) {
                    false
                }
            }

            // Scroll and verify the added story appears in the list
            var storyFound = false
            val maxScrollAttempts = 50

            for (index in 0 until maxScrollAttempts) {
                try {
                    rule.onNodeWithText(testTitle).assertIsDisplayed()
                    rule.onNodeWithText(testDescription).assertIsDisplayed()
                    storyFound = true
                    break
                } catch (e: AssertionError) {
                    // Scroll to the next index if the story is not found
                    rule.onNodeWithTag("ListStory").performScrollToIndex(index)
                }
            }

            // Assert that the story was eventually found
            if (!storyFound) {
                throw AssertionError("Story with title '$testTitle' was not found in the list.")
            }

            // Step 5: Open the Detail Story Screen
            rule.onNodeWithText(testTitle).performClick()

            // Verify details in the Detail Story Screen
            rule.onNodeWithText(testTitle).assertIsDisplayed()
            rule.onNodeWithText(testDescription).assertIsDisplayed()
        }

        // Menghentikan pengecekan penggunaan memori dan CPU
        handler.removeCallbacks(memoryCheckRunnable)
        handler.removeCallbacks(cpuCheckRunnable)

        // Mengambil total byte yang dikirim dan diterima di akhir pengujian
        val finalTx = TrafficStats.getTotalTxBytes()
        val finalRx = TrafficStats.getTotalRxBytes()

        // Mencatat metrik performa seperti memori puncak, data yang dikirim/diterima, dan waktu eksekusi
        logPerformanceMetrics(
            testName = "PerformanceTestStory",
            peakMemoryUsage = peakMemoryUsage,
            peakCpuUsage = peakCpuUsage,
            initialTx = initialTx,
            finalTx = finalTx,
            initialRx = initialRx,
            finalRx = finalRx,
            elapsedTimeMillis = elapsedTimeMillis
        )
    }

    private fun logPerformanceMetrics(
        testName: String,
        peakMemoryUsage: Long,
        peakCpuUsage: Long,
        initialTx: Long,
        finalTx: Long,
        initialRx: Long,
        finalRx: Long,
        elapsedTimeMillis: Long
    ) {
        // Hitung penggunaan memori tertinggi dalam (MB)
        val peakMemoryUsageMb = peakMemoryUsage / (1024.0 * 1024.0)

        // Hitung data upload dan download dalam (KB)
        val dataSentKb = (finalTx - initialTx) / 1024.0
        val dataReceivedKb = (finalRx - initialRx) / 1024.0

        // Logging untuk performa aplikasi
        Log.d(testName, "Penggunaan Memori Tertinggi: ${String.format("%.2f", peakMemoryUsageMb)} MB")
        Log.d(testName, "Waktu Penggunaan CPU: $peakCpuUsage ms")
        Log.d(testName, "Data Unggah: ${String.format("%.2f", dataSentKb)} KB")
        Log.d(testName, "Data Unduh: ${String.format("%.2f", dataReceivedKb)} KB")
        Log.d(testName, "Test Runtime: ${String.format("%.2f", elapsedTimeMillis / 1000.0)} seconds")
    }
}
