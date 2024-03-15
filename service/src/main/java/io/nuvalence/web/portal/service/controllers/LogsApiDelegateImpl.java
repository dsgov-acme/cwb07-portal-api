package io.nuvalence.web.portal.service.controllers;

import io.nuvalence.web.portal.service.generated.controllers.LogsApiDelegate;
import io.nuvalence.web.portal.service.generated.models.LogEvent;
import io.nuvalence.web.portal.service.service.LogsApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Controller handling logs.
 */
@Service
@RequiredArgsConstructor
public class LogsApiDelegateImpl implements LogsApiDelegate {

    private final LogsApiService logsApiService;

    @Override
    public ResponseEntity<Void> createLogs(List<LogEvent> events) {

        boolean batchResultSuccess = logsApiService.createLogs(events);

        if (batchResultSuccess) {
            return ResponseEntity.status(204).build();
        } else {
            return ResponseEntity.status(400).build();
        }
    }
}
