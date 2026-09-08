// Stage 2 — REST API controllers
package com.syncboard.api.controller;

import com.syncboard.api.dto.UserSummary;
import com.syncboard.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Contract 01-CONTRACT.md §4: {@code GET /api/v1/users} and {@code GET /api/v1/me}.
 * No logic here — delegates to {@link UserService}.
 */
@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public List<UserSummary> listUsers(@RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        return userService.listUsers(activeOnly);
    }

    @GetMapping("/me")
    public UserSummary me() {
        return userService.getCurrentUser();
    }
}
