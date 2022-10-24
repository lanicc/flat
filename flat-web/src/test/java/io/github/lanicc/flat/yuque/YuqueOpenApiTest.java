package io.github.lanicc.flat.yuque;

import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created on 2022/10/24.
 *
 * @author lan
 */
class YuqueOpenApiTest {

    YuqueOpenApi openApi = new YuqueOpenApi();

    @Test
    void user() throws URISyntaxException {
        System.out.println(openApi.user());
    }

    @Test
    void groups() throws URISyntaxException {
        System.out.println(openApi.groups(openApi.user().getJSONObject("data").getString("id")));
    }

    @Test
    void groupRepo() throws URISyntaxException {
        System.out.println(openApi.groupRepo("245846"));
    }

    @Test
    void repoDocs() {
        System.out.println(openApi.repoDocs("base-developer/wxz0gg"));
    }

    @Test
    void repoDoc() {
        System.out.println(openApi.repoDoc("base-developer/wxz0gg", "fehed0"));
    }
}
