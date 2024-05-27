package com.rm.loginappcompose.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class KeyStoreUtil(private val alias: String) {

    private val keyStore = KeyStore.getInstance(PROVIDER).apply {
        load(null)
    }

    private val encryptCipher get() = Cipher.getInstance(TRANSFORMATION).apply {
        init(Cipher.ENCRYPT_MODE, getKey())
    }

    private fun getDecryptCipherForIv(iv: ByteArray): Cipher {
        return Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, getKey(), IvParameterSpec(iv))
        }
    }

    private fun getKey(): SecretKey {
        val existingKey = keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey()
    }

    private fun createKey(): SecretKey {
        return KeyGenerator.getInstance(ALGORITHM).apply {
            init(
                KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(BLOCK_MODE)
                    .setEncryptionPaddings(PADDING)
                    .setUserAuthenticationRequired(false)
                    .setRandomizedEncryptionRequired(true)
                    .build()
            )
        }.generateKey()
    }

    fun encrypt(bytes: ByteArray, outputStream: OutputStream): ByteArray {
        val cipher = encryptCipher

        return outputStream.use { outStream ->
            outStream.write(cipher.iv.size)
            outStream.write(cipher.iv)

            // Support for large bytes
            var finalEncryptedBytes = ByteArray(0)
            val byteArrayInputStream = ByteArrayInputStream(bytes)
            byteArrayInputStream.use { inStream ->
                val buffer = ByteArray(CHUNK_SIZE)
                while (inStream.available() > CHUNK_SIZE) {
                    inStream.read(buffer)
                    val encryptedChunk = cipher.update(buffer)
                    outStream.write(encryptedChunk)
                    finalEncryptedBytes += encryptedChunk
                }

                // Last chunk written using doFinal() because this takes the padding into account
                val remainingBytes = inStream.readBytes()
                val lastChunk = cipher.doFinal(remainingBytes)
                outStream.write(lastChunk)
                finalEncryptedBytes += lastChunk
                finalEncryptedBytes
            }
        }
    }

    fun decrypt(inputStream: InputStream): ByteArray {
        return inputStream.use { inStream ->
            val ivSize = inStream.read()
            val iv = ByteArray(ivSize)
            inStream.read(iv)
            val cipher = getDecryptCipherForIv(iv)

            // Support for large bytes
            val byteArrayOutputStream = ByteArrayOutputStream()
            byteArrayOutputStream.use { outStream ->
                val buffer = ByteArray(CHUNK_SIZE)
                while (inStream.available() > CHUNK_SIZE) {
                    inStream.read(buffer)
                    val encryptedChunk = cipher.update(buffer)
                    outStream.write(encryptedChunk)
                }

                // Last chunk read using doFinal() because this takes the padding into account
                val remainingBytes = inStream.readBytes()
                val lastChunk = cipher.doFinal(remainingBytes)
                outStream.write(lastChunk)
                outStream.toByteArray()
            }
        }
    }

    companion object {
        private const val PROVIDER = "AndroidKeyStore"
        private const val CHUNK_SIZE = 1024 * 4  // 4 kb
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
    }
}