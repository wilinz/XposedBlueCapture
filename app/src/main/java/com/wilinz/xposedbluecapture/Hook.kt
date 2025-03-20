package com.wilinz.xposedbluecapture

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

class Hook : IXposedHookLoadPackage {

    // 分别为 read 和 write 创建线程安全的 Set
    private val printedWriteSet: MutableSet<String> = Collections.newSetFromMap(ConcurrentHashMap())
    private val printedReadSet: MutableSet<String> = Collections.newSetFromMap(ConcurrentHashMap())

    @Throws(Throwable::class)
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        val bluetooth = lpparam.classLoader.loadClass("android.bluetooth.BluetoothGatt")
        XposedBridge.log("hello BluetoothGatt")
        try {
            if (bluetooth != null) {
                // writeCharacteristic hook
                XposedHelpers.findAndHookMethod(
                    bluetooth,
                    "writeCharacteristic",
                    BluetoothGattCharacteristic::class.java,
                    ByteArray::class.java,
                    Int::class.java,
                    object : XC_MethodHook() {
                        @Throws(Throwable::class)
                        override fun afterHookedMethod(param: MethodHookParam) {
                            super.afterHookedMethod(param)
                            val characteristic = param.args[0] as BluetoothGattCharacteristic
                            val value = param.args[1] as ByteArray
                            val writeType = param.args[2] as Int

                            val serviceUuid = characteristic.service.uuid
                            val charUuid = characteristic.uuid
                            val hex = value.joinToString(" ") { String.format("%02x", it) }

                            val key = "service:$serviceUuid | char:$charUuid | hex:$hex"

                            if (printedWriteSet.add(key)) { // 只影响 write
                                XposedBridge.log(
                                    "writeCharacteristic: len: ${value.size} | hex: $hex | utf8: ${value.toString(Charsets.UTF_8)} | writeType: $writeType | gatt-char: $charUuid | service: $serviceUuid"
                                )
                            }
                        }
                    })

                // onCharacteristicRead hook
                XposedHelpers.findAndHookMethod(
                    "android.bluetooth.BluetoothGattCallback",
                    lpparam.classLoader,
                    "onCharacteristicRead",
                    BluetoothGatt::class.java,
                    BluetoothGattCharacteristic::class.java,
                    ByteArray::class.java,
                    Int::class.java,
                    object : XC_MethodHook() {
                        @Throws(Throwable::class)
                        override fun beforeHookedMethod(param: MethodHookParam?) {
                            super.beforeHookedMethod(param)

                            val characteristic = param?.args?.get(1) as BluetoothGattCharacteristic
                            val value = param.args?.get(2) as ByteArray
                            val status = param.args?.get(3) as Int
                            val serviceUuid = characteristic.service.uuid
                            val charUuid = characteristic.uuid
                            val hex = value.joinToString(" ") { String.format("%02x", it) }

                            val key = "service:$serviceUuid | char:$charUuid | hex:$hex"

                            if (printedReadSet.add(key)) { // 只影响 read
                                XposedBridge.log(
                                    "onCharacteristicRead: len: ${value.size} | hex: $hex | utf8: ${value.toString(Charsets.UTF_8)} | status: $status | gatt-char: $charUuid | service: $serviceUuid"
                                )
                            }
                        }
                    })
            }
        } catch (e: Exception) {
            XposedBridge.log("BluetoothGatt Capture Exception: $e")
        }
    }
}



//private val bluetoothGattCallback = object : BluetoothGattCallback() {
//
//    override fun onCharacteristicRead(
//        gatt: BluetoothGatt,
//        characteristic: BluetoothGattCharacteristic,
//        value: ByteArray,
//        status: Int
//    ) {
//        super.onCharacteristicRead(gatt, characteristic, value, status)
//    }
//
//}
