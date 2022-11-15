package io.github.lanicc.flat.controller;

import io.github.lanicc.flat.mapper.ServiceGroupMapper;
import io.github.lanicc.flat.model.ServiceGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created on 2022/10/27.
 *
 * @author lan
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ServiceGroupMapper serviceGroupMapper;

    @PostMapping("/createServiceGroup")
    public Object createServiceGroup(@RequestBody ServiceGroup serviceGroup) {
        return null;
    }

    @PostMapping("/recreateDingServiceGroup")
    public Object recreateDingServiceGroup(@RequestParam int id) {
        return null;
    }
}
