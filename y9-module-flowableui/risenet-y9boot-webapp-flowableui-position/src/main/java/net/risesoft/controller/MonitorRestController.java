package net.risesoft.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;

import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.api.itemadmin.ProcessParamApi;
import net.risesoft.api.itemadmin.TransactionWordApi;
import net.risesoft.api.itemadmin.position.Attachment4PositionApi;
import net.risesoft.api.itemadmin.position.Item4PositionApi;
import net.risesoft.api.processadmin.HistoricProcessApi;
import net.risesoft.model.itemadmin.ChaoSongModel;
import net.risesoft.model.itemadmin.ItemModel;
import net.risesoft.model.itemadmin.ProcessParamModel;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.MonitorService;
import net.risesoft.util.SysVariables;
import net.risesoft.y9.Y9LoginUserHolder;

/**
 * 监控列表
 *
 * @author zhangchongjie
 * @date 2024/06/05
 */
@Validated
@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping(value = "/vue/monitor")
public class MonitorRestController {

    private final MonitorService monitorService;

    private final HistoricProcessApi historicProcessApi;

    private final TransactionWordApi transactionWordApi;

    private final Attachment4PositionApi attachment4PositionApi;

    private final ProcessParamApi processParamApi;

    private final Item4PositionApi item4PositionApi;

    /**
     * 单位所有件
     *
     * @param itemId 事项id
     * @param searchName 搜索词
     * @param userName 发起人
     * @param state 办件状态
     * @param year 年度
     * @param page 页码
     * @param rows 条数
     * @return Y9Page<Map < String, Object>>
     */
    @RequestMapping(value = "/deptList", method = RequestMethod.GET, produces = "application/json")
    public Y9Page<Map<String, Object>> deptList(@RequestParam @NotBlank String itemId,
        @RequestParam(required = false) String searchName, @RequestParam(required = false) String userName,
        @RequestParam(required = false) String state, @RequestParam(required = false) String year,
        @RequestParam Integer page, @RequestParam Integer rows) {
        return monitorService.pageDeptList(itemId, searchName, userName, state, year, page, rows);
    }

    /**
     * 获取所有事项
     *
     * @return Y9Result<List < ItemModel>>
     */
    @RequestMapping(value = "/getAllItemList", method = RequestMethod.GET, produces = "application/json")
    public Y9Result<List<ItemModel>> getAllItemList() {
        String tenantId = Y9LoginUserHolder.getTenantId();
        return item4PositionApi.getAllItemList(tenantId);
    }

    /**
     * 监控办件列表
     *
     * @param searchName 搜索词
     * @param itemId 事项id
     * @param userName 发起人
     * @param state 办件状态
     * @param year 年度
     * @param page 页码
     * @param rows 条数
     * @return Y9Page<Map < String, Object>>
     */
    @RequestMapping(value = "/monitorBanjianList", method = RequestMethod.GET, produces = "application/json")
    public Y9Page<Map<String, Object>> monitorBanjianList(@RequestParam(required = false) String searchName,
        @RequestParam(required = false) String itemId, @RequestParam(required = false) String userName,
        @RequestParam(required = false) String state, @RequestParam(required = false) String year,
        @RequestParam Integer page, @RequestParam Integer rows) {
        return monitorService.pageMonitorBanjianList(searchName, itemId, userName, state, year, page, rows);
    }

    /**
     * 监控阅件列表
     *
     * @param searchName 搜索词
     * @param itemId 事项id
     * @param senderName 发送人
     * @param userName 收件人
     * @param state 办件状态
     * @param year 年度
     * @param page 页码
     * @param rows 条数
     * @return Y9Page<ChaoSongModel>
     */
    @RequestMapping(value = "/monitorChaosongList", method = RequestMethod.GET, produces = "application/json")
    public Y9Page<ChaoSongModel> monitorChaosongList(@RequestParam(required = false) String searchName,
        @RequestParam(required = false) String itemId, @RequestParam(required = false) String senderName,
        @RequestParam(required = false) String userName, @RequestParam(required = false) String state,
        @RequestParam(required = false) String year, @RequestParam Integer page, @RequestParam Integer rows) {
        return monitorService.pageMonitorChaosongList(searchName, itemId, senderName, userName, state, year, page,
            rows);
    }

    /**
     * 获取监控在办列表
     *
     * @param itemId 事项id
     * @param searchTerm 搜索词
     * @param page 页码
     * @param rows 条数
     * @return Y9Page<Map < String, Object>>
     */
    @RequestMapping(value = "/monitorDoingList", method = RequestMethod.GET, produces = "application/json")
    public Y9Page<Map<String, Object>> monitorDoingList(@RequestParam @NotBlank String itemId,
        @RequestParam(required = false) String searchTerm, @RequestParam Integer page, @RequestParam Integer rows) {
        return monitorService.pageMonitorDoingList(itemId, searchTerm, page, rows);
    }

    /**
     * 获取监控办结列表
     *
     * @param itemId 事项id
     * @param searchTerm 搜索词
     * @param page 页码
     * @param rows 条数
     * @return Y9Page<Map < String, Object>>
     */
    @RequestMapping(value = "/monitorDoneList", method = RequestMethod.GET, produces = "application/json")
    public Y9Page<Map<String, Object>> monitorDoneList(@RequestParam @NotBlank String itemId,
        @RequestParam(required = false) String searchTerm, @RequestParam Integer page, @RequestParam Integer rows) {
        return monitorService.pageMonitorDoneList(itemId, searchTerm, page, rows);
    }

    /**
     * 彻底删除流程实例
     *
     * @param processInstanceIds 流程实例ids，逗号隔开
     * @return Y9Result<String>
     */
    @RequestMapping(value = "/removeProcess", method = RequestMethod.POST, produces = "application/json")
    public Y9Result<String> removeProcess(@RequestParam @NotBlank String processInstanceIds) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        ProcessParamModel processParamModel;
        List<String> list = null;
        try {
            if (StringUtils.isNotBlank(processInstanceIds)) {
                list = new ArrayList<>();
                String[] ids = processInstanceIds.split(SysVariables.COMMA);
                for (String processInstanceId : ids) {
                    processParamModel = processParamApi.findByProcessInstanceId(tenantId, processInstanceId).getData();
                    if (processParamModel != null) {
                        list.add(processParamModel.getProcessSerialNumber());
                    }
                }
            }
            boolean b = historicProcessApi.removeProcess4Position(tenantId, processInstanceIds).isSuccess();
            if (b) {
                // 批量删除附件表
                attachment4PositionApi.delBatchByProcessSerialNumbers(tenantId, list);
                // 批量删除正文表
                transactionWordApi.delBatchByProcessSerialNumbers(tenantId, list);
                return Y9Result.successMsg("删除成功");
            }
        } catch (Exception e) {
            LOGGER.error("删除失败", e);
        }
        return Y9Result.failure("删除失败");
    }
}
