package io.github.lanicc.flat.elastic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created on 2022/10/24.
 *
 * @author lan
 */
class ElasticsearchServiceTest {

        ElasticsearchService elasticsearchService = new ElasticsearchService();
    @Test
    void listItems() {
        System.out.println(elasticsearchService.listItems());
    }
}
