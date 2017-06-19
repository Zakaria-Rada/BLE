package org.lynxz.ble_lib

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import org.lynxz.ble_lib.config.BleConstant
import org.lynxz.ble_lib.config.BlePara
import org.lynxz.ble_lib.util.Logger

/**
 * Created by lynxz on 19/06/2017.
 * 低功耗蓝牙帮助类
 */
object BleHelper : BaseRelayHelper() {
    var mContext: Context? = null
    var mBleServiceIntent: Intent? = null
    var mBleBinder: BleService.BleBinder? = null
    val mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mBleBinder = service as BleService.BleBinder?
        }
    }

    override fun init(context: Context): Int {
        if (mContext == null) {
            Logger.logLevel = Logger.DEBUG_LEVEL
            mContext = context.applicationContext
            mBleServiceIntent = Intent(mContext, BleService::class.java)
            context.bindService(mBleServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE)
        }
        return RelayCode.SUCCESS
    }

    /**
     * 参数设定
     *
     * @param mode ble模式
     *  * 双工 [BleConstant.MODE_BOTH][BleConstant.MODE_BOTH]
     *  * 只可收 [BleConstant.MODE_PERIPHERAL_ONLY][BleConstant.MODE_PERIPHERAL_ONLY]
     *  * 只可发 [BleConstant.MODE_BOTH][BleConstant.MODE_CENTRAL_ONLY]
     * @param desKey des加密密钥,默认值为 [BleConstant.DEFAULT_DES_KEY][BleConstant.DEFAULT_DES_KEY]
     * @param adCharacteristicValue 默认值为 "" ,即表示不做过滤
     * */
    private fun updatePara(mode: Int = BleConstant.MODE_BOTH,
                           adCharacteristicValue: String = "",
                           desKey: String = BleConstant.DEFAULT_DES_KEY): Int {
        if (mode < 0 || mode > 3 || desKey.length < 8 || adCharacteristicValue.length > 20) {
            return RelayCode.ERR_PARA_INVALID
        }

        BlePara.mode = mode
        BlePara.desKey = desKey
        BlePara.adCharacteristicValue = adCharacteristicValue
        updateBleService()
        return RelayCode.SUCCESS
    }

    /**
     * 按照用户设定的新参数来更新service动作
     * */
    private fun updateBleService() {

    }

    override fun relayData(msg: String?): Int {
        if (msg?.isEmpty() ?: true) {
            Logger.d("relay data fail as msg is empty")
            return RelayCode.ERR_PARA_INVALID
        }
        return RelayCode.SUCCESS
    }

    /**
     * @param context 不能为空
     * @param mode ble模式设定
     * @param adCharacteristicValue 广播时,用于标识应用特征码的内容,不能超过20字节
     * @param desKey 内容进行des加密key,不少于8字节
     * */
    data class Builder @JvmOverloads constructor(var context: Context,
                                                 var mode: Int = BleConstant.MODE_BOTH,
                                                 var adCharacteristicValue: String = "",
                                                 var desKey: String = BleConstant.DEFAULT_DES_KEY) {
        fun build(): BleHelper {
            with(BleHelper) {
                init(context)
                updatePara(mode, adCharacteristicValue, desKey)
            }
            return BleHelper
        }
    }
}