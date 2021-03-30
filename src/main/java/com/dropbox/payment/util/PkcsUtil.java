package com.dropbox.payment.util;

import com.dropbox.payment.service.PaymentParameterService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class PkcsUtil {

    final String prodCode = "PROD";

    @Getter
    private String privateKey;
    @Getter
    private String publicKey;

    public PkcsUtil(String envMode) {
        log.info("initial keypairs");

        String cer = "";
        String priv = "";

        if(prodCode.equals(envMode)){
            cer = "123PRODSECURE16-Publickey.cer";
            priv = "priv_base64_prod";
        } else {
            cer = "123UATSECURE16-Publickey.cer";
            priv = "priv_base64";
        }

        Resource pubResource = new ClassPathResource(cer);
        Resource privResource = new ClassPathResource(priv);
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
}