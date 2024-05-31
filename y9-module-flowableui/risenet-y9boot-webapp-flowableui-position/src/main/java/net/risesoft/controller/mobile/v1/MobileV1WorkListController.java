package net.risesoft.controller.mobile.v1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.api.itemadmin.position.Item4PositionApi;
import net.risesoft.api.itemadmin.position.OfficeDoneInfo4PositionApi;
import net.risesoft.api.itemadmin.position.ProcessTrack4PositionApi;
import net.risesoft.api.platform.permission.PositionResourceApi;
import net.risesoft.api.platform.resource.ResourceApi;
import net.risesoft.api.processadmin.ProcessTodoApi;
import net.risesoft.consts.UtilConsts;
import net.risesoft.enums.platform.AuthorityEnum;
import net.risesoft.model.itemadmin.ItemModel;
import net.risesoft.model.platform.Resource;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.DoingService;
import net.risesoft.service.DoneService;
import net.risesoft.service.TodoService;
import net.risesoft.util.SysVariables;
import net.risesoft.y9.Y9Context;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.json.Y9JsonUtil;

/**
 * 办件列表相关接口
 *
 * @author zhangchongjie
 * @date 2024/01/17
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/mobile/v1/workList")
public class MobileV1WorkListController {

    private final TodoService todoService;

    private final DoingService doingService;

    private final DoneService doneService;

    private final ProcessTrack4PositionApi processTrack4PositionApi;

    private final ProcessTodoApi processTodoApi;

    private final Item4PositionApi item4PositionApi;

    private final OfficeDoneInfo4PositionApi officeDoneInfo4PositionApi;

    private final ResourceApi resourceApi;

    private final PositionResourceApi positionResourceApi;

    /**
     * 获取在办件列表
     *
     * @param itemId     事项id
     * @param title      搜索标题
     * @param page       页码
     * @param rows       行数
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/doingList")
    public Y9Page<Map<String, Object>> doingList(@RequestParam String itemId, @RequestParam(required = false) String title, @RequestParam Integer page, @RequestParam Integer rows) {
        try {
            Map<String, Object> retMap = doingService.list(itemId, title, page, rows);
            List<Map<String, Object>> doingList = (List<Map<String, Object>>) retMap.get("rows");
            return Y9Page.success(page, Integer.parseInt(retMap.get("totalpages").toString()), Integer.parseInt(retMap.get("total").toString()), doingList, "获取列表成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Y9Page.success(page, 0, 0, new ArrayList<Map<String, Object>>(), "获取列表失败");
    }

    /**
     * 办结件列表
     *
     * @param itemId     事项id
     * @param title      搜索标题
     * @param page       页码
     * @param rows       行数
     */
    @RequestMapping(value = "/doneList")
    public Y9Page<Map<String, Object>> doneList(@RequestParam String itemId, @RequestParam(required = false) String title, @RequestParam Integer page, @RequestParam Integer rows) {
        try {
            return doneService.list(itemId, title, page, rows);
        } catch (Exception e) {
            LOGGER.error("办结件列表异常：");
            e.printStackTrace();
        }
        return Y9Page.success(page, 0, 0, new ArrayList<Map<String, Object>>(), "获取列表失败");
    }

    /**
     * app办件计数
     *
     * @param positionId 岗位id
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/getAppCount")
    public Y9Result<List<Map<String, Object>>> getAppCount(@RequestHeader("auth-positionId") String positionId) {
        Map<String, Object> map = new HashMap<String, Object>(16);
        map.put(UtilConsts.SUCCESS, true);
        try {
            String tenantId = Y9LoginUserHolder.getTenantId();
            Resource resource = resourceApi.getResource(tenantId).getData();
            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            if (null != resource && null != resource.getId()) {
                String resourceId = resource.getId();
                List<Resource> list0 = positionResourceApi.listSubResources(tenantId, positionId, AuthorityEnum.BROWSE, resourceId).getData();
                String url = "";
                for (Resource r : list0) {
                    map = new HashMap<>(16);
                    url = r.getUrl();
                    if (StringUtils.isBlank(url)) {
                        continue;
                    }
                    if (!url.contains("itemId=")) {
                        continue;
                    }
                    String itemId = url.split("itemId=")[1];
                    ItemModel item = item4PositionApi.getByItemId(tenantId, itemId);
                    String processDefinitionKey = item.getWorkflowGuid();
                    long todoCount = processTodoApi.getTodoCountByUserIdAndProcessDefinitionKey(tenantId, positionId, processDefinitionKey);
                    Map<String, Object> m = new HashMap<String, Object>(16);
                    Map<String, Object> resMap = todoService.list(item.getId(), "", 1, 1);
                    List<Map<String, Object>> todoList = (List<Map<String, Object>>) resMap.get("rows");
                    if (todoList != null && todoList.size() > 0) {
                        Map<String, Object> todo = todoList.get(0);
                        m.put("title", todo.get(SysVariables.DOCUMENTTITLE));
                        m.put("time", todo.get("taskCreateTime"));
                    }
                    m.put("todoCount", todoCount);
                    m.put("itemId", item.getId());
                    m.put("itemName", item.getName());
                    list.add(m);
                }
            }
            return Y9Result.success(list, "获取成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Y9Result.failure("获取失败");
    }

    /**
     * 获取日程应用
     *
     * @param tenantId 租户id
     * @param userId   人员id
     * @return
     */
    public Map<String, Object> getCalendar(String tenantId, String userId) {
        Map<String, Object> map = new HashMap<String, Object>(16);
        HttpClient client = new HttpClient();
        client.getParams().setParameter(HttpMethodParams.BUFFER_WARN_TRIGGER_LIMIT, 1024 * 1024 * 10);
        client.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
        HttpMethod method = new GetMethod();
        try {
            // 设置请求超时时间10s
            client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
            // 设置读取数据超时时间10s
            client.getHttpConnectionManager().getParams().setSoTimeout(5000);
            String url = Y9Context.getProperty("y9.common.calendarBaseUrl") + "/mobile/calendar/getTodo";
            method.setPath(url);
            method.addRequestHeader("auth-tenantId", tenantId);
            method.addRequestHeader("auth-userId", userId);
            int code = client.executeMethod(method);
            if (code == HttpStatus.SC_OK) {
                String msg = new String(method.getResponseBodyAsString().getBytes("UTF-8"), "UTF-8");
                map = Y9JsonUtil.readHashMap(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 获取消息快递
     *
     * @param tenantId
     * @param userId
     * @return
     */
    public Map<String, Object> getMessage(String tenantId, String userId) {
        // 获取日程应用
        Map<String, Object> map = new HashMap<String, Object>(16);
        HttpClient client = new HttpClient();
        client.getParams().setParameter(HttpMethodParams.BUFFER_WARN_TRIGGER_LIMIT, 1024 * 1024 * 10);
        client.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
        HttpMethod method = new GetMethod();
        try {
            // 设置请求超时时间10s
            client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
            // 设置读取数据超时时间10s
            client.getHttpConnectionManager().getParams().setSoTimeout(5000);
            String url = Y9Context.getProperty("y9.common.messageBaseUrl") + "/mobile/messageDelivery/getNotReadNum";
            method.setPath(url);
            method.addRequestHeader("auth-tenantId", tenantId);
            method.addRequestHeader("auth-userId", userId);
            int code = client.executeMethod(method);
            if (code == HttpStatus.SC_OK) {
                String msg = new String(method.getResponseBodyAsString().getBytes("UTF-8"), "UTF-8");
                map = Y9JsonUtil.readHashMap(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 办件计数
     *
     * @param itemId     事项id
     */
    @RequestMapping(value = "/getCount")
    public Y9Result<Map<String, Object>> getTodoCount(@RequestParam String itemId) {
        Map<String, Object> map = new HashMap<String, Object>(16);
        try {
            String tenantId = Y9LoginUserHolder.getTenantId();
            String positionId = Y9LoginUserHolder.getPositionId();
            ItemModel item = item4PositionApi.getByItemId(tenantId, itemId);
            String processDefinitionKey = item.getWorkflowGuid();
            Map<String, Object> countMap = processTodoApi.getCountByUserIdAndProcessDefinitionKey(tenantId, positionId, processDefinitionKey);
            int todoCount = countMap != null ? (int) countMap.get("todoCount") : 0;
            int doingCount = countMap != null ? (int) countMap.get("doingCount") : 0;
            // int doneCount = countMap != null ? (int) countMap.get("doneCount") : 0;
            int doneCount = officeDoneInfo4PositionApi.countByPositionId(tenantId, positionId, itemId);
            map.put("todoCount", todoCount);
            map.put("doingCount", doingCount);
            map.put("doneCount", doneCount);
            return Y9Result.success(map, "获取成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Y9Result.failure("获取失败");
    }

    /**
     * 历程
     *
     * @param processInstanceId 流程实例id
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/history")
    public Y9Result<List<Map<String, Object>>> history(@RequestParam String processInstanceId) {
        Map<String, Object> retMap = new HashMap<String, Object>(16);
        try {
            String tenantId = Y9LoginUserHolder.getTenantId();
            String positionId = Y9LoginUserHolder.getPositionId();
            retMap = processTrack4PositionApi.processTrackList(tenantId, positionId, processInstanceId);
            if ((boolean) retMap.get("success")) {
                return Y9Result.success((List<Map<String, Object>>) retMap.get("rows"), "获取成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Y9Result.failure("获取失败");
    }

    /**
     * 待办件列表
     *
     * @param itemId     事项id
     * @param title      搜索标题
     * @param page       页码
     * @param rows       行数
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/todoList")
    public Y9Page<Map<String, Object>> todoList(@RequestParam String itemId, @RequestParam(required = false) String title, @RequestParam Integer page, @RequestParam Integer rows) {
        try {
            Map<String, Object> retMap = todoService.list(itemId, title, page, rows);
            List<Map<String, Object>> todoList = (List<Map<String, Object>>) retMap.get("rows");
            return Y9Page.success(page, Integer.parseInt(retMap.get("totalpages").toString()), Integer.parseInt(retMap.get("total").toString()), todoList, "获取列表成功");
        } catch (Exception e) {
            LOGGER.error("手机端待办件列表异常");
            e.printStackTrace();
        }
        return Y9Page.success(page, 0, 0, new ArrayList<Map<String, Object>>(), "获取列表失败");
    }
}
