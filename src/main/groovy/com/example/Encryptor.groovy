package com.example

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.messaging.Processor
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder


@EnableBinding(Processor)
@EnableConfigurationProperties(EncryptorOptionsMetadata)
class Encryptor {
    private static Logger logger = LoggerFactory.getLogger(Encryptor)

    private EncryptorOptionsMetadata options
    private EncryptionService encryptionService

    public Encryptor(EncryptorOptionsMetadata options, EncryptionService encryptionService) {
        this.options = options
        this.encryptionService = encryptionService
    }

    private static final String TMPDIR = System.getProperty("java.io.tmpdir")

    @ServiceActivator(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
    public Message encryptIt(Message<?> message) {
        Object payload = message.payload
        Map results = encryptionService.encrypt(new FileInputStream(payload))

        String filename = UUID.randomUUID().toString()

        FileOutputStream fos = new FileOutputStream("${TMPDIR}/${filename}")
        fos.write(results.encryptedBytes)
        fos.close()

        File file = new File("${TMPDIR}/${filename}")

        return MessageBuilder.withPayload(file)
            .copyHeadersIfAbsent(message.headers)
            .setHeader('salt', results.salt)
            .setHeader('iv', results.iv)
            .setHeader('file_name', filename)
            .build()
    }
}
