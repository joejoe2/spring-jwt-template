package com.joejoe2.demo.config;

import lombok.SneakyThrows;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Component
@ConfigurationPropertiesBinding
public class PrivateKeyConverter implements Converter<String, PrivateKey> {
    @SneakyThrows
    @Override
    public PrivateKey convert(String from) {
        byte[] bytes = Base64.getDecoder().decode(from.getBytes());
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePrivate(spec);
    }
}
