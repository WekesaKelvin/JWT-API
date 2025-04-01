package com.springjwt.services.Token;

import java.util.Date;

public interface TokenBlacklistService {
    void blacklistToken(String token);
    void blacklistToken(String token, Date expirationDate);
    boolean isTokenBlacklisted(String token);
    void cleanupExpiredTokens();
}