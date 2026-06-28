package com.ljlblogserver.service;

import com.ljlblogserver.common.BusinessException;
import com.ljlblogserver.dto.ChangePasswordRequest;
import com.ljlblogserver.dto.LoginRequest;
import com.ljlblogserver.dto.LoginResponse;
import com.ljlblogserver.dto.UserInfo;
import com.ljlblogserver.entity.User;
import com.ljlblogserver.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw BusinessException.unauthorized("用户名或密码错误");
        }
        String token = jwtService.generateToken(user.getUsername());
        return new LoginResponse(token, user.getUsername());
    }

    public UserInfo currentUser(String username) {
        return new UserInfo(username);
    }

    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw BusinessException.badRequest("当前密码不正确");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw BusinessException.badRequest("新密码不能与当前密码相同");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updatePassword(user);
    }
}
