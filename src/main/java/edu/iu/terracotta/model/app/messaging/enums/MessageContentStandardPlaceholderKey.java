package edu.iu.terracotta.model.app.messaging.enums;

import java.util.Locale;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

public enum MessageContentStandardPlaceholderKey {

    FIRSTNAME,
    LASTNAME,
    EMAIL;

    public String key() {
        return StringUtils.lowerCase(this.toString(), Locale.US);
    }

    public MessageContentStandardPlaceholderKey find(String key) {
        return EnumUtils.getEnumIgnoreCase(MessageContentStandardPlaceholderKey.class, key);
    }

}
