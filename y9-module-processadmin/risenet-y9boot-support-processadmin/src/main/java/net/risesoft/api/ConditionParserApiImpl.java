package net.risesoft.api;

import lombok.RequiredArgsConstructor;
import net.risesoft.api.processadmin.ConditionParserApi;
import net.risesoft.service.CustomConditionParser;
import net.risesoft.service.FlowableTenantInfoHolder;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 解析表达式条件接口
 *
 * @author qinman
 * @date 2023/11/01
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/services/rest/conditionParser")
public class ConditionParserApiImpl implements ConditionParserApi {

    private final CustomConditionParser customConditionParser;

    /**
     * 解析表达式条件是否满足
     *
     * @param tenantId 租户id
     * @param conditionExpression 网关上的表达式
     * @param variables 流程变量
     * @return Boolean
     */
    @Override
    @PostMapping(value = "/parser", produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean parser(@RequestParam String tenantId, @RequestParam String conditionExpression, @RequestBody Map<String, Object> variables) {
        FlowableTenantInfoHolder.setTenantId(tenantId);
        return customConditionParser.parser(conditionExpression, variables);
    }
}
