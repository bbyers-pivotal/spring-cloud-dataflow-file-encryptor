package com.example

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.security.AlgorithmParameters
import java.security.SecureRandom
import java.security.spec.KeySpec

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

//copied from http://javapapers.com/java/java-file-encryption-decryption-using-aes-password-based-encryption-pbe/

@Service
class EncryptionService {

    @Autowired
    private EncryptorOptionsMetadata options

    public Map encrypt(InputStream inputStream) {

        String password = options.password

        if (!password) {
            throw new RuntimeException('no password set')
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()

        byte[] salt = new byte[8];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(salt);

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKey secretKey = factory.generateSecret(keySpec);
        SecretKey secret = new SecretKeySpec(secretKey.getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        AlgorithmParameters params = cipher.getParameters();

        byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();

        //inputStream encryption
        byte[] input = new byte[64]
        int bytesRead

        while ((bytesRead = inputStream.read(input)) != -1) {
            byte[] output = cipher.update(input, 0, bytesRead)
            if (output != null)
                outputStream.write(output)
        }

        byte[] output = cipher.doFinal()
        if (output != null)
            outputStream.write(output)

        inputStream.close()
        outputStream.close()
        return [encryptedBytes: outputStream.toByteArray(), salt: salt, iv: iv]
    }

    public byte[] decrypt(InputStream inputStream, salt, iv) {

        String password = options.password

        if (!password) {
            throw new RuntimeException('no password set')
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()

        SecretKeyFactory factory = SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA1")
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256)
        SecretKey tmp = factory.generateSecret(keySpec)
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES")

        // inputStream decryption
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv))

        byte[] inBytes = new byte[64]
        int read
        while ((read = inputStream.read(inBytes)) != -1) {
            byte[] output = cipher.update(inBytes, 0, read)
            if (output != null)
                outputStream.write(output)
        }

        byte[] output = cipher.doFinal()
        if (output != null)
            outputStream.write(output)

        inputStream.close()
        outputStream.close()
        return outputStream.toByteArray()
    }
}