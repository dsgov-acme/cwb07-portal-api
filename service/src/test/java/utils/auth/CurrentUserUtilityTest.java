package utils.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import io.nuvalence.auth.token.UserToken;
import io.nuvalence.web.portal.service.utils.auth.CurrentUserUtility;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

class CurrentUserUtilityTest {

    @Test
    void getCurrentUserShouldReturnUserTokenWhenAuthenticationIsUserToken() {
        // Arrange
        UserToken mockUserToken = mock(UserToken.class);
        SecurityContextHolder.getContext().setAuthentication(mockUserToken);

        // Act
        Optional<UserToken> result = CurrentUserUtility.getCurrentUser();

        // Assert
        assertTrue(result.isPresent());
        assertEquals(mockUserToken, result.get());
    }

    @Test
    void getCurrentUserShouldReturnEmptyOptionalWhenAuthenticationIsNotUserToken() {
        // Arrange
        Authentication authentication =
                new UsernamePasswordAuthenticationToken("username", "password");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act
        Optional<UserToken> result = CurrentUserUtility.getCurrentUser();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void getCurrentUserShouldReturnEmptyOptionalWhenNoAuthentication() {
        // Arrange
        SecurityContextHolder.clearContext();

        // Act
        Optional<UserToken> result = CurrentUserUtility.getCurrentUser();

        // Assert
        assertTrue(result.isEmpty());
    }
}
