package io.nuvalence.web.portal.service.events.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.nuvalence.events.event.RoleReportingEvent;
import io.nuvalence.events.exception.EventProcessingException;
import io.nuvalence.web.portal.service.service.CapabilitiesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(MockitoExtension.class)
class RoleReportingEventProcessorTest {

    private RoleReportingEventProcessor processor;
    private CapabilitiesService service;

    @BeforeEach
    void setUp() {
        service = mock(CapabilitiesService.class);
        processor = new RoleReportingEventProcessor(service);
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void testExecute(CapturedOutput output) throws EventProcessingException {

        RoleReportingEvent event = RoleReportingEvent.builder().name("test").build();
        processor.execute(event);

        verify(service, times(1)).updateCapabilitiesFromRolesEvent(event);

        assertTrue(output.getOut().contains("RoleReportingEvent received from test"));
    }

    @Test
    void testExecuteThrowsException() throws EventProcessingException {

        doThrow(new RuntimeException("message"))
                .when(service)
                .updateCapabilitiesFromRolesEvent(any());
        RoleReportingEvent event = RoleReportingEvent.builder().name("test").build();

        var e = assertThrows(EventProcessingException.class, () -> processor.execute(event));
        assertEquals("message", e.getMessage());
    }

    @Test
    void testGetEventClass() {
        assertEquals(RoleReportingEvent.class, processor.getEventClass());
    }
}
