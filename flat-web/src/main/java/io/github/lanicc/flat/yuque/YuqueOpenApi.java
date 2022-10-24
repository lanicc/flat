package io.github.lanicc.flat.yuque;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.github.lanicc.flat.elastic.ElasticsearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <a href="https://www.yuque.com/yuque/developer/api">语雀开放平台</a>
 * <p>
 * Created on 2022/10/24.
 *
 * @author lan
 */
@Component
@Slf4j
public class YuqueOpenApi {

    private static final String accessToken = "kF0gzLFpJC8ELl4k7nZ6vXTDsxnaepKHly9wgfMN";

    static RestTemplate restTemplate;

    static {
        SimpleClientHttpRequestFactory httpRequestFactory =
                new SimpleClientHttpRequestFactory() {
                    @Override
                    public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
                        ClientHttpRequest request = super.createRequest(uri, httpMethod);
                        request.getHeaders().add("X-Auth-Token", accessToken);
                        return request;
                    }

                };

        restTemplate = new RestTemplate(httpRequestFactory);
    }

    public JSONObject user() {
        return restTemplate.getForObject("https://souche.yuque.com/api/v2/user", JSONObject.class);
    }

    public JSONObject groups(String userId) {
        return restTemplate.getForObject("https://souche.yuque.com/api/v2/users/" + userId + "/groups", JSONObject.class);
    }

    public JSONObject groupRepo(String groupId) {
        return restTemplate.getForObject("https://souche.yuque.com/api/v2/groups/" + groupId + "/repos", JSONObject.class);
    }

    public JSONObject repoDocs(String namespace) {
        return restTemplate.getForObject("https://souche.yuque.com/api/v2/repos/" + namespace + "/docs", JSONObject.class);
    }

    public JSONObject repoDoc(String namespace, String slug) {
        return restTemplate.getForObject("https://souche.yuque.com/api/v2/repos/" + namespace + "/docs/" + slug, JSONObject.class);
    }

    @Autowired
    private ElasticsearchService elasticsearchService;

    @PostConstruct
    public void syncToEs() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            sync("base-developer/wxz0gg");
            sync("base-developer/fr17sm");
            sync("base-developer/ip0qq6");
        });
    }

    private void sync(String namespace) {
        JSONObject repoDocs = repoDocs(namespace);
        JSONArray data = repoDocs.getJSONArray("data");
        for (Object datum : data) {
            @SuppressWarnings("unchecked") JSONObject dat = new JSONObject((Map<String, Object>) datum);
            log.info("start sync doc: {} to es", dat.getString("title"));
            JSONObject doc = repoDoc(namespace, dat.getString("slug"));
            JSONObject docJSONObject = doc.getJSONObject("data");
            elasticsearchService.index(docJSONObject, jsonObject -> jsonObject.getString("id"));
        }
    }
}
