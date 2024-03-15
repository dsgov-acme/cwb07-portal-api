package io.nuvalence.web.portal.service.controllers;

import io.nuvalence.auth.token.TokenSignOutService;
import io.nuvalence.web.portal.service.generated.controllers.SignOutApiDelegate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Controller handling web session sign out.
 */
@Service
@RequiredArgsConstructor
public class SignOutApiDelegateImpl implements SignOutApiDelegate {
    private final TokenSignOutService tokenSignOutService;

    @Override
    public ResponseEntity<Void> signOut() {
        tokenSignOutService.signOut();

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
