package com.springjwt.services.Token;

import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {
    private final Map<String, Date> blacklistedTokens = new ConcurrentHashMap<>();
    private static final long DEFAULT_BLACKLIST_DURATION = 24 * 60 * 60 * 1000;

    @Override
    public void blacklistToken(String token) {
        Date expirationDate = new Date(System.currentTimeMillis() + DEFAULT_BLACKLIST_DURATION);
        blacklistedTokens.put(token, expirationDate);
    }

    @Override
    public void blacklistToken(String token, Date expirationDate) {
        if (expirationDate == null) {
            blacklistToken(token);
            return;
        }
        blacklistedTokens.put(token, expirationDate);
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        Date expiration = blacklistedTokens.get(token);
        if (expiration == null) {
            return false;
        }

        if (expiration.before(new Date())) {
            blacklistedTokens.remove(token);
            return false;
        }

        return true;
    }

    @Override
    public void cleanupExpiredTokens() {
        Date now = new Date();
        blacklistedTokens.entrySet().removeIf(entry ->
                entry.getValue().before(now)
        );
    }
}
