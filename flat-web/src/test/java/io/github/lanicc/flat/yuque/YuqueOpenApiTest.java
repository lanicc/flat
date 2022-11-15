package io.github.lanicc.flat.yuque;

import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.List;

/**
 * Created on 2022/10/24.
 *
 * @author lan
 */
class YuqueOpenApiTest {

    YuqueOpenApi openApi = new YuqueOpenApi("https://souche.yuque.com", "kF0gzLFpJC8ELl4k7nZ6vXTDsxnaepKHly9wgfMN");

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

    @Test
    void search() {
        YuqueOpenApi.SearchResult result = openApi.search("用户组件", "doc");

        System.out.println();
    }

    @Test
    void syncToEs() {
    }

    @Test
    void searchDoc() {
        List<YuqueOpenApi.SearchResultItem> resultItems = openApi.searchDoc("用户组件");
        System.out.println(resultItems);
    }

    @Test
    void search0() {
        JSONObject jsonObject = openApi.search0("reportId", "doc");
        System.out.println(jsonObject);
    }
}
