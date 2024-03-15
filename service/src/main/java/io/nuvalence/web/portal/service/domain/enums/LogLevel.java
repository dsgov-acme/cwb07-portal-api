package io.nuvalence.web.portal.service.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Logging levels.
 */
public enum LogLevel {
    TRACE("TRACE"),
    DEBUG("DEBUG"),
    INFO("INFO"),
    WARN("WARN"),

    ERROR("ERROR");

    private String value;

    LogLevel(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Constructs a new LogLevel with the given string value.
     * @param value the string value to be converted to an enum value
     * @return an element from the enum
     * @throws IllegalArgumentException if the given value does not match any of the enum values
     */
    @JsonCreator
    public static LogLevel fromValue(String value) {
        for (LogLevel logLevel : LogLevel.values()) {
            if (logLevel.value.equals(value)) {
                return logLevel;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}
