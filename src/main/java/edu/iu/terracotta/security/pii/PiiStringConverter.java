package edu.iu.terracotta.security.pii;

import org.springframework.stereotype.Component;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;

/**
 * Stores a PII string column encrypted. Hibernate gets this converter from Spring, so the
 * configured keys are injected.
 */
@Component
@Converter
@RequiredArgsConstructor
public class PiiStringConverter implements AttributeConverter<String, String> {

    private final PiiCipher piiCipher;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return piiCipher.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return piiCipher.decrypt(dbData);
    }

}
