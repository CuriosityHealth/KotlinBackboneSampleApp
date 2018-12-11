package org.researchstack.kotlinbackbonesampleapp

import android.app.Application
import android.arch.lifecycle.DefaultLifecycleObserver
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.ProcessLifecycleOwner
import android.support.v4.os.CancellationSignal
import android.text.format.DateUtils
import org.researchstack.feature.storage.StorageAccess
import org.researchstack.feature.storage.database.AppDatabase
import org.researchstack.feature.storage.database.sqlite.DatabaseHelper
import org.researchstack.feature.storage.database.sqlite.SqlCipherDatabaseHelper
import org.researchstack.feature.storage.database.sqlite.UpdatablePassphraseProvider
import org.researchstack.feature.storage.file.SimpleFileAccess
import org.researchstack.feature.storage.file.UnencryptedProvider
import org.researchstack.feature.storage.file.aes.AesProvider

import net.sqlcipher.database.SQLiteDatabase
import org.researchstack.feature.authentication.pincode.PasscodeAuthenticator
import org.researchstack.feature.authentication.pincode.PinCodeConfig
import org.researchstack.feature.authentication.pincode.PinCodeConfigProvider
import org.researchstack.feature.storage.StorageAccessPasscodeStore
import org.researchstack.foundation.components.utils.LogExt

public class BackboneLifecycleObserver: DefaultLifecycleObserver {

    override fun onResume(owner: LifecycleOwner) {
        val passcodeAuthenticator: PasscodeAuthenticator = BackboneApplication.instance.passcodeAuthenticator!!

        if (passcodeAuthenticator.isRegistered()) {
            val cancelationSignal = CancellationSignal()

            cancelationSignal.setOnCancelListener {

            }

            val callback = object: PasscodeAuthenticator.PasscodeAuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: PasscodeAuthenticator.PasscodeAuthenticationResult) {

                }
            }

            passcodeAuthenticator.authenticate(cancelationSignal, callback)
        }

    }

}

public class BackboneApplication : Application() {

//    var pinCodeProvider: PinCodeConfigProvider
    var passcodeAuthenticator: PasscodeAuthenticator? = null
    val lifecycleObserver = BackboneLifecycleObserver()

    companion object {

        val TAG = BackboneApplication.javaClass.name

        private var _instance: BackboneApplication? = null
        val instance: BackboneApplication
            get() = this._instance!!

        fun configure(instance: BackboneApplication) {
            this._instance = instance
        }
    }

    override fun onCreate() {
        super.onCreate()

        BackboneApplication.configure(this)

        val pinCodeConfig = PinCodeConfig(PinCodeConfig.PinCodeType.AlphaNumeric, 6, 5 * DateUtils.MINUTE_IN_MILLIS)
        PinCodeConfigProvider.config(pinCodeConfig)

//        val encryptionProvider = UnencryptedProvider()
        val encryptionProvider = AesProvider()
        val fileAccess = SimpleFileAccess()

//        val database: AppDatabase = DatabaseHelper(
//                this,
//                DatabaseHelper.DEFAULT_NAME,
//                null,
//                DatabaseHelper.DEFAULT_VERSION
//        )

        SQLiteDatabase.loadLibs(this)

        val database = SqlCipherDatabaseHelper(
                this,
                DatabaseHelper.DEFAULT_NAME,
                null,
                DatabaseHelper.DEFAULT_VERSION,
                UpdatablePassphraseProvider()
        )

        StorageAccess.getInstance().init(pinCodeConfig, encryptionProvider, fileAccess, database)

        val passcodeStore = StorageAccessPasscodeStore(this)
        this.passcodeAuthenticator = PasscodeAuthenticator(pinCodeConfig, passcodeStore)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this.lifecycleObserver)
    }

}