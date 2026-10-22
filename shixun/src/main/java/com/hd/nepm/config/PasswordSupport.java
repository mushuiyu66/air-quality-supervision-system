package com.hd.nepm.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/** 新密码用 BCrypt。库里残留的明文仍可登录，matches 为 true 后再由业务改写成哈希。 */
@Component
public class PasswordSupport {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public boolean matches(String raw, String stored) {
        if (raw == null || stored == null) {
            return false;
        }
        if (isEncoded(stored)) {
            return encoder.matches(raw, stored);
        }
        return stored.equals(raw);
    }

    public boolean isEncoded(String stored) {
        return stored != null && (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$"));
    }

    public String encode(String raw) {
        return encoder.encode(raw);
    }

    /** 已是哈希则原样返回，明文才加密。空密码不处理。 */
    public String encodeIfPlain(String raw) {
        if (raw == null || raw.isEmpty() || isEncoded(raw)) {
            return raw;
        }
        return encoder.encode(raw);
    }
}
