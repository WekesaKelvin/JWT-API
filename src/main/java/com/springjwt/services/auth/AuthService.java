package com.springjwt.services.auth;

import com.springjwt.dto.SignupDTO;
import com.springjwt.dto.UserDTO;

public interface AuthService {
    UserDTO createUser(SignupDTO signupDTO);
    String generatePasswordResetToken(String email);
    boolean resetPassword(String token, String newPassword);
}
