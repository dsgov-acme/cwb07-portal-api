package io.nuvalence.web.portal.service.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

import io.nuvalence.web.portal.service.domain.enums.LogLevel;
import io.nuvalence.web.portal.service.generated.models.LogEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

class LogsApiServiceTest {

    private LogsApiService service;
    private MockedStatic<LoggerFactory> loggerFactoryMock;
    private Logger loggerMock;

    @BeforeEach
    public void setUp() {
        service = new LogsApiService();
        loggerMock = mock(Logger.class);
        loggerFactoryMock = mockStatic(LoggerFactory.class);
        loggerFactoryMock.when(() -> LoggerFactory.getLogger(anyString())).thenReturn(loggerMock);
    }

    @AfterEach
    public void cleanUp() {
        loggerFactoryMock.close();
    }

    private LogEvent getLog(LogLevel level, String context) {
        var log = new LogEvent();
        log.setLevel(level.getValue());
        log.setContext(context);
        log.setMessage("hello world");
        return log;
    }

    private List<LogEvent> getLogs() {
        var contextBase = "context";
        var logs = new ArrayList<LogEvent>();
        logs.add(getLog(LogLevel.INFO, contextBase));
        logs.add(getLog(LogLevel.DEBUG, contextBase));
        logs.add(getLog(LogLevel.DEBUG, contextBase));
        return logs;
    }

    @Test
    void nullLogCollectionReturnsFalse() {
        assertFalse(service.createLogs(null));
    }

    @Test
    void emptyLogCollectionReturnsFalse() {
        assertFalse(service.createLogs(new ArrayList<>()));
    }

    @Test
    void nonEmptyLogCollectionWithValidLogsReturnsTrue() {
        assertTrue(service.createLogs(getLogs()));
    }

    @Test
    void loggersWithSameContextNameAreProperlyCached() {
        service.createLogs(getLogs());
        loggerFactoryMock.verify(() -> LoggerFactory.getLogger("WEB-LOGGER.CONTEXT"), times(1));
    }

    static Stream<String> contextNames() {
        return Stream.of("", "My-Context", null);
    }

    @ParameterizedTest
    @MethodSource("contextNames")
    void loggerCreationWithDifferentContextNames(String contextName) {
        var log = getLog(LogLevel.DEBUG, contextName);
        var logs = new ArrayList<LogEvent>();
        logs.add(log);
        service.createLogs(logs);

        if (contextName == null || contextName.isEmpty()) {
            loggerFactoryMock.verify(() -> LoggerFactory.getLogger("WEB-LOGGER"), times(1));
        } else {
            loggerFactoryMock.verify(
                    () ->
                            LoggerFactory.getLogger(
                                    "WEB-LOGGER." + contextName.toUpperCase(Locale.ROOT)),
                    times(1));
        }
    }

    @Test
    void nonEmptyLogCollectionWithValidLogsGeneratesSameNumberOfLogMessages() {
        service.createLogs(getLogs());
        Mockito.verify(loggerMock, times(1)).info(anyString());
        Mockito.verify(loggerMock, times(2)).debug(anyString());
    }

    @Test
    void nonEmptyLogCollectionWithAtLeastOneIncorrectLogReturnsFalse() {
        Mockito.doAnswer(
                        invocation -> {
                            throw new Exception();
                        })
                .when(loggerMock)
                .info(anyString());

        assertFalse(service.createLogs(getLogs()));
        Mockito.verify(loggerMock, times(1)).info(anyString());
        Mockito.verify(loggerMock, times(2)).debug(anyString());
    }

    /**
     * This test checks to see that there is no client-attributes in the
     * MDC prior to execution as well as post execution but checks to make
     * sure that by the time the logger is called, client-attributes
     * have been added.
     */
    @Test
    void logAttributesAreAddedToMdcContext() {
        var answer = mock(Answer.class);
        Mockito.doAnswer(answer).when(loggerMock).trace(any());
        Mockito.doAnswer(answer).when(loggerMock).info(anyString());
        Mockito.doAnswer(answer).when(loggerMock).debug(anyString());
        Mockito.doAnswer(answer).when(loggerMock).warn(anyString());
        Mockito.doAnswer(answer).when(loggerMock).error(anyString());

        assertNull(MDC.get("client-attributes"));
        assertTrue(service.createLogs(getLogs()));
        assertNull(MDC.get("client-attributes"));
    }

    @Test
    void logMessageLevelIndicatesLoggerLevelBeingUsed() {
        var log1 = getLog(LogLevel.TRACE, null);
        var log2 = getLog(LogLevel.DEBUG, null);
        var log3 = getLog(LogLevel.INFO, null);
        var log4 = getLog(LogLevel.WARN, null);
        var log5 = getLog(LogLevel.ERROR, null);

        var logs = new ArrayList<LogEvent>();

        logs.add(log1);
        logs.add(log2);
        logs.add(log3);
        logs.add(log4);
        logs.add(log5);

        service.createLogs(logs);
        Mockito.verify(loggerMock, times(1)).trace(anyString());
        Mockito.verify(loggerMock, times(1)).debug(anyString());
        Mockito.verify(loggerMock, times(1)).info(anyString());
        Mockito.verify(loggerMock, times(1)).warn(anyString());
        Mockito.verify(loggerMock, times(1)).error(anyString());
    }
}
