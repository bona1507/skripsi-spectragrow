package com.pkmkcub.spectragrow

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.graphics.Bitmap
import android.net.TrafficStats
import android.os.Debug
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
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
import androidx.test.uiautomator.UiSelector
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
        // Mengambil alokasi memori awal saat heap dialokasikan
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

        // Mengukur waktu eksekusi pengujian dalam milidetik
        val elapsedTimeMillis: Long = measureTimeMillis {
            // Memastikan bahwa fragment onboarding ditampilkan
            onView(withId(R.id.onboarding_fragment)).check(matches(isDisplayed()))

            // Melakukan swipe dua kali untuk berpindah antar layar onboarding
            repeat(2) {
                onView(withId(R.id.onboarding_fragment)).perform(swipeLeft())
            }

            // Memastikan bahwa card view untuk memulai aplikasi ditampilkan setelah onboarding selesai
            onView(withId(R.id.card_start_app)).check(matches(isDisplayed()))

            // Mengklik tombol "Masuk" pada card view
            onView(withId(R.id.log_btn)).perform(click())

            // Memastikan bahwa fragment login ditampilkan
            onView(withId(R.id.login_fragment)).check(matches(isDisplayed()))

            // Memasukkan email ke dalam field email
            onView(withId(R.id.et_email))
                .perform(typeText("adminpunya@gmail.com"), closeSoftKeyboard())

            // Memasukkan password ke dalam field password
            onView(withId(R.id.et_password))
                .perform(typeText("punyaadmin"), closeSoftKeyboard())

            // Memastikan checkbox syarat dan ketentuan ditampilkan dan belum dicentang
            onView(withId(R.id.snk_check))
                .check(matches(isDisplayed()))
                .check(matches(isNotChecked()))

            // Mencentang checkbox syarat dan ketentuan sebelum melanjutkan
            onView(withId(R.id.snk_check)).perform(click())

            // Mengklik tombol login di fragment login
            onView(withId(R.id.btn_login)).perform(click())

            // Menunggu selama 2 detik untuk memastikan fragment home tampil dengan benar
            onView(isRoot()).perform(waitFor(2000))

            // Memastikan bahwa fragment home ditampilkan setelah login berhasil
            onView(withId(R.id.fragment_home)).check(matches(isDisplayed()))
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
    fun mainFlow() {
        // Mengambil alokasi memori awal saat heap dialokasikan
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
            // Memastikan fragment beranda ditampilkan
            onView(withId(R.id.fragment_home)).check(matches(isDisplayed()))

            // Menunggu RecyclerView diisi data dari Firestore
            onView(isRoot()).perform(waitFor(5000))

            // Scroll ke item tanaman dengan judul "Bawang Merah" dan klik item tersebut
            onView(withId(R.id.rv_search))
                .perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    hasDescendant(withText(containsString("Bawang Merah")))
                ))
                .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Bawang Merah")), click()
                ))

            // Pastikan item yang diklik tidak menampilkan teks default "Lorem ipsum"
            onView(withId(R.id.item_title_id))
                .check(matches(not(withText("Lorem ipsum"))))

            // Masukkan teks "Kopi" di bar pencarian
            onView(withId(R.id.et_search))
                .perform(typeText("Kopi"), closeSoftKeyboard())

            // Tunggu pencarian selesai
            onView(isRoot()).perform(waitFor(2000))

            // Scroll ke item tanaman dengan judul "Kopi" dan klik item tersebut
            onView(withId(R.id.rv_search))
                .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Kopi")), click()
                ))

            // Pastikan item yang diklik tidak menampilkan teks default "Lorem ipsum"
            onView(withId(R.id.item_title_id))
                .check(matches(not(withText("Lorem ipsum"))))

            // Pantau penggunaan memori selama pengujian
            val currentMemory = Debug.getNativeHeapAllocatedSize()
            if (currentMemory > peakMemoryUsage) {
                peakMemoryUsage = currentMemory
            }
        }

        // Menghentikan pengecekan penggunaan memori dan CPU
        handler.removeCallbacks(memoryCheckRunnable)
        handler.removeCallbacks(cpuCheckRunnable)

        // Ambil total byte jaringan yang dikirim dan diterima setelah tes selesai
        val finalTx = TrafficStats.getTotalTxBytes()
        val finalRx = TrafficStats.getTotalRxBytes()

        // Mencatat metrik performa seperti memori puncak, data yang dikirim/diterima, dan waktu eksekusi
        logPerformanceMetrics(
            testName = "PerformanceTestMain",
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
    fun mapsFlow() {
        // Mengambil alokasi memori awal saat heap dialokasikan
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
            // Memastikan fragment beranda ditampilkan
            onView(withId(R.id.fragment_home)).check(matches(isDisplayed()))

            // Tunggu untuk UI loading selama 2 detik
            onView(isRoot()).perform(waitFor(2000))

            // Klik tombol peta pada toolbar untuk membuka MapsFragment
            onView(withId(R.id.maps_btn)).perform(click())

            // Tunggu selama 2 detik untuk memuat data
            onView(isRoot()).perform(waitFor(2000))

            // Memastikan MapsFragment ditampilkan
            onView(withId(R.id.map)).check(matches(isDisplayed()))

            // Klik pada AutocompleteSupportFragment untuk memulai pencarian
            onView(withId(R.id.auto_complete_fragment)).perform(click())

            // Gunakan UI Automator untuk mengetik "Universitas Brawijaya" di kolom pencarian
            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            val searchText = device.findObject(UiSelector().className(EditText::class.java.name))
            searchText.setText("Universitas Brawijaya")
            device.pressEnter() // Menjalankan pencarian

            // Tunggu 2 detik hingga hasil pencarian dimuat
            onView(isRoot()).perform(waitFor(2000))

            // Klik saran pertama dari dropdown autocomplete
            val firstSuggestion = device.findObject(UiSelector().className(TextView::class.java.name).instance(0))
            firstSuggestion.click()

            // Tunggu 2 detik agar peta berpindah ke lokasi yang dipilih
            onView(isRoot()).perform(waitFor(2000))

            // Simulasi klik lama pada peta untuk memilih lokasi "Universitas Brawijaya"
            onView(withId(R.id.map)).perform(longClick())

            // Pastikan detail kartu hasil ditampilkan setelah klik lama
            onView(withId(R.id.result_section)).check(matches(isDisplayed()))

            // Tunggu 2 detik untuk memastikan kartu hasil tampil dengan benar
            onView(isRoot()).perform(waitFor(2000))

            // Pantau penggunaan memori selama pengujian
            val currentMemory = Debug.getNativeHeapAllocatedSize()
            if (currentMemory > peakMemoryUsage) {
                peakMemoryUsage = currentMemory
            }
        }

        // Menghentikan pengecekan penggunaan memori dan CPU
        handler.removeCallbacks(memoryCheckRunnable)
        handler.removeCallbacks(cpuCheckRunnable)

        // Ambil total byte jaringan yang dikirim dan diterima setelah tes selesai
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
    fun storyFlow() {
        // Mengambil alokasi memori awal saat heap dialokasikan
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
            // Memastikan fragment beranda ditampilkan
            onView(withId(R.id.fragment_home)).check(matches(isDisplayed()))

            // Tunggu untuk UI loading selama 2 detik
            onView(isRoot()).perform(waitFor(2000))

            // Klik tombol tambah pada toolbar untuk membuka StoryFragment
            onView(withId(R.id.add_story_btn))
                .perform(click())
            onView(withId(R.id.story_list_fragment))
                .check(matches(isDisplayed()))

            // Klik tombol 'New Story' untuk membuka halaman cerita baru
            onView(withId(R.id.new_story_btn))
                .perform(click())
            onView(withId(R.id.new_story_fragment))
                .check(matches(isDisplayed()))

            // Simulasikan pengambilan gambar menggunakan kamera
            simulateCameraCapture()

            // Isi judul dan deskripsi cerita
            onView(withId(R.id.write_title))
                .perform(typeText("Sample Story Title"), closeSoftKeyboard())
            onView(withId(R.id.write_desc))
                .perform(typeText("This is a detailed description of the sample story."), closeSoftKeyboard())

            // Klik tombol 'Add Story' untuk mengirim cerita baru
            onView(withId(R.id.btn_add))
                .perform(click())

            // Tunggu agar RecyclerView terisi dengan data dari Firestore
            onView(isRoot()).perform(waitFor(3000))

            // Klik item pertama di RecyclerView untuk membuka detailnya
            onView(withId(R.id.rv_main))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

            // Tunggu 2 detik untuk memastikan proses pemuatan cerita berjalan dengan benar
            onView(isRoot()).perform(waitFor(2000))

            onView(withId(R.id.story_detail_fragment))
                .check(matches(isDisplayed()))

            // Monitor penggunaan memori dan CPU selama pengujian
            val currentMemory = Debug.getNativeHeapAllocatedSize()
            if (currentMemory > peakMemoryUsage) {
                peakMemoryUsage = currentMemory
            }
        }

        // Menghentikan pengecekan penggunaan memori dan CPU
        handler.removeCallbacks(memoryCheckRunnable)
        handler.removeCallbacks(cpuCheckRunnable)

        // Ambil total byte jaringan yang dikirim dan diterima setelah tes selesai
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
