package io.github.lanicc.flat.elastic;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.xpack.client.PreBuiltXPackTransportClient;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created on 2022/10/24.
 *
 * @author lan
 */
@Component
public class ElasticsearchService {

    static TransportClient client;

    static String FLAT_ITEM = "flat_item";

    static String FLAT_YUQUE_DOCS = "flat_yuque_docs";

    static TimeValue timeValue = new TimeValue(TimeUnit.SECONDS.toMillis(5));

    static {
        EsCluster esCluster = new EsCluster(1L, "es-cn-v641cc4uw0001xtat", "es-cn-v641cc4uw0001xtat.public.elasticsearch.aliyuncs.com:9300", "xpack.security.user=elastic:6183esG5eX9Y0sVl", "123", new Date(), new Date());
        client = buildClient(esCluster);
    }

    public List<String> listItems() {
        SearchResponse response =
                client.prepareSearch(FLAT_ITEM)
                        .setTypes("doc")
                        .setQuery(
                                QueryBuilders.matchAllQuery()
                        )
                        .setFetchSource(new String[]{"item"}, new String[0])
                        .addSort(
                                SortBuilders.fieldSort("item")
                        )
                        .get(timeValue);
        SearchHit[] hits = response.getHits().getHits();
        if (ArrayUtils.isNotEmpty(hits)) {
            return Arrays.stream(hits)
                    .map(searchHitFields -> String.valueOf(searchHitFields.getSource().get("item")))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public Maintainer getByItem(String item) {
        // SearchResponse response =
        SearchRequestBuilder searchRequestBuilder =
                client.prepareSearch(FLAT_ITEM)
                        .setTypes("doc")
                        .setQuery(
                                QueryBuilders.termQuery("item", item)
                        )
                        .setSize(1);
        System.out.println(searchRequestBuilder);
        SearchResponse response = searchRequestBuilder.get(timeValue);
        SearchHit[] hits = response.getHits().getHits();
        if (ArrayUtils.isNotEmpty(hits)) {
            Map<String, Object> source = hits[0].getSource();
            return new Maintainer(String.valueOf(source.get("maintainerDingId")), String.valueOf(source.get("maintainer")));
        }
        return null;
    }

    public void index(JSONObject source, Function<JSONObject, String> idGetter) {
        IndexResponse response =
                client.prepareIndex()
                        .setIndex(FLAT_YUQUE_DOCS)
                        .setType("doc")
                        .setId(idGetter.apply(source))
                        .setSource(source)
                        .get();
        System.out.println(response.status());
    }

    public List<YuqueDocSearchResult> search(String keyword) {
        SearchResponse response =
                client.prepareSearch(FLAT_YUQUE_DOCS)
                        .setTypes("doc")
                        .setQuery(
                                QueryBuilders.multiMatchQuery(keyword, "title", "body", "body_draft", "description")
                        )
                        .setSize(5)
                        .setFetchSource(new String[]{"id", "title", "book.namespace", "slug"}, new String[0])
                        .get(timeValue);
        SearchHit[] hits = response.getHits().getHits();
        if (ArrayUtils.isNotEmpty(hits)) {
            return Arrays.stream(hits)
                    .map(hit -> {
                        Map<String, Object> source = hit.getSource();
                        //noinspection rawtypes
                        return new YuqueDocSearchResult(
                                String.valueOf(source.get("id")),
                                String.valueOf(source.get("title")),
                                String.valueOf(((Map)source.get("book")).get("namespace")),
                                String.valueOf(source.get("slug"))
                        );
                    })
                    .collect(Collectors.toList());

        }
        return Collections.emptyList();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class YuqueDocSearchResult {

        private String id;
        private String title;
        private String namespace;
        private String slug;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Maintainer {

        private String maintainerDingId;
        private String maintainer;
    }


    private static TransportClient buildClient(EsCluster esDO) {
        Properties properties = new Properties();
        if (StringUtils.isNotBlank(esDO.getContent())) {
            try {
                properties.load(new ByteArrayInputStream(esDO.getContent().getBytes(StandardCharsets.UTF_8)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Settings.Builder builder = Settings.builder();
        properties.forEach((k, v) -> builder.put(k, String.valueOf(v)));
        Settings settings =
                builder
                        .put("cluster.name", esDO.getName())
                        .build();
        TransportClient client = new PreBuiltXPackTransportClient(settings);
        String[] split = esDO.getAddress().split(":");
        client.addTransportAddress(new InetSocketTransportAddress(new InetSocketAddress(split[0], Integer.parseInt(split[1]))));
        return client;
    }

    @Data
    @AllArgsConstructor
    private static class EsCluster implements Serializable {

        private Long id;

        private String name;

        private String address;

        private String content;

        private String contentMd5;

        private Date dateCreate;

        private Date dateUpdate;

    }

}
