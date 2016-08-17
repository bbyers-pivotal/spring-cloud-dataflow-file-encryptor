package com.example

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner)
@SpringBootTest
class EncryptorServiceTests {

    @Autowired
    private EncryptorOptionsMetadata options

    @Autowired
    private EncryptionService encryptionService

    @Test
    void encryptThenDecryptFile() {

        //set the password to use for encryption/decryption
        String password = 'mypassword'
        options.password = password

        byte[] content = 'mytestfile'.bytes
        InputStream input = new ByteArrayInputStream(content)

        //encrypt the file
        Map encryptedData = encryptionService.encrypt(input)

        InputStream encrypted = new ByteArrayInputStream(encryptedData.encryptedBytes)

        //decrypt the file
        byte[] decrypted = encryptionService.decrypt(encrypted, encryptedData.salt, encryptedData.iv)

        //make sure original and decrypted match
        assert content == decrypted
    }
}
