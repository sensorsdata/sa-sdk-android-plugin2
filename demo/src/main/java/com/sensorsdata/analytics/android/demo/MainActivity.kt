package com.sensorsdata.analytics.android.demo

import android.app.PendingIntent
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.webkit.WebView
import android.widget.Button
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.test_btn).setOnClickListener {
            println("====clicked222====")
            println("====clicked====")
            startActivity(Intent(this@MainActivity, H5Activity::class.java))
        }
    }




    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        return super.dispatchKeyEvent(event)
    }

    private fun testNotification(){

       var pendingIntent =  PendingIntent.getActivity(this,200, null, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(this, "CHANNEL_ID")
            .setContentIntent(pendingIntent)
            .setContentTitle("My notification")
            .setContentText("Hello World!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Set the intent that will fire when the user taps the notification
            .setAutoCancel(true)

        NotificationManagerCompat.from(this).notify(123,  builder.build())
    }
}