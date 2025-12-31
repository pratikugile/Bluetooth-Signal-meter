package com.example.bluetoothsignalmeter

data class BleDevice(
    val name: String,
    val mac: String,

    // Raw RSSI
    var rssi: Int,

    // Smoothed RSSI
    var smoothRssi: Double,

    // Analyzer
    var rawData: String,
    var packetType: String,
    var companyId: String? = null,

    // iBeacon
    var isIBeacon: Boolean = false,
    var uuid: String? = null,
    var major: Int? = null,
    var minor: Int? = null,
    var txPower: Int? = null,

    // Distance
    var distance: Double? = null,

    // UI State
    var isExpanded: Boolean = false,

    // RSSI history (for smoothing)
    var rssiHistory: MutableList<Int> = mutableListOf()
)
