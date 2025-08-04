package com.example.hospitalcare_be.util;

import com.example.hospitalcare_be.model.OtpInfo;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Component
public class OtpCache {
    private final Map<String, OtpInfo> otpStorage = new ConcurrentHashMap<>();

    public void put(String key, String otpCode, long ttlInSeconds) {
        otpStorage.put(key, new OtpInfo(otpCode, System.currentTimeMillis() + ttlInSeconds * 1000));
    }

    public String get(String key) {
        OtpInfo info = otpStorage.get(key);
        if (info == null || info.isExpired()) {
            otpStorage.remove(key);
            return null;
        }
        return info.getOtpCode();
    }

    public void remove(String key) {
        otpStorage.remove(key);
    }

}
