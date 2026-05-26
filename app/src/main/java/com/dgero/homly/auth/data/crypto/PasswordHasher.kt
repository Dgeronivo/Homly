package com.dgero.homly.auth.data.crypto

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

interface PasswordHasher {
    fun generateSalt(): String
    fun hash(password: String, salt: String): String
    fun verify(password: String, hash: String, salt: String): Boolean
}

class Pbkdf2PasswordHasher(private val iterations: Int = 120_000) : PasswordHasher {

    override fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.toHex()
    }

    override fun hash(password: String, salt: String): String {
        val saltBytes = salt.hexToBytes()
        val spec = PBEKeySpec(password.toCharArray(), saltBytes, iterations, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(spec).encoded.toHex()
    }

    override fun verify(password: String, hash: String, salt: String): Boolean =
        hash(password, salt) == hash

    private fun ByteArray.toHex() = joinToString("") { "%02x".format(it) }

    private fun String.hexToBytes(): ByteArray {
        val result = ByteArray(length / 2)
        for (i in result.indices) {
            result[i] = ((Character.digit(this[i * 2], 16) shl 4) + Character.digit(this[i * 2 + 1], 16)).toByte()
        }
        return result
    }
}
