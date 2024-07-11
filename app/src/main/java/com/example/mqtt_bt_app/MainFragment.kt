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



class MainFragment : Fragment(), BluetoothController.Listener {
    private lateinit var bluetoothController: BluetoothController
    private lateinit var btAdapter: BluetoothAdapter
    private lateinit var binding: FragmentMainBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,

        ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        binding.FrameLayout2.setVisibility(View.GONE)

        return binding.root

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
                    closeButton.setOnClickListener {  FrameLayout2.setVisibility(View.GONE) }
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

                    else if (item.itemId == R.id.changePsw) {
                    FrameLayout2.setVisibility(View.VISIBLE)

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
                // Добавление сообщения в лог
                val logsTextView = binding.FrameLayout2.findViewById<TextView>(R.id.btLogsTextView)
                logsTextView.append("Bluetooth log: $message\n")
                // Прокрутка до конца текста
                val scrollAmount = logsTextView.layout.getLineTop(logsTextView.lineCount) - logsTextView.height
                if (scrollAmount > 0) logsTextView.scrollTo(0, scrollAmount) else logsTextView.scrollTo(0, 0)
            }
        }


        override fun onDestroy() {
            super.onDestroy()
            bluetoothController.closeConnection()
        }
    override fun onDestroyView() {
        super.onDestroyView()
    }
    }



