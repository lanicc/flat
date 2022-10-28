package io.github.lanicc.flat.dingtalk;

import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2022/10/27.
 *
 * @author lan
 */
@Slf4j
public class FileCacheAccessTokenManager implements AccessTokenManager {
    @Override
    public String get(String appKey, String appSecret) {
        @SuppressWarnings("UnnecessaryLocalVariable") final String cacheKey = appKey;
        GetTokenResult cache = readCache(cacheKey);
        if (cache.getExpiresIn() > TimeUnit.SECONDS.toMillis(10)) {
            return cache.getAccessToken();
        }
        try {
            GetTokenResult result = DingtalkOpenApi.reqForAccessToken(appKey, appSecret);
            assert result != null;
            cacheAccessToken(cacheKey, result);
            return result.getAccessToken();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void cacheAccessToken(String cacheKey, GetTokenResult result) throws IOException {
        FileOutputStream out = new FileOutputStream(System.getProperty("user.home") + "/" + cacheKey + ".txt");
        DataOutputStream dataOutputStream = new DataOutputStream(out);
        dataOutputStream.writeLong(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + result.getExpiresIn());
        dataOutputStream.writeUTF(result.getAccessToken());
        dataOutputStream.flush();
        dataOutputStream.close();
    }

    private GetTokenResult readCache(String cacheKey) {
        try {
            FileInputStream in = new FileInputStream(System.getProperty("user.home") + "/" + cacheKey + ".txt");
            DataInputStream dataInputStream = new DataInputStream(in);
            long expireAt = dataInputStream.readLong() - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
            String accessToken = dataInputStream.readUTF();
            return new GetTokenResult(accessToken, expireAt);
        } catch (IOException e) {
            log.warn("read cache error", e);
            return new GetTokenResult();
        }
    }
}
