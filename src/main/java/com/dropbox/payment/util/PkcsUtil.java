package com.dropbox.payment.util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class PkcsUtil {

    private static PkcsUtil instance;

    @Getter
    private String privateKey;
    @Getter
    private String publicKey;

    private PkcsUtil() {
        log.info("initial keypairs");
        //My public cert
        //pubResource = new ClassPathResource("cert.crt");
        //Use UAT public cert
        Resource pubResource = new ClassPathResource("123UATSECURE16-Publickey.cer");
        Resource privResource = new ClassPathResource("priv_base64");
        if (null == privResource || null == pubResource) {
            throw new IllegalStateException("Encryption keypair not found!");
        }
        try (Reader reader = new InputStreamReader(privResource.getInputStream(), UTF_8)) {
            privateKey = FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException("Encryption keypair not found!");
        }

        try (Reader reader = new InputStreamReader(pubResource.getInputStream(), UTF_8)) {
            publicKey = FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException("Encryption keypair not found!");
        }
    }

    public static PkcsUtil getInstance() {
        if (instance == null) {
            instance = new PkcsUtil();
        }
        return instance;
    }
}