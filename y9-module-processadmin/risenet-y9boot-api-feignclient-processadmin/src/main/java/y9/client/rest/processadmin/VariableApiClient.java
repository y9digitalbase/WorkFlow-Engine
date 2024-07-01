package y9.client.rest.processadmin;

import java.util.Collection;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import net.risesoft.api.processadmin.VariableApi;
import net.risesoft.pojo.Y9Result;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/19
 */
@FeignClient(contextId = "VariableApiClient", name = "${y9.service.processAdmin.name:processAdmin}",
    url = "${y9.service.processAdmin.directUrl:}",
    path = "/${y9.service.processAdmin.name:processAdmin}/services/rest/variable")
public interface VariableApiClient extends VariableApi {

    /**
     * 删除流程变量
     *
     * @param tenantId 租户id
     * @param taskId 任务id
     * @param key key
     */
    @Override
    @PostMapping("/deleteVariable")
    Y9Result<Object> deleteVariable(@RequestParam("tenantId") String tenantId, @RequestParam("taskId") String taskId,
        @RequestParam("key") String key);

    /***
     * 删除任务变量
     *
     * @param tenantId
     * @param taskId
     * @param key
     */
    @Override
    @PostMapping("/deleteVariableLocal")
    Y9Result<Object> deleteVariableLocal(@RequestParam("tenantId") String tenantId,
        @RequestParam("taskId") String taskId, @RequestParam("key") String key);

    /**
     * 获取流程变量
     *
     * @param tenantId 租户id
     * @param taskId 任务id
     * @param key key
     * @return Object
     */
    @Override
    @GetMapping("/getVariable")
    Y9Result<String> getVariable(@RequestParam("tenantId") String tenantId, @RequestParam("taskId") String taskId,
        @RequestParam("key") String key);

    /**
     * 获取流程变量
     *
     * @param tenantId 租户id
     * @param processInstanceId 流程id
     * @param key key
     * @return Object
     */
    @Override
    @GetMapping("/getVariableByProcessInstanceId")
    Y9Result<String> getVariableByProcessInstanceId(@RequestParam("tenantId") String tenantId,
        @RequestParam("processInstanceId") String processInstanceId, @RequestParam("key") String key);

    /**
     * 获取任务变量
     *
     * @param tenantId 租户id
     * @param taskId 任务id
     * @param key key
     * @return Object
     */
    @Override
    @GetMapping("/getVariableLocal")
    Y9Result<String> getVariableLocal(@RequestParam("tenantId") String tenantId, @RequestParam("taskId") String taskId,
        @RequestParam("key") String key);

    /**
     * 获取多个流程变量
     *
     * @param tenantId 租户id
     * @param taskId 任务id
     * @return Map
     */
    @Override
    @GetMapping("/getVariables")
    Y9Result<Map<String, Object>> getVariables(@RequestParam("tenantId") String tenantId,
        @RequestParam("taskId") String taskId);

    /**
     *
     * Description: 获取指定的流程变量
     *
     * @param tenantId
     * @param processInstanceId
     * @param keys
     * @return
     */
    @Override
    @RequestMapping(value = "/getVariablesByProcessInstanceId", consumes = MediaType.APPLICATION_JSON_VALUE)
    Y9Result<Map<String, Object>> getVariablesByProcessInstanceId(@RequestParam("tenantId") String tenantId,
        @RequestParam("processInstanceId") String processInstanceId, @RequestBody Collection<String> keys);

    /**
     * 获取所有任务变量
     *
     * @param tenantId 租户id
     * @param taskId 任务id
     * @return Map
     */
    @Override
    @GetMapping("/getVariablesLocal")
    Y9Result<Map<String, Object>> getVariablesLocal(@RequestParam("tenantId") String tenantId,
        @RequestParam("taskId") String taskId);

    /**
     * 设置流程变量
     *
     * @param tenantId 租户id
     * @param taskId 任务id
     * @param key key
     * @param val val
     */
    @Override
    @PostMapping(value = "/setVariable", consumes = MediaType.APPLICATION_JSON_VALUE)
    Y9Result<Object> setVariable(@RequestParam("tenantId") String tenantId, @RequestParam("taskId") String taskId,
        @RequestParam("key") String key, @RequestBody Map<String, Object> map);

    /**
     *
     * Description: 设置流程变量
     *
     * @param tenantId
     * @param processInstanceId
     * @param key
     * @param val
     */
    @Override
    @PostMapping(value = "/setVariableByProcessInstanceId", consumes = MediaType.APPLICATION_JSON_VALUE)
    Y9Result<Object> setVariableByProcessInstanceId(@RequestParam("tenantId") String tenantId,
        @RequestParam("processInstanceId") String processInstanceId, @RequestParam("key") String key,
        @RequestBody Map<String, Object> map);

    /**
     * 设置任务变量
     *
     * @param tenantId 租户id
     * @param taskId 任务id
     * @param key key
     * @param val val
     */
    @Override
    @PostMapping(value = "/setVariableLocal", consumes = MediaType.APPLICATION_JSON_VALUE)
    Y9Result<Object> setVariableLocal(@RequestParam("tenantId") String tenantId, @RequestParam("taskId") String taskId,
        @RequestParam("key") String key, @RequestBody Map<String, Object> map);

    /**
     * 这只多个流程变量
     *
     * @param tenantId 租户id
     * @param taskId 任务id
     * @param map map
     */
    @Override
    @PostMapping(value = "/setVariables", consumes = MediaType.APPLICATION_JSON_VALUE)
    Y9Result<Object> setVariables(@RequestParam("tenantId") String tenantId, @RequestParam("taskId") String taskId,
        @RequestBody Map<String, Object> map);

    /**
     * 设置多个任务变量
     *
     * @param tenantId 租户id
     * @param taskId 任务id
     * @param map map
     */
    @Override
    @PostMapping(value = "/setVariablesLocal", consumes = MediaType.APPLICATION_JSON_VALUE)
    Y9Result<Object> setVariablesLocal(@RequestParam("tenantId") String tenantId, @RequestParam("taskId") String taskId,
        @RequestBody Map<String, Object> map);
}
