package com.ljlblogserver.config;

import com.ljlblogserver.entity.User;
import com.ljlblogserver.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties adminProperties;

    @Override
    public void run(String... args) {
        User existing = userMapper.findByUsername(adminProperties.getUsername());
        if (existing != null) {
            return;
        }
        User user = new User();
        user.setUsername(adminProperties.getUsername());
        user.setPassword(passwordEncoder.encode(adminProperties.getPassword()));
        userMapper.insert(user);
        log.info("已创建默认管理员账号: {}", adminProperties.getUsername());
    }
}
