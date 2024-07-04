package net.risesoft.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import net.risesoft.api.processadmin.ProcessDefinitionApi;
import net.risesoft.entity.ItemTaskConf;
import net.risesoft.model.processadmin.TargetModel;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.ItemTaskConfService;
import net.risesoft.util.SysVariables;
import net.risesoft.y9.Y9LoginUserHolder;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/20
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/vue/itemTaskConfig")
public class ItemTaskConfRestController {

    private final ItemTaskConfService taskConfService;

    private final ProcessDefinitionApi processDefinitionManager;

    /**
     * 复制签收配置
     *
     * @param itemId 事项id
     * @param processDefinitionId 流程定义id
     * @return
     */
    @RequestMapping(value = "/copyTaskConfig", method = RequestMethod.POST, produces = "application/json")
    public Y9Result<String> copyTaskConfig(@RequestParam String itemId, @RequestParam String processDefinitionId) {
        taskConfService.copyTaskConf(itemId, processDefinitionId);
        return Y9Result.successMsg("复制成功");
    }

    /**
     * 获取任务节点信息
     *
     * @param itemId 事项id
     * @param processDefinitionId 流程定义id
     * @return
     */
    @RequestMapping(value = "/getBpmList", method = RequestMethod.GET, produces = "application/json")
    public Y9Result<Map<String, Object>> getBpmList(@RequestParam String itemId,
        @RequestParam String processDefinitionId) {
        List<Map<String, Object>> resList = new ArrayList<>();
        Map<String, Object> resMap = new HashMap<>(16);
        String tenantId = Y9LoginUserHolder.getTenantId();
        List<TargetModel> list = processDefinitionManager.getNodes(tenantId, processDefinitionId, false).getData();
        Map<String, Object> map;
        for (TargetModel targetModel : list) {
            map = new HashMap<>(16);
            if (targetModel.getMultiInstance() != null
                && !targetModel.getMultiInstance().equals(SysVariables.SEQUENTIAL)) {
                map.put("id", "");
                map.put("signTask", false);
                map.put("taskType", "单人");
                if (targetModel.getMultiInstance().equals(SysVariables.PARALLEL)) {
                    map.put("taskType", "并行");
                }
                String taskDefKey = targetModel.getTaskDefKey();
                ItemTaskConf confTemp = taskConfService.findByItemIdAndProcessDefinitionIdAndTaskDefKey4Own(itemId,
                    processDefinitionId, taskDefKey);
                if (null != confTemp) {
                    map.put("id", confTemp.getId());
                    map.put("signTask", confTemp.getSignTask());
                }
                resList.add(map);
            }
        }
        resMap.put("rows", resList);
        // TODO
        return Y9Result.success(resMap, "获取成功");
    }

    /**
     * 保存任务基本配置
     *
     * @param itemTaskConf 任务基本配置
     * @return
     */
    @RequestMapping(value = "/saveBind", method = RequestMethod.POST, produces = "application/json")
    public Y9Result<String> save(ItemTaskConf itemTaskConf) {
        taskConfService.save(itemTaskConf);
        return Y9Result.successMsg("保存成功");
    }
}
