package com.rappidrive.application.ports.output;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CurrentUserPort {

    Optional<CurrentUser> getCurrentUser();

    record CurrentUser(UUID userId, String username, List<String> roles, List<String> scopes) {
    }
}
