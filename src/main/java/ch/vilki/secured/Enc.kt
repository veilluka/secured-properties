package ch.vilki.secured

import org.apache.commons.codec.binary.Base64
import java.nio.charset.Charset
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.spec.InvalidKeySpecException
import java.util.*
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class Enc {

    val iterations: Int = 500 * 1000
    val desiredKeyLen: Int = 256

    val saltLen: Int = 64
    val ENCODING: Charset = Charset.forName("UTF-8")
    var encryption_alg = "AES"
    val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

    @Throws(InvalidKeySpecException::class, NoSuchAlgorithmException::class)
    public fun hash(password: CharArray, salt: ByteArray)=
             Base64.encodeBase64String(keyFactory.generateSecret(PBEKeySpec(password, salt, iterations, desiredKeyLen)).encoded)

    @Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class)
    fun getSaltedHash(password: CharArray?): String {
        val salt = SecureRandom.getInstanceStrong().generateSeed(saltLen)
        return Base64.encodeBase64String(salt) + "$" + hash(password!!, salt)
    }

    /** Checks whether given plaintext password corresponds
     * to a stored salted hash of the password.  */
    @Throws(Exception::class)
    fun check(password: SecureString?, stored: SecureString): Boolean {
        val saltAndPass = stored.toString().split("\\$".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        check(saltAndPass.size == 2) { "The stored password have the form 'salt\$hash'" }
        val hashOfInput = hash(password!!._value, Base64.decodeBase64(saltAndPass[0]))
        return hashOfInput == saltAndPass[1]
    }

    @Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class)
    fun getKeyFromPassword(password: String, salt: ByteArray): SecretKey {
        PBEKeySpec(password.toCharArray(), salt, iterations, desiredKeyLen).let { spec ->
            return SecretKeySpec(keyFactory.generateSecret(spec).encoded, encryption_alg)
        }
    }

    fun generateIv(): IvParameterSpec {
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        return IvParameterSpec(iv)
    }

    @Throws(
        NoSuchAlgorithmException::class,
        InvalidKeySpecException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        InvalidAlgorithmParameterException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class
    )


    fun encrypt(strClearText: String, strKey: String): String {
        // Generate a random salt based on the specified length
        val salt = ByteArray(saltLen)
        SecureRandom().nextBytes(salt)
        // Derive a key using the password and the random salt
        val key = getKeyFromPassword(strKey, salt)
        // Generate a random IV for each encryption
        val ivSpec = generateIv()
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)
        // Encrypt the clear text
        val cipherText = cipher.doFinal(strClearText.toByteArray(ENCODING))
        val iv = ivSpec.iv
        // Combine salt, IV, and cipher text into a single byte array
        val combined = ByteArray(salt.size + iv.size + cipherText.size)
        System.arraycopy(salt, 0, combined, 0, salt.size)
        System.arraycopy(iv, 0, combined, salt.size, iv.size)
        System.arraycopy(cipherText, 0, combined, salt.size + iv.size, cipherText.size)
        // Return the encoded combined byte array as a base64 string
        return java.util.Base64.getEncoder().encodeToString(combined)
    }

    fun generatePassword(lowerCase: Int=4, upperCase: Int=4, numbers: Int=4, symbols: Int=4): String {

        val lowercaseChars = "abcdefghijklmnopqrstuvwxyz".toCharArray()
        val uppercaseChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()
        val nummerChars = "0123456789".toCharArray()
        val symChars = "$?!@#%&{}[]+-*@".toCharArray()
        val random: Random = SecureRandom()
        var pass = mutableListOf<String>()
        repeat(upperCase) {pass.add(uppercaseChars[random.nextInt(uppercaseChars.size)].toString())}
        repeat(lowerCase) {pass.add(lowercaseChars[random.nextInt(lowercaseChars.size)].toString())}
        repeat(numbers) {pass.add(nummerChars[random.nextInt(nummerChars.size)].toString())}
        repeat(symbols) {pass.add(symChars[random.nextInt(symChars.size)].toString())}
        pass.shuffle(random)
        var retValue = StringBuilder()
        pass.forEach { retValue.append(it) }
        return retValue.toString()

    }

    @Throws(Exception::class)
    fun decrypt(strEncrypted: String, strKey: String): String {
        val decoded = java.util.Base64.getDecoder().decode(strEncrypted)
        // Adjust to use the configured salt length
        val salt = Arrays.copyOfRange(decoded, 0, saltLen)
        val iv = Arrays.copyOfRange(decoded, saltLen, saltLen + 16)
        val encryptedText = Arrays.copyOfRange(decoded, saltLen + 16, decoded.size)
        val key = getKeyFromPassword(strKey, salt)
        val spec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        val plainText = cipher.doFinal(encryptedText)
        return String(plainText, ENCODING)
    }
}

fun main(args: Array<String>) {
    println(Enc().getSaltedHash("justMe".toCharArray()))

    val key = "NoneOfTheAbove+1"
    val input  = "Why is noone here"
    repeat(5){
        val encrypted = Enc().encrypt(input,key)
        val decrypted = Enc().decrypt(encrypted,key)
        println(encrypted)
        println(decrypted)
    }


}