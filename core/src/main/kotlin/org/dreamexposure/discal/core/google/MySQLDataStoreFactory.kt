package org.dreamexposure.discal.core.google

import com.google.api.client.util.store.AbstractDataStoreFactory
import com.google.api.client.util.store.AbstractMemoryDataStore
import com.google.api.client.util.store.DataStore
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.crypto.AESEncryption
import org.dreamexposure.discal.core.database.DatabaseManager
import org.json.JSONObject
import java.io.Serializable

class MySQLDataStoreFactory(val credentialNumber: Int) : AbstractDataStoreFactory() {
    private val aes = AESEncryption(BotSettings.CREDENTIALS_KEY.get())

    override fun <V : Serializable> createDataStore(id: String?): DataStore<V> {
        return MySQLDataStore<V>(this, id)
    }

    class MySQLDataStore<V : Serializable>(dataStoreFactory: MySQLDataStoreFactory?, id: String?) :
            AbstractMemoryDataStore<V>(dataStoreFactory, id) {
        init {
            DatabaseManager.getCredentialData(dataStoreFactory!!.credentialNumber)
                    .doOnNext { decryptAndLoad(it) }
                    .subscribe() //Dangling, can't do anything about this
        }

        override fun save() {
            val toSave = convertToEncryptedString()
            //Dangling, can't do anything about this
            DatabaseManager.updateCredentialData(dataStoreFactory.credentialNumber, toSave).subscribe()
        }

        override fun getDataStoreFactory(): MySQLDataStoreFactory {
            return super.getDataStoreFactory() as MySQLDataStoreFactory
        }

        private fun convertToEncryptedString(): String {
            //Convert the map to json
            val json = JSONObject()
            for (key in keyValueMap.keys) {
                //This should work, but I don't know the charset for sure
                val value = keyValueMap.getValue(key).toString()

                json.put(key, value)
            }

            //Encrypt the json...
            return dataStoreFactory.aes.encrypt(json.toString())
        }

        private fun decryptAndLoad(data: String) {
            //Decrypt the json
            val json = JSONObject(dataStoreFactory.aes.decrypt(data))

            //Convert json back to our dumb dumb byte array map
            for (key in json.keySet()) {
                //Convert key to string because org.json dumb sometimes
                val keyStr = key as String
                //Convert back to byte array, still don't actually know charset tho
                val value = json.getString(key).toByteArray()

                keyValueMap[keyStr] = value
            }

            //That should be everything?
        }
    }
}
