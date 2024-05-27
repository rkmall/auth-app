package com.rm.loginappcompose.data.model

import android.util.Log
import androidx.datastore.core.Serializer
import com.rm.loginappcompose.util.KeyStoreUtil
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

class UserSerializer(private val keyStoreUtil: KeyStoreUtil) : Serializer<UserInfo> {
    override val defaultValue: UserInfo
        get() = UserInfo()

    override suspend fun readFrom(input: InputStream): UserInfo {
       val decryptedBytes = keyStoreUtil.decrypt(input)
        return try {
            Json.decodeFromString(
                deserializer = UserInfo.serializer(),
                string = decryptedBytes.decodeToString())
        } catch (e: SerializationException) {
            Log.d("Serialization", "${e.printStackTrace()}")
            defaultValue
        }
    }

    override suspend fun writeTo(t: UserInfo, output: OutputStream) {
        keyStoreUtil.encrypt(
            bytes = Json.encodeToString(UserInfo.serializer(), t).encodeToByteArray(),
            outputStream = output
        )
    }
}