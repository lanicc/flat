package io.github.lanicc.flat.dingtalk;

/**
 * Created on 2022/10/27.
 *
 * @author lan
 */
public interface AccessTokenManager {

    String get(String appKey, String appSecret);

    static AccessTokenManager def() {
        return new FileCacheAccessTokenManager();
    }
}
