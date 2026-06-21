package com.bquelhas.navme

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val status = findViewById<TextView>(R.id.status)
        findViewById<Button>(R.id.btnNotifAccess).setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
        findViewById<Button>(R.id.btnMock).setOnClickListener {
            val mock = NaviData(Direction.LEFT, "300 m — Vire à esquerda na Rua da Paz", "Good")
            PebbleEmitter.sendNav(applicationContext, mock)
            Toast.makeText(this, getString(R.string.mock_sent), Toast.LENGTH_SHORT).show()
        }

        status.text = getString(
            if (PebbleEmitter.isWatchConnected(applicationContext)) R.string.watch_connected
            else R.string.watch_disconnected
        )
    }

    override fun onResume() {
        super.onResume()
        findViewById<TextView>(R.id.status).text = getString(
            if (PebbleEmitter.isWatchConnected(applicationContext)) R.string.watch_connected
            else R.string.watch_disconnected
        )
    }
}
