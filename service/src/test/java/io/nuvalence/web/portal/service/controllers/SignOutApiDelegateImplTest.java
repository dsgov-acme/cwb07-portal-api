package io.nuvalence.web.portal.service.controllers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import io.nuvalence.auth.token.TokenSignOutService;
import io.nuvalence.web.portal.service.generated.controllers.SignOutApiDelegate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SignOutApiDelegateImplTest {
    private SignOutApiDelegate delegate;
    private TokenSignOutService service;

    @BeforeEach
    public void setUp() {
        service = mock(TokenSignOutService.class);
        delegate = new SignOutApiDelegateImpl(service);
    }

    @Test
    void signOutDelegatesToTokenSignOutService() {
        delegate.signOut();

        Mockito.verify(service, times(1)).signOut();
    }
}
