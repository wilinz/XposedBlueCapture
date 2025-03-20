package com.wilinz.xposedbluecapture

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
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
                            val bluetoothGattCharacteristic: BluetoothGattCharacteristic =
                                param.args[0] as BluetoothGattCharacteristic

                            val value: ByteArray = param.args[1] as ByteArray

                            //     /** Write characteristic, requesting acknowledgement by the remote device */
                            //    public static final int WRITE_TYPE_DEFAULT = 0x02;
                            //
                            //    /** Write characteristic without requiring a response by the remote device */
                            //    public static final int WRITE_TYPE_NO_RESPONSE = 0x01;
                            //
                            //    /** Write characteristic including authentication signature */
                            //    public static final int WRITE_TYPE_SIGNED = 0x04;
                            val writeType = param.args[2] as Int

                            // 获取服务 UUID
                            val serviceUuid = bluetoothGattCharacteristic.service.uuid

                            val hex = value.joinToString(" ") { String.format("%02x", it) }
                            XposedBridge.log(
                                "writeCharacteristic hex: $hex utf8: $value writeType: $writeType gatt-char: ${bluetoothGattCharacteristic.uuid} service: $serviceUuid"
                            )
                        }
                    })

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

                            val gatt = param?.args?.get(0) as BluetoothGatt

                            val bluetoothGattCharacteristic: BluetoothGattCharacteristic =
                                param.args[1] as BluetoothGattCharacteristic

                            val value = param.args[2] as ByteArray
                            val status = param.args[3] as Int // example: BluetoothGatt.GATT_SUCCESS

                            // 获取服务 UUID
                            val serviceUuid = bluetoothGattCharacteristic.service.uuid

                            val hex = value.joinToString(" ") { String.format("%02x", it) }
                            XposedBridge.log(
                                "readCharacteristic hex: $hex utf8: $value status: $status gatt-char: ${bluetoothGattCharacteristic.uuid} service: $serviceUuid"
                            )

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
