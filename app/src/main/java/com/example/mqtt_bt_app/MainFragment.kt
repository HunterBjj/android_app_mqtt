package com.example.mqtt_bt_app

import BluetoothController
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.mqtt_bt_app.databinding.FragmentMainBinding
import com.example.bt_def.BluetoothConstants
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence




class MainFragment : Fragment(), BluetoothController.Listener {
    private lateinit var bluetoothController: BluetoothController
    private lateinit var btAdapter: BluetoothAdapter
    private lateinit var binding: FragmentMainBinding
    private lateinit var parametersRecyclerView: RecyclerView
    private lateinit var mqttClient: MqttAndroidClient

    private val mqttServerUri = "tcp://mqtt.eclipse.org:1883"
    private val clientId = "AndroidClient"
    private val subscriptionTopic = "test/topic"
    private val publishTopic = "test/topic"
    private val qos = 1

    fun ByteArray.toHex() = joinToString(separator = "") { byte -> "%02x".format(byte) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,

        ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        binding.FrameLayout2.setVisibility(View.GONE)
        // MQTT клиент и подписка
        mqttClient =
            MqttAndroidClient(requireContext(), mqttServerUri, clientId, MemoryPersistence())
        mqttClient.setCallback(mqttCallback)
        val options = MqttConnectOptions()
        options.isAutomaticReconnect = true
        mqttClient.connect(options, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                subscribeToTopic()
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Toast.makeText(
                    requireContext(),
                    "Failed to connect to MQTT broker",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
        return binding.root

    }

    private fun subscribeToTopic() {
        mqttClient.subscribe(subscriptionTopic, qos, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Toast.makeText(requireContext(), "Subscribed to MQTT topic", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Toast.makeText(
                    requireContext(),
                    "Failed to subscribe to MQTT topic",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun publishMessage(message: String) {
        try {
            val mqttMessage = MqttMessage()
            mqttMessage.payload = message.toByteArray()
            mqttClient.publish(publishTopic, mqttMessage)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val mqttCallback = object : MqttCallbackExtended {
        override fun connectComplete(reconnect: Boolean, serverURI: String) {
            if (reconnect) {
                // Подключение было восстановлено
                subscribeToTopic()
            } else {
                // Новое подключение
                Toast.makeText(requireContext(), "Connected to MQTT broker", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        override fun connectionLost(cause: Throwable) {
            // Потеря соединения
            Toast.makeText(requireContext(), "Connection lost to MQTT broker", Toast.LENGTH_SHORT)
                .show()
        }

        @Throws(Exception::class)
        override fun messageArrived(topic: String, message: MqttMessage) {
            // Получение сообщения от MQTT
            val payload = String(message.payload)
            activity?.runOnUiThread {
                // Обновление интерфейса с полученными данными
            }
        }
    override fun deliveryComplete(token: IMqttDeliveryToken) {
        // Доставка сообщения завершена
    }
}
        override fun onResume() {
            super.onResume()
            activity?.invalidateOptionsMenu() // Для фрагмента
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            setHasOptionsMenu(true)
            super.onCreate(savedInstanceState)
            val callback =
                requireActivity().onBackPressedDispatcher.addCallback(this) { // Обработки кнопки для выхода из приложение.
                    Toast.makeText(
                        requireActivity(),
                        "Вы успешно вышли",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    binding.FrameLayout2.setVisibility(View.GONE)

                }

        }


        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
        }

        override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
            inflater.inflate(R.menu.main_menu, menu)
            super.onCreateOptionsMenu(menu, inflater)
            menu.setGroupVisible(R.id.menuColor, false)
        }


        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            try {
                binding.apply {
                    //val bundle = Bundle()

                    if (item.itemId == R.id.id_bt_connect) { // Подключение к ESP32.
                        initBtAdapter()
                        val pref = activity?.getSharedPreferences(
                            BluetoothConstants.PREFERENCES, Context.MODE_PRIVATE
                        )
                        val mac = pref?.getString(BluetoothConstants.MAC, "")
                        bluetoothController = BluetoothController(btAdapter)
                        bluetoothController.connect(mac ?: "", this@MainFragment)
                        Toast.makeText(requireActivity(), "Подключение к маяку", Toast.LENGTH_SHORT)
                            .show()
                    } else if (item.itemId == R.id.id_search) {
                        findNavController().navigate(R.id.listFragment)
                    } else if (item.itemId == R.id.redButton) {
                        bluetoothController.sendMessage("R") //RED
                        Toast.makeText(
                            requireActivity(),
                            "Вы выбрали красный цвет",
                            Toast.LENGTH_SHORT
                        )
                            .show()

                    } else if (item.itemId == R.id.yellowButton) {
                        bluetoothController.sendMessage("Y") //RED
                        Toast.makeText(
                            requireActivity(),
                            "Вы выбрали желтый цвет",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    } else if (item.itemId == R.id.greenButton) {
                        bluetoothController.sendMessage("G")
                        Toast.makeText(
                            requireActivity(),
                            "Вы выбрали зеленый цвет",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    } else if (item.itemId == R.id.whiteButton) {
                        bluetoothController.sendMessage("W")
                        Toast.makeText(
                            requireActivity(),
                            "Вы выбрали белый цвет",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireActivity(), "Сбой подключения к огню", Toast.LENGTH_LONG)
                    .show()
            }
            return super.onOptionsItemSelected(item)
        }

        private fun initBtAdapter() {
            val bManager = activity?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            btAdapter = bManager.adapter
        }

        override fun onReceive(message: String) {
            activity?.runOnUiThread {
                if (message == "0") {
                    binding.textColor.text = "Цвет свечения: Красный"
                } else if (message == "2") {
                    binding.textColor.text = "Цвет свечения: Желтый"
                }
            }
        }


        override fun onDestroy() {
            super.onDestroy()
            bluetoothController.closeConnection()
        }
    override fun onDestroyView() {
        super.onDestroyView()
        mqttClient.disconnect()
    }
    }



