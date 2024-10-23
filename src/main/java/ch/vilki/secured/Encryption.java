/*
package ch.vilki.secured;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;


public class Encryption {
        // The higher the number of iterations the more
        // expensive computing the hash is for us and
        // also for an attacker.
        private static final int iterations = 500*1000;
        private static final int saltLen = 64;
        private static final int desiredKeyLen = 256;

        private static final Charset ENCODING=Charset.forName("UTF-8");

        private static String algorithm = "AES/CBC/PKCS5Padding";

        private static final String CHAR_LIST =
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890;.,!+*%&()=?@#§";


        public  String getSaltedHash(char[] password) throws NoSuchAlgorithmException, InvalidKeySpecException {
            byte[] salt = SecureRandom.getInstanceStrong().generateSeed(saltLen);
            return Base64.encodeBase64String(salt) + "$" + hash(password, salt);
        }

        */
/** Checks whether given plaintext password corresponds
         to a stored salted hash of the password. *//*

        public boolean check(SecureString password, SecureString stored) throws Exception{
            String[] saltAndPass = stored.toString().split("\\$");
            if (saltAndPass.length != 2) {
                throw new IllegalStateException(
                        "The stored password have the form 'salt$hash'");
            }
            String hashOfInput = hash(password, Base64.decodeBase64(saltAndPass[0]));
            return hashOfInput.equals(saltAndPass[1]);
        }

        // using PBKDF2 from Sun, an alternative is https://github.com/wg/scrypt
        // cf. http://www.unlimitednovelty.com/2012/03/dont-use-bcrypt.html
        private String hash(SecureString password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
            if (password == null || password.get_value() == null || password.get_value().length == 0)
                throw new IllegalArgumentException("Empty passwords are not supported.");
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            SecretKey key = f.generateSecret(new PBEKeySpec(
                    password.get_value(), salt, iterations, desiredKeyLen)
            );
            return Base64.encodeBase64String(key.getEncoded());
        }

        private  String hash(char[] password, byte[] salt) throws InvalidKeySpecException, NoSuchAlgorithmException {
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            SecretKey key = f.generateSecret(new PBEKeySpec(
                    password, salt, iterations, desiredKeyLen)
            );
            return Base64.encodeBase64String(key.getEncoded());
        }

        public  SecureString getRandomPassword(int length)
        {
            if(length < 8) return null;
            Random random = new Random();

            char[] pass = new char[length];
            for(int i=0; i< length; i++)
            {
                pass[i] = CHAR_LIST.charAt(random.nextInt(CHAR_LIST.length()));
            }
            return new SecureString(pass);
        }


        public  SecretKey generateKey(int n) throws NoSuchAlgorithmException {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(n);
            SecretKey key = keyGenerator.generateKey();
            return key;
        }

        public  SecretKey getKeyFromPassword(String password, String salt)
                throws NoSuchAlgorithmException, InvalidKeySpecException {

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
            SecretKey secret = new SecretKeySpec(factory.generateSecret(spec)
                    .getEncoded(), "AES");
            return secret;
        }

        public  IvParameterSpec generateIv() {
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            return new IvParameterSpec(iv);
        }

        public  String encrypt(SecureString strClearText,SecureString strKey) throws NoSuchAlgorithmException, InvalidKeySpecException,
                NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException,
                BadPaddingException, IllegalBlockSizeException {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            // Derive the key from the password and salt
            SecretKey key = getKeyFromPassword(strKey.toString(), new String(salt));
            // Initialize the cipher
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec spec = generateIv();
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);
            byte[] cipherText = cipher.doFinal(strClearText.getBytes(ENCODING));
            // Prepend salt and IV to the cipher text
            byte[] iv = spec.getIV();
            byte[] combined = new byte[salt.length + iv.length + cipherText.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(iv, 0, combined, salt.length, iv.length);
            System.arraycopy(cipherText, 0, combined, salt.length + iv.length, cipherText.length);
            return java.util.Base64.getEncoder().encodeToString(combined);
        }


        public  SecureString decrypt(String strEncrypted,SecureString strKey) throws NoSuchPaddingException, NoSuchAlgorithmException,
                InvalidAlgorithmParameterException, InvalidKeyException,
                BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException {
            byte[] decoded = java.util.Base64.getDecoder().decode(strEncrypted);
            // Assuming salt and IV are each 16 bytes
            byte[] salt = Arrays.copyOfRange(decoded, 0, 16);
            byte[] iv = Arrays.copyOfRange(decoded, 16, 32);
            byte[] encryptedText = Arrays.copyOfRange(decoded, 32, decoded.length);
            // Derive the key from the password and extracted salt
            SecretKey key = getKeyFromPassword(strKey.toString(), new String(salt));
            // Initialize the cipher for decryption
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec spec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            byte[] plainText = cipher.doFinal(encryptedText);
            return new SecureString(new String(plainText));
        }

    }

*/
