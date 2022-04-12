package xyz.junerver.testasm

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import xyz.junerver.analytics_utils.annotation.TrackDimension
import xyz.junerver.analytics_utils.annotation.TrackEvent
import xyz.junerver.analytics_utils.annotation.TrackScreenView
import xyz.junerver.analytics_utils.common.LifecycleMethod
import kotlin.jvm.Throws

@TrackScreenView("/main", "mainModule", "mainActivity", LifecycleMethod.ON_CREATE)
class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        println("Hello, world!")
        findViewById<Button>(R.id.button).setOnClickListener {
            testEvent()
        }
        findViewById<Button>(R.id.button2).setOnClickListener {
            testDimension()
        }
        findViewById<Button>(R.id.button3).setOnClickListener {
            startActivity(Intent(this, SecondActivity::class.java))
        }
    }

    @Throws(Exception::class)
    @TrackEvent(category = "test11", action = "test22", name = "test33")
    fun testEvent() {
        Log.d(TAG, "testEvent: 我被程序主动调用")
    }

    @TrackDimension(dimension = "test111", value = "test222")
    fun testDimension() {
        Log.d(TAG, "testDimension: 我被程序主动调用")
    }

}