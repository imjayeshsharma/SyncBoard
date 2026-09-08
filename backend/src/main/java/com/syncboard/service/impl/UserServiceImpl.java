// Stage 2/3 — service layer
package com.syncboard.service.impl;

import com.syncboard.api.dto.UserSummary;
import com.syncboard.api.mapper.UserMapper;
import com.syncboard.persistence.entity.UserEntity;
import com.syncboard.persistence.repository.UserRepository;
import com.syncboard.service.CurrentUserProvider;
import com.syncboard.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * {@code UserRepository.findAllByActiveTrue()} is a real finder — used directly for the
 * {@code activeOnly} case instead of loading every user via {@code findAll()} and filtering in
 * Java.
 */
@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CurrentUserProvider currentUserProvider;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, CurrentUserProvider currentUserProvider) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public List<UserSummary> listUsers(boolean activeOnly) {
        List<UserEntity> users = activeOnly ? userRepository.findAllByActiveTrue() : userRepository.findAll();
        return users.stream().map(userMapper::toSummary).toList();
    }

    @Override
    public UserSummary getCurrentUser() {
        return userMapper.toSummary(currentUserProvider.requireCurrentUser());
    }
}
