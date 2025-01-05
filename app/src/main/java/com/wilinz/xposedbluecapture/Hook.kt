package com.wilinz.xposedbluecapture

import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class Hook : IXposedHookLoadPackage {
    @Override
    @kotlin.Throws(Throwable::class)
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        val bluetooth = lpparam.classLoader.loadClass("android.bluetooth.BluetoothGatt")
        try {
            if (bluetooth != null) {
                XposedHelpers.findAndHookMethod(bluetooth, "writeCharacteristic",
                    BluetoothGattCharacteristic::class.java, object : XC_MethodHook() {
                        @kotlin.Throws(Throwable::class)
                        override fun afterHookedMethod(param: MethodHookParam) {
                            super.afterHookedMethod(param)
                            val bluetoothGattCharacteristic: BluetoothGattCharacteristic =
                                param.args.get(0) as BluetoothGattCharacteristic
                            val mValue: ByteArray? = bluetoothGattCharacteristic.value

                            var str: String = ""
                            if (mValue != null) {
                                for (i in mValue.indices) {
                                    str += String.format("%x ", mValue.get(i))
                                }
                            }
                            Log.e(
                                "packageName" + lpparam.packageName,
                                "writeCharacteristic   str :$str bluetoothGattCharacteristic" + bluetoothGattCharacteristic.getUuid()
                                    .toString()
                            )
                        }
                    })

                XposedHelpers.findAndHookMethod(bluetooth, "readCharacteristic",
                    BluetoothGattCharacteristic::class.java, object : XC_MethodHook() {
                        @kotlin.Throws(Throwable::class)
                        override fun afterHookedMethod(param: MethodHookParam) {
                            super.afterHookedMethod(param)
                            val bluetoothGattCharacteristic: BluetoothGattCharacteristic =
                                param.args.get(0) as BluetoothGattCharacteristic
                            val mValue: ByteArray? = bluetoothGattCharacteristic.value

                            var str: String = ""
                            if (mValue != null) {
                                for (i in mValue.indices) {
                                    str += String.format("%x ", mValue.get(i))
                                }
                            }
                            Log.e(
                                "packageName" + lpparam.packageName,
                                "readCharacteristic   str :$str bluetoothGattCharacteristic" + bluetoothGattCharacteristic.getUuid()
                                    .toString()
                            )
                        }
                    })
            }
        } catch (e: Exception) {
            Log.e("wanghaha", e.toString())
        }
    }
}