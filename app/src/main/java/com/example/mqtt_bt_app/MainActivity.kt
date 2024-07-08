package com.example.mqtt_bt_app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.MqttClient

class MainActivity : AppCompatActivity() {
    private val mqttAndroidClient: MqttAndroidClient? = null
    private val serverUri = "tcp://broker.hivemq.com:1883" // Замените на адрес вашего брокера
    private val clientId = MqttClient.generateClientId()
    private val subscriptionTopic = "test/topic"
    private val publishTopic = "test/topic"



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}