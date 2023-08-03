package net.risesoft.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.ItemDataTransferService;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/22
 */
@RestController
@RequestMapping(value = "/vue/dataTransfer")
public class ItemDataTransferRestController {

    @Autowired
    private ItemDataTransferService itemDataTransferService;

    /**
     * 数据迁移
     * 
     * @param processDefinitionId 流程定义id
     * @param processInstanceId 流程实例id
     * @return
     */
    @RequestMapping(value = "/dataTransfer", method = RequestMethod.POST, produces = "application/json")
    public Y9Result<String> dataTransfer(@RequestParam(required = true) String processDefinitionId,
        @RequestParam(required = false) String processInstanceId) {
        return itemDataTransferService.dataTransfer(processDefinitionId, processInstanceId);
    }

    /**
     * 获取流程实例列表
     * 
     * @param itemId 事项id
     * @param processDefinitionId 流程定义id
     * @param page 页码
     * @param rows 条数
     * @return
     */
    @RequestMapping(value = "/getProcessInstanceList", method = RequestMethod.GET, produces = "application/json")
    public Y9Page<Map<String, Object>> getProcessInstanceList(@RequestParam(required = true) String itemId,
        @RequestParam(required = true) String processDefinitionId, @RequestParam(required = true) Integer page,
        @RequestParam(required = true) Integer rows) {
        return itemDataTransferService.getProcessInstanceList(itemId, processDefinitionId, page, rows);
    }
}
