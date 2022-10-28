package io.github.lanicc.flat.dingtalk;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created on 2022/10/27.
 *
 * @author lan
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetTokenResult {
    private String accessToken;

    private long expiresIn;

    private Integer errcode;

    private String errmsg;

    public GetTokenResult(String accessToken, long expiresIn) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
    }
}
