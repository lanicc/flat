package io.github.lanicc.flat.mapper;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.lanicc.flat.FlatWebApplicationTests;
import io.github.lanicc.flat.model.ServiceGroup;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created on 2022/10/27.
 *
 * @author lan
 */
class ServiceGroupMapperTest extends FlatWebApplicationTests {

    @Autowired
    private ServiceGroupMapper serviceGroupMapper;

    @Test
    void insert() throws IOException {
        ClassPathResource resource = new ClassPathResource("setup/config-example.json");
        ServiceGroup.Config serviceGroupConfig = JSON.parseObject(resource.getInputStream(), ServiceGroup.Config.class);
        assertNotNull(serviceGroupConfig);
        ServiceGroup serviceGroup = new ServiceGroup();
        serviceGroup.setName("测试服务群1");
        serviceGroup.setConfig(serviceGroupConfig);
        assertTrue(serviceGroupMapper.insert(serviceGroup) > 0);
        assertNotNull(serviceGroup.getId());
        System.out.println(serviceGroup);
    }

    @Test
    void list() {
        List<ServiceGroup> serviceGroups =
                serviceGroupMapper.selectList(
                        Wrappers.<ServiceGroup>lambdaQuery()
                                .eq(ServiceGroup::getOpenConversationId, StringUtils.EMPTY)
                );
        assertNotNull(serviceGroups);
        System.out.println(serviceGroups);
    }
}
