 package net.risesoft.api;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.risesoft.api.processadmin.ConditionParserApi;
import net.risesoft.service.CustomConditionParser;
import net.risesoft.service.FlowableTenantInfoHolder;

/**
 * @author qinman
 * @date 2023/11/01
 */
@RestController
@RequestMapping(value = "/services/rest/conditionParser")
public class ConditionParserApiImpl implements ConditionParserApi {
    
    @Autowired
    private CustomConditionParser customConditionParser;

    @Override
    public Boolean parser(String tenantId,String conditionExpression, Map<String, Object> variables) {
        FlowableTenantInfoHolder.setTenantId(tenantId);
        return customConditionParser.parser(conditionExpression, variables);
    }
}
