package io.github.lanicc.flat.service;

import com.alibaba.fastjson.JSON;
import io.github.lanicc.flat.FlatWebApplicationTests;
import io.github.lanicc.flat.model.ServiceGroup;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created on 2022/10/27.
 *
 * @author lan
 */
class ServiceGroupServiceTest extends FlatWebApplicationTests {

    @Autowired
    private ServiceGroupService serviceGroupService;

    @Test
    void createServiceGroup() throws IOException {
        ClassPathResource resource = new ClassPathResource("setup/config-example.json");
        ServiceGroup.Config serviceGroupConfig = JSON.parseObject(resource.getInputStream(), ServiceGroup.Config.class);
        assertNotNull(serviceGroupConfig);
        ServiceGroup serviceGroup = new ServiceGroup();
        serviceGroup.setName("测试服务群1");
        serviceGroup.setConfig(serviceGroupConfig);
        serviceGroupService.createServiceGroup(serviceGroup);
        assertNotNull(serviceGroup.getOpenConversationId());
        System.out.println(serviceGroup);
    }

    @Test
    void recreateDingServiceGroup() {
        ServiceGroup serviceGroup = serviceGroupService.recreateDingServiceGroup(1);
        assertNotNull(serviceGroup);
        System.out.println(serviceGroup);
    }
}
