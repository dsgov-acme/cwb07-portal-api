package io.nuvalence.web.portal.service.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import io.nuvalence.web.portal.service.generated.controllers.LogsApiDelegate;
import io.nuvalence.web.portal.service.service.LogsApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class LogsApiDelegateImplTest {
    private LogsApiDelegate delegate;
    private LogsApiService service;

    @BeforeEach
    public void setUp() {
        service = mock(LogsApiService.class);
        delegate = new LogsApiDelegateImpl(service);
    }

    @Test
    void createLogsDelegatesToLogsApiService() {
        delegate.createLogs(null);
        Mockito.verify(service, times(1)).createLogs(null);
    }

    @Test
    void successfulProcessingReturnsCode204() {

        Mockito.doAnswer(invocation -> true).when(service).createLogs(any());
        var result = delegate.createLogs(null);
        assertEquals(204, result.getStatusCodeValue());
    }

    @Test
    void failedProcessingReturnsCode400() {
        Mockito.doAnswer(invocation -> false).when(service).createLogs(any());
        var result = delegate.createLogs(null);
        assertEquals(400, result.getStatusCodeValue());
    }
}
