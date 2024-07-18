package net.risesoft.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.HistoryService;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import net.risesoft.pojo.Y9Page;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2023/01/03
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/vue/historicVariable", produces = MediaType.APPLICATION_JSON_VALUE)
public class HistoricVariableVueController {

    private final HistoryService historyService;

    /**
     * 获取所有历史流程变量集合
     *
     */
    @RequestMapping(value = "/getAllHistoricVariable")
    public Y9Page<Map<String, Object>> pageAllHistoricVariable(@RequestParam int page, @RequestParam int rows) {
        List<Map<String, Object>> items = new ArrayList<>();
        long totalCount = historyService.createHistoricVariableInstanceQuery().count();
        List<HistoricVariableInstance> hviList =
            historyService.createHistoricVariableInstanceQuery().listPage((page - 1) * rows, rows);
        for (HistoricVariableInstance var : hviList) {
            Map<String, Object> map = new HashMap<>(16);
            map.put("processInstanceId", var.getProcessInstanceId());
            map.put("taskId", var.getTaskId());
            map.put("name", var.getVariableName());
            if (var.getValue() == null) {
                map.put("value", "");
            } else {
                map.put("value", var.getValue());
            }
            map.put("type", var.getVariableTypeName());
            items.add(map);
        }
        int totalPages = (int)totalCount / rows + 1;
        return Y9Page.success(page, totalPages, totalCount, items, "获取列表成功");
    }

    /**
     * 搜索历史流程变量
     *
     * @param processInstanceId 流程实例id
     * @param taskId 任务id
     * @param variableName 变量名
     * @param page 页码
     * @param rows 条数
     * @return Y9Page<Map<String, Object>>
     */
    @GetMapping(value = "/searchHistoricVariable")
    public Y9Page<Map<String, Object>> searchHistoricVariable(@RequestParam(required = false) String processInstanceId,
        @RequestParam(required = false) String taskId, @RequestParam(required = false) String variableName,
        @RequestParam int page, @RequestParam int rows) {
        List<Map<String, Object>> items = new ArrayList<>();
        long totalCount;
        List<HistoricVariableInstance> hviList;
        if (StringUtils.isBlank(processInstanceId)) {
            if (StringUtils.isBlank(taskId)) {
                totalCount = historyService.createHistoricVariableInstanceQuery()
                    .variableNameLike("%" + variableName + "%").count();
                hviList = historyService.createHistoricVariableInstanceQuery()
                    .variableNameLike("%" + variableName + "%").listPage((page - 1) * rows, rows);
            } else {
                totalCount = historyService.createHistoricVariableInstanceQuery().taskId(taskId)
                    .variableNameLike("%" + variableName + "%").count();
                hviList = historyService.createHistoricVariableInstanceQuery().taskId(taskId)
                    .variableNameLike("%" + variableName + "%").listPage((page - 1) * rows, rows);
            }
        } else {
            if (StringUtils.isBlank(taskId)) {
                totalCount = historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstanceId)
                    .variableNameLike("%" + variableName + "%").count();
                hviList = historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstanceId)
                    .variableNameLike("%" + variableName + "%").listPage((page - 1) * rows, rows);
            } else {
                totalCount = historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstanceId)
                    .taskId(taskId).variableNameLike("%" + variableName + "%").count();
                hviList = historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstanceId)
                    .taskId(taskId).variableNameLike("%" + variableName + "%").listPage((page - 1) * rows, rows);
            }
        }
        for (HistoricVariableInstance var : hviList) {
            Map<String, Object> map = new HashMap<>(16);
            map.put("processInstanceId", var.getProcessInstanceId());
            map.put("taskId", var.getTaskId());
            map.put("name", var.getVariableName());
            if (var.getValue() == null) {
                map.put("value", "");
            } else {
                map.put("value", var.getValue());
            }
            map.put("type", var.getVariableTypeName());
            items.add(map);
        }
        int totalPages = (int)totalCount / rows + 1;
        return Y9Page.success(page, totalPages, totalCount, items, "获取列表成功");
    }
}
