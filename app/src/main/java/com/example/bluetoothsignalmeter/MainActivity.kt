package com.example.bluetoothsignalmeter

import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
<<<<<<< HEAD
import android.os.Bundle
=======
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
>>>>>>> 22e59a1 (Udate the bluetooth scan add search option)
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bluetoothsignalmeter.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

<<<<<<< HEAD
    private lateinit var binding: ActivityMainBinding
    private lateinit var scanner: BluetoothLeScanner
    private val devices = mutableMapOf<String, Int>()
    private lateinit var adapter: DeviceAdapter

=======
    companion object {
        private const val RSSI_WINDOW_SIZE = 5
        private const val ENV_FACTOR = 2.0 // indoor
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var scanner: BluetoothLeScanner? = null
    private lateinit var adapter: DeviceAdapter

    private val devices = mutableMapOf<String, BleDevice>()
    private var scanning = false
    private var searchQuery = ""

>>>>>>> 22e59a1 (Udate the bluetooth scan add search option)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val manager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
<<<<<<< HEAD
        scanner = manager.adapter.bluetoothLeScanner
=======
        bluetoothAdapter = manager.adapter
        scanner = bluetoothAdapter.bluetoothLeScanner
>>>>>>> 22e59a1 (Udate the bluetooth scan add search option)

        adapter = DeviceAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

<<<<<<< HEAD
        binding.btnScan.setOnClickListener {
            startScan()
        }
    }

    private fun startScan() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                101
            )
            return
        }

        scanner.startScan(object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                devices[result.device.address] = result.rssi
                adapter.update(devices)
            }
        })
=======
        // Initial Empty State
        updateEmptyState(true)

        binding.btnScan.setOnClickListener {
            if (scanning) {
                scanner?.stopScan(scanCallback)
                binding.btnScan.text = "Start Scan"
            } else {
                startScan()
                binding.btnScan.text = "Stop Scan"
            }
            scanning = !scanning
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s.toString()
                filterAndAdapterUpdate()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun startScan() {
        val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        val notGranted = permissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, notGranted.toTypedArray(), 1001)
            return
        }

        devices.clear()
        filterAndAdapterUpdate()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setLegacy(true)
            .build()

        scanner?.startScan(null, settings, scanCallback)
    }

    private fun filterAndAdapterUpdate() {
        val filteredList = devices.values.filter { device ->
            val query = searchQuery.lowercase()
            (device.name.lowercase().contains(query) ||
                    device.mac.lowercase().contains(query))
        }
        adapter.update(filteredList)
        updateEmptyState(filteredList.isEmpty())
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.txtEmpty.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
            
            if (scanning) {
                binding.txtEmpty.text = "Scanning for devices..."
            } else {
                if (searchQuery.isNotEmpty()) {
                    binding.txtEmpty.text = "No devices match your search."
                } else {
                    binding.txtEmpty.text = "No devices found.\nTap 'Start Scan' to begin."
                }
            }
        } else {
            binding.txtEmpty.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

    private fun smoothRssi(device: BleDevice, newRssi: Int): Double {
        val history = device.rssiHistory
        history.add(newRssi)

        if (history.size > RSSI_WINDOW_SIZE) {
            history.removeAt(0)
        }

        return history.average()
    }

    private fun calculateDistance(txPower: Int?, smoothRssi: Double): Double? {
        if (txPower == null) return null
        val raw = Math.pow(10.0, (txPower - smoothRssi) / (10 * ENV_FACTOR))
        return String.format("%.2f", raw).toDouble()
    }


    private fun formatDistance(d: Double?): Double? {
        return d?.let { String.format("%.2f", it).toDouble() }
    }


    private val scanCallback = object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            runOnUiThread {

                val record = result.scanRecord ?: return@runOnUiThread
                val rawBytes = record.bytes
                val rawHex = rawBytes.joinToString(" ") { "%02X".format(it) }

                val packetType = when (callbackType) {
                    ScanSettings.CALLBACK_TYPE_ALL_MATCHES -> "ADV_IND"
                    else -> "UNKNOWN"
                }

                var companyId: String? = null
                var isIBeacon = false
                var uuid: String? = null
                var major: Int? = null
                var minor: Int? = null
                var txPower: Int? = null

                // Manufacturer specific data
                val mData = record.manufacturerSpecificData
                if (mData.size() > 0) {
                    val key = mData.keyAt(0)
                    companyId = String.format("0x%04X", key)

                    val data = mData.valueAt(0)

                    // iBeacon check: 0x02 0x15
                    if (data.size >= 23 && data[0] == 0x02.toByte() && data[1] == 0x15.toByte()) {
                        isIBeacon = true

                        val uuidBytes = data.copyOfRange(2, 18)
                        uuid = uuidBytes.joinToString("") { "%02x".format(it) }
                            .replaceFirst(
                                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})".toRegex(),
                                "$1-$2-$3-$4-$5"
                            )

                        major = ((data[18].toInt() and 0xFF) shl 8) + (data[19].toInt() and 0xFF)
                        minor = ((data[20].toInt() and 0xFF) shl 8) + (data[21].toInt() and 0xFF)
                        txPower = data[22].toInt()
                    }
                }

                val mac = result.device.address
                val rssi = result.rssi

                val existing = devices[mac]

                val deviceName =
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                        ActivityCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        result.device.name ?: "Unknown"
                    } else {
                        "Unknown"
                    }

                if (existing != null) {

                    // Update RSSI + smoothing
                    existing.rssi = rssi
                    existing.smoothRssi = smoothRssi(existing, rssi)

                    // Update distance using smoothed RSSI
                    existing.distance = calculateDistance(existing.txPower, existing.smoothRssi)

                    existing.rawData = rawHex
                    existing.packetType = packetType
                    existing.companyId = companyId

                } else {

                    val tempDevice = BleDevice(
                        name = deviceName,
                        mac = mac,
                        rssi = rssi,
                        smoothRssi = rssi.toDouble(),
                        rawData = rawHex,
                        packetType = packetType,
                        companyId = companyId,
                        isIBeacon = isIBeacon,
                        uuid = uuid,
                        major = major,
                        minor = minor,
                        txPower = txPower
                    )

                    tempDevice.rssiHistory.add(rssi)
                    tempDevice.distance = calculateDistance(txPower, tempDevice.smoothRssi)

                    devices[mac] = tempDevice
                }

                filterAndAdapterUpdate()
            }
        }
>>>>>>> 22e59a1 (Udate the bluetooth scan add search option)
    }
}
