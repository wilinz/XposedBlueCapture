package com.wilinz.xposedbluecapture

import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class Hook : IXposedHookLoadPackage {
    @Throws(Throwable::class)
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        val bluetooth = lpparam.classLoader.loadClass("android.bluetooth.BluetoothGatt")
        XposedBridge.log("hello BluetoothGatt")
        try {
            if (bluetooth != null) {
                XposedHelpers.findAndHookMethod(bluetooth, "writeCharacteristic",
                    BluetoothGattCharacteristic::class.java, object : XC_MethodHook() {
                        @Throws(Throwable::class)
                        override fun afterHookedMethod(param: MethodHookParam) {
                            super.afterHookedMethod(param)
                            val bluetoothGattCharacteristic: BluetoothGattCharacteristic =
                                param.args[0] as BluetoothGattCharacteristic
                            val mValue: ByteArray? = bluetoothGattCharacteristic.value

                            val str = mValue?.joinToString(" ") { String.format("%02x", it) } ?: ""
                            XposedBridge.log(
                                "writeCharacteristic str: $str gatt char: ${bluetoothGattCharacteristic.uuid}"
                            )
                        }
                    })

                XposedHelpers.findAndHookMethod(bluetooth, "readCharacteristic",
                    BluetoothGattCharacteristic::class.java, object : XC_MethodHook() {
                        @Throws(Throwable::class)
                        override fun afterHookedMethod(param: MethodHookParam) {
                            super.afterHookedMethod(param)
                            val bluetoothGattCharacteristic: BluetoothGattCharacteristic =
                                param.args[0] as BluetoothGattCharacteristic
                            val mValue: ByteArray? = bluetoothGattCharacteristic.value

                            val str = mValue?.joinToString(" ") { String.format("%02x", it) } ?: ""
                            XposedBridge.log(
                                "readCharacteristic str: $str gatt char: ${bluetoothGattCharacteristic.uuid}"
                            )
                        }
                    })
            }
        } catch (e: Exception) {
            XposedBridge.log("BluetoothGatt Capture Exception: $e")
        }
    }
}
