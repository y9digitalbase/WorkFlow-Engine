package net.risesoft.controller.mobile;

import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.risesoft.api.itemadmin.ItemApi;
import net.risesoft.api.org.PersonApi;
import net.risesoft.api.processadmin.HistoricProcessApi;
import net.risesoft.api.processadmin.MonitorApi;
import net.risesoft.consts.UtilConsts;
import net.risesoft.model.Person;
import net.risesoft.model.itemadmin.ItemModel;
import net.risesoft.service.MonitorService;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.json.Y9JsonUtil;
import net.risesoft.y9.util.Y9Util;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2023/01/03
 */
@RestController
@RequestMapping("/mobile/monitor")
public class MobileMonitorController {

    @Autowired
    private MonitorService monitorService;

    @Autowired
    private PersonApi personApi;

    @Autowired
    private HistoricProcessApi historicProcessManager;

    @Autowired
    private MonitorApi monitorManager;

    @Autowired
    private ItemApi itemManager;

    /**
     * 删除流程实例
     *
     * @param tenantId
     * @param userId
     * @param processInstanceId
     * @param response
     */
    @RequestMapping(value = "/deleteProcessInstance")
    public void deleteProcessInstance(@RequestHeader("auth-tenantId") String tenantId,
        @RequestHeader("auth-userId") String userId, @RequestParam String processInstanceId,
        HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>(16);
        try {
            boolean b = historicProcessManager.deleteProcessInstance(tenantId, processInstanceId);
            map.put(UtilConsts.SUCCESS, b);
        } catch (Exception e) {
            map.put("msg", "发生异常");
            map.put(UtilConsts.SUCCESS, false);
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 监控回收站统计
     *
     * @param tenantId
     * @param userId
     * @param itemId
     * @param response
     */
    @RequestMapping(value = "/getRecycleCount")
    public void getRecycleCount(@RequestHeader("auth-tenantId") String tenantId,
        @RequestHeader("auth-userId") String userId, @RequestParam String itemId, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>(16);
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            ItemModel item = itemManager.getByItemId(tenantId, itemId);
            String processDefinitionKey = item.getWorkflowGuid();
            long recycleCount = monitorManager.getRecycleCountByProcessDefinitionKey(tenantId, processDefinitionKey);
            map.put("recycleCount", recycleCount);
            map.put(UtilConsts.SUCCESS, true);
            map.put("msg", "获取数据成功");
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "获取数据失败");
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 监控在办件统计
     *
     * @param tenantId
     * @param userId
     * @param itemId
     * @param response
     */
    @RequestMapping(value = "/monitorDoingCount")
    public void monitorDoingCount(@RequestHeader("auth-tenantId") String tenantId,
        @RequestHeader("auth-userId") String userId, @RequestParam String itemId, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>(16);
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            ItemModel item = itemManager.getByItemId(tenantId, itemId);
            String processDefinitionKey = item.getWorkflowGuid();
            long monitorDoingCount = monitorManager.getDoingCountByProcessDefinitionKey(tenantId, processDefinitionKey);
            map.put("monitorDoingCount", monitorDoingCount);
            map.put(UtilConsts.SUCCESS, true);
            map.put("msg", "获取数据成功");
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "获取数据失败");
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 监控在办件
     *
     * @param tenantId
     * @param userId
     * @param itemId
     * @param title
     * @param page
     * @param rows
     * @param response
     */
    @RequestMapping(value = "/monitorDoingList")
    public void monitorDoingList(@RequestHeader("auth-tenantId") String tenantId,
        @RequestHeader("auth-userId") String userId, @RequestParam String itemId,
        @RequestParam(required = false) String title, int page, int rows, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>(16);
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            Person person = personApi.getPerson(tenantId, userId);
            Y9LoginUserHolder.setPerson(person);
        } catch (Exception e) {
            map.put("msg", "发生异常");
            map.put(UtilConsts.SUCCESS, false);
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 监控办结件统计
     *
     * @param tenantId
     * @param userId
     * @param itemId
     * @param response
     */
    @RequestMapping(value = "/monitorDoneCount")
    public void monitorDoneCount(@RequestHeader("auth-tenantId") String tenantId,
        @RequestHeader("auth-userId") String userId, @RequestParam String itemId, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>(16);
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            ItemModel item = itemManager.getByItemId(tenantId, itemId);
            String processDefinitionKey = item.getWorkflowGuid();
            long monitorDoneCount = monitorManager.getDoneCountByProcessDefinitionKey(tenantId, processDefinitionKey);
            map.put("monitorDoneCount", monitorDoneCount);
            map.put(UtilConsts.SUCCESS, true);
            map.put("msg", "获取数据成功");
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "获取数据失败");
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 监控办结件
     *
     * @param tenantId
     * @param userId
     * @param itemId
     * @param title
     * @param page
     * @param rows
     * @param response
     */
    @RequestMapping(value = "/monitorDoneList")
    public void monitorDoneList(@RequestHeader("auth-tenantId") String tenantId,
        @RequestHeader("auth-userId") String userId, @RequestParam String itemId,
        @RequestParam(required = false) String title, int page, int rows, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>(16);
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            Person person = personApi.getPerson(tenantId, userId);
            Y9LoginUserHolder.setPerson(person);
        } catch (Exception e) {
            map.put("msg", "发生异常");
            map.put(UtilConsts.SUCCESS, false);
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 监控回收站列表
     *
     * @param tenantId
     * @param userId
     * @param itemId
     * @param title
     * @param page
     * @param rows
     * @param response
     */
    @RequestMapping(value = "/monitorRecycleList")
    public void monitorRecycleList(@RequestHeader("auth-tenantId") String tenantId,
        @RequestHeader("auth-userId") String userId, @RequestParam String itemId,
        @RequestParam(required = false) String title, int page, int rows, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>(16);
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            Person person = personApi.getPerson(tenantId, userId);
            Y9LoginUserHolder.setPerson(person);
            map = monitorService.monitorRecycleList(itemId, title, page, rows);
        } catch (Exception e) {
            map.put("msg", "发生异常");
            map.put(UtilConsts.SUCCESS, false);
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 恢复流程实例
     *
     * @param tenantId
     * @param userId
     * @param processInstanceId
     * @param response
     */
    @RequestMapping(value = "/recoveryProcess")
    public void recoveryProcess(@RequestHeader("auth-tenantId") String tenantId,
        @RequestHeader("auth-userId") String userId, @RequestParam String processInstanceId,
        HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>(16);
        try {
            boolean b = historicProcessManager.recoveryProcess(tenantId, userId, processInstanceId);
            map.put(UtilConsts.SUCCESS, b);
        } catch (Exception e) {
            map.put("msg", "发生异常");
            map.put(UtilConsts.SUCCESS, false);
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 彻底删除流程实例
     *
     * @param tenantId
     * @param userId
     * @param processInstanceId
     * @param response
     */
    @RequestMapping(value = "/removeProcess")
    public void removeProcess(@RequestHeader("auth-tenantId") String tenantId,
        @RequestHeader("auth-userId") String userId, @RequestParam String processInstanceId,
        HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>(16);
        try {
            boolean b = historicProcessManager.removeProcess(tenantId, processInstanceId);
            map.put(UtilConsts.SUCCESS, b);
        } catch (Exception e) {
            map.put("msg", "发生异常");
            map.put(UtilConsts.SUCCESS, false);
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

}
