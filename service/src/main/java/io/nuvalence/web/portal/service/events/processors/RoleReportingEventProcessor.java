package io.nuvalence.web.portal.service.events.processors;

import io.nuvalence.events.event.RoleReportingEvent;
import io.nuvalence.events.exception.EventProcessingException;
import io.nuvalence.events.subscriber.EventProcessor;
import io.nuvalence.web.portal.service.service.CapabilitiesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * This class is responsible for processing RoleReporting events.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RoleReportingEventProcessor implements EventProcessor<RoleReportingEvent> {

    private final CapabilitiesService capabilitiesService;

    @Override
    public Class<RoleReportingEvent> getEventClass() {
        return RoleReportingEvent.class;
    }

    @Override
    public void execute(RoleReportingEvent event) throws EventProcessingException {
        try {
            log.info(
                    RoleReportingEvent.class.getSimpleName() + " received from " + event.getName());

            capabilitiesService.updateCapabilitiesFromRolesEvent(event);

        } catch (Exception e) {
            throw new EventProcessingException(e.getMessage());
        }
    }
}
