package com.example

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("encryptor")
class EncryptorOptionsMetadata {
    String password
}
