package io.nuvalence.web.portal.service.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import io.nuvalence.web.portal.service.domain.enums.LogLevel;
import io.nuvalence.web.portal.service.generated.models.LogEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Handles logging needs for the logs api.
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.MoreThanOneLogger")
@Slf4j
public class LogsApiService {

    private final LoadingCache<String, Logger> cacheByContext =
            CacheBuilder.newBuilder()
                    .maximumSize(1000)
                    .expireAfterWrite(10, TimeUnit.MINUTES)
                    .build(
                            new CacheLoader<>() {
                                @Override
                                public Logger load(String key) {
                                    return LoggerFactory.getLogger(key);
                                }
                            });

    /**
     * Handles a batch of event logs.
     *
     * @param events The individual event logs
     * @return {@code true} if all logs are processed successfully,
     *     {@code false} if any errors are encountered processing one or more event logs or if
     *     there are no event logs to process.
     */
    public boolean createLogs(List<LogEvent> events) {
        boolean encounteredErrors = false;
        if (events == null || events.isEmpty()) {
            log.warn("Skipping log request as no logs were provided");
            return false;
        }

        for (LogEvent logEvent : events) {
            try {

                var context = logEvent.getContext();
                var message = logEvent.getMessage();
                var loggerName =
                        (context == null || context.trim().isEmpty())
                                ? "WEB-LOGGER"
                                : "WEB-LOGGER." + context.trim();

                var tmpLogger = cacheByContext.getUnchecked(loggerName.toUpperCase(Locale.ROOT));
                logMessage(logEvent, message, tmpLogger);

            } catch (Exception e) {
                log.error("Unable to record log from web browser", e);
                encounteredErrors = true;
            }
        }

        return !encounteredErrors;
    }

    private static void logMessage(LogEvent logEvent, String message, Logger tmpLogger) {

        Gson gson = new Gson();
        var key = "client-attributes";

        try {
            MDC.put(key, gson.toJson(getClientAttributes(logEvent)));
            switch (LogLevel.fromValue(logEvent.getLevel())) {
                case TRACE -> tmpLogger.trace(message);
                case DEBUG -> tmpLogger.debug(message);
                case INFO -> tmpLogger.info(message);
                case WARN -> tmpLogger.warn(message);
                case ERROR -> tmpLogger.error(message);
                default -> {
                    log.warn(
                            String.format(
                                    "Encountered log message with unexpected log level '%s'. ERROR"
                                            + " will be used instead.",
                                    logEvent.getLevel()));
                    tmpLogger.error((message));
                }
            }
        } finally {
            MDC.remove(key);
        }
    }

    private static Map<String, Object> getClientAttributes(LogEvent log) {
        Map<String, Object> result = new HashMap<>();
        result.put("browser-timestamp", String.valueOf(log.getTimestamp()));
        result.put("tags", log.getTags());
        result.put("keys", log.getKeys());
        return result;
    }
}
