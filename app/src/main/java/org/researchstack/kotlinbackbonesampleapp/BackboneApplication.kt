package org.researchstack.kotlinbackbonesampleapp

import android.app.Application
import org.researchstack.backbone.StorageAccess
import org.researchstack.backbone.storage.database.AppDatabase
import org.researchstack.backbone.storage.database.sqlite.DatabaseHelper
import org.researchstack.backbone.storage.file.PinCodeConfig
import org.researchstack.backbone.storage.file.SimpleFileAccess
import org.researchstack.backbone.storage.file.UnencryptedProvider

public class BackboneApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val pinCodeConfig = PinCodeConfig()
        val encryptionProvider = UnencryptedProvider()

        val fileAccess = SimpleFileAccess()

        val database: AppDatabase = DatabaseHelper(
                this,
                DatabaseHelper.DEFAULT_NAME,
                null,
                DatabaseHelper.DEFAULT_VERSION
        )

        StorageAccess.getInstance().init(pinCodeConfig, encryptionProvider, fileAccess, database)

    }
}