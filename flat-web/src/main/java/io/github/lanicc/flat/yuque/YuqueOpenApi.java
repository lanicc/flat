package io.github.lanicc.flat.yuque;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import io.github.lanicc.flat.model.yuque.DocDescribe;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <a href="https://www.yuque.com/yuque/developer/api">语雀开放平台</a>
 * <p>
 * Created on 2022/10/24.
 *
 * @author lan
 */
@Slf4j
public class YuqueOpenApi {

    // private static final String accessToken = "kF0gzLFpJC8ELl4k7nZ6vXTDsxnaepKHly9wgfMN";
    private static final String AUTH_TOKEN_KEY = "X-Auth-Token";
    private final RestTemplate restTemplate;


    private final String apiUrl;
    private final String accessToken;

    private final String url;

    public YuqueOpenApi(String url, String accessToken) {
        this.url = url;
        this.apiUrl = url + "/api/v2";
        this.accessToken = accessToken;
        this.restTemplate = buildRestTemplate();
    }

    private RestTemplate buildRestTemplate() {
        SimpleClientHttpRequestFactory httpRequestFactory =
                new SimpleClientHttpRequestFactory() {
                    @NotNull
                    @Override
                    public ClientHttpRequest createRequest(@NotNull URI uri, @NotNull HttpMethod httpMethod) throws IOException {
                        ClientHttpRequest request = super.createRequest(uri, httpMethod);
                        request.getHeaders().add(AUTH_TOKEN_KEY, accessToken);
                        return request;
                    }

                };

        Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder = new Jackson2ObjectMapperBuilder();
        jackson2ObjectMapperBuilder.serializationInclusion(JsonInclude.Include.NON_NULL);
        jackson2ObjectMapperBuilder.propertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        jackson2ObjectMapperBuilder.failOnUnknownProperties(false);
        ObjectMapper mapper = jackson2ObjectMapperBuilder.build();
        HttpMessageConverter<Object> jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter(mapper);

        RestTemplate rt = new RestTemplate(Collections.singletonList(jackson2HttpMessageConverter));
        rt.setRequestFactory(httpRequestFactory);
        return rt;
    }

    public JSONObject user() {
        return restTemplate.getForObject(apiUrl + "/user", JSONObject.class);
    }

    public JSONObject groups(String userId) {
        return restTemplate.getForObject(apiUrl + "/users/" + userId + "/groups", JSONObject.class);
    }

    public JSONObject groupRepo(String groupId) {
        return restTemplate.getForObject(apiUrl + "/groups/" + groupId + "/repos", JSONObject.class);
    }

    public DocDescribe.Result repoDocs(String namespace) {
        return restTemplate.getForObject(apiUrl + "/repos/" + namespace + "/docs", DocDescribe.Result.class);
    }

    public JSONObject repoDoc(String namespace, String slug) {
        return restTemplate.getForObject(apiUrl + "/repos/" + namespace + "/docs/" + slug, JSONObject.class);
    }

    public JSONObject search0(String q, String type) {
        return restTemplate.getForObject(apiUrl + "/search?q=" + q + "&type=" + type, JSONObject.class);
    }
    public SearchResult search(String q, String type) {
        return restTemplate.getForObject(apiUrl + "/search?q=" + q + "&type=" + type, SearchResult.class);
    }
    public List<SearchResultItem> searchDoc(String q) {
        SearchResult result = search(q, "doc");
        assert result != null;
        List<SearchResultItem> resultItems = result.getData();
        if (CollectionUtils.isNotEmpty(resultItems)) {
            for (SearchResultItem resultItem : resultItems) {
                resultItem.setUrl(url + resultItem.getUrl());
                if (!StringUtils.contains(resultItem.getInfo(), resultItem.getTitle())) {
                    resultItem.setInfo(resultItem.getInfo() + "#" + resultItem.getTitle());
                }
            }
            return resultItems.stream()
                    .distinct()
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }


    @Data
    public static class SearchResult{

        private List<SearchResultItem> data;
    }
    @Data
    public static class SearchResultItem {

        private String title;

        private String url;

        private String info;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SearchResultItem item = (SearchResultItem) o;
            return Objects.equals(title, item.title) && Objects.equals(url, item.url) && Objects.equals(info, item.info);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title, url, info);
        }
    }
}
