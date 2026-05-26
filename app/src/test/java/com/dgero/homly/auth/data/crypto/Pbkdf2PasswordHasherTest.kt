package com.dgero.homly.auth.data.crypto

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Pbkdf2PasswordHasherTest {

    // Use low iterations to keep tests fast
    private val hasher = Pbkdf2PasswordHasher(iterations = 1_000)

    @Test
    fun `hash and verify roundtrip returns true`() {
        val salt = hasher.generateSalt()
        val hash = hasher.hash("myPassword", salt)
        assertTrue(hasher.verify("myPassword", hash, salt))
    }

    @Test
    fun `verify with wrong password returns false`() {
        val salt = hasher.generateSalt()
        val hash = hasher.hash("correctPassword", salt)
        assertFalse(hasher.verify("wrongPassword", hash, salt))
    }

    @Test
    fun `same password with different salt produces different hash`() {
        val salt1 = hasher.generateSalt()
        val salt2 = hasher.generateSalt()
        val hash1 = hasher.hash("samePassword", salt1)
        val hash2 = hasher.hash("samePassword", salt2)
        assertNotEquals(hash1, hash2)
    }

    @Test
    fun `generateSalt produces non-empty string`() {
        val salt = hasher.generateSalt()
        assertTrue(salt.isNotEmpty())
    }

    @Test
    fun `two generateSalt calls produce different values`() {
        val salt1 = hasher.generateSalt()
        val salt2 = hasher.generateSalt()
        assertNotEquals(salt1, salt2)
    }
}
