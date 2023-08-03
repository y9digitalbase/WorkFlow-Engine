package net.risesoft.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import net.risesoft.model.processadmin.ProcessDefinitionModel;
import net.risesoft.pojo.Y9Result;
import net.risesoft.y9.Y9LoginUserHolder;

import y9.client.rest.processadmin.RepositoryApiClient;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/22
 */
@RestController
@RequestMapping(value = "/vue/itemProcessDefinition")
public class ItemProcessDefinitionRestController {

    @Autowired
    private RepositoryApiClient repositoryManager;

    @ResponseBody
    @RequestMapping(value = "/getProcessDefinitionList", method = RequestMethod.GET, produces = "application/json")
    public Y9Result<List<ProcessDefinitionModel>> getProcessDefinitionList(@RequestParam String processDefineKey) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        List<ProcessDefinitionModel> pdList =
            repositoryManager.getProcessDefinitionListByKey(tenantId, processDefineKey);
        return Y9Result.success(pdList, "获取成功");
    }
}