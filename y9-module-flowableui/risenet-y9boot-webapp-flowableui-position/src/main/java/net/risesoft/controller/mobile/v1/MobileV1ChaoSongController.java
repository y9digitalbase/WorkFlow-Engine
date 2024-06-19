package net.risesoft.controller.mobile.v1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotBlank;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.api.itemadmin.position.ChaoSong4PositionApi;
import net.risesoft.api.itemadmin.position.ItemRole4PositionApi;
import net.risesoft.consts.UtilConsts;
import net.risesoft.exception.GlobalErrorCodeEnum;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;
import net.risesoft.util.DocumentUtil;
import net.risesoft.util.SysVariables;
import net.risesoft.y9.Y9LoginUserHolder;

/**
 * 抄送阅件相关接口
 *
 * @author zhangchongjie
 * @date 2024/01/17
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/mobile/v1/chaosong")
public class MobileV1ChaoSongController {

    private final ChaoSong4PositionApi chaoSong4PositionApi;

    private final ItemRole4PositionApi itemRole4PositionApi;

    /**
     * 抄送件收回
     *
     * @param ids 抄送件ids
     * @return Y9Result<String>
     */
    @RequestMapping(value = "/deleteList")
    public Y9Result<String> deleteList(@RequestParam @NotBlank String ids) {
        try {
            String tenantId = Y9LoginUserHolder.getTenantId();
            String[] id = ids.split(",");
            chaoSong4PositionApi.deleteByIds(tenantId, id);
            return Y9Result.successMsg("收回成功");
        } catch (Exception e) {
            LOGGER.error("手机端跟踪抄送件收回", e);
        }
        return Y9Result.failure("收回失败");
    }

    /**
     * 抄送件详情
     *
     * @param id 抄送id
     * @param processInstanceId 流程实例id
     * @param status 状态0为未阅件打开，1为已阅件打开
     * @return Y9Result<Map < String, Object>>
     */
    @RequestMapping(value = "/detail")
    public Y9Result<Map<String, Object>> detail(@RequestParam @NotBlank String id, @RequestParam @NotBlank String processInstanceId, @RequestParam(required = false) Integer status) {
        Map<String, Object> map;
        try {
            String tenantId = Y9LoginUserHolder.getTenantId();
            String positionId = Y9LoginUserHolder.getPositionId();
            map = chaoSong4PositionApi.detail(tenantId, positionId, id, processInstanceId, status, true);
            String processSerialNumber = (String)map.get("processSerialNumber");
            String activitiUser = (String)map.get(SysVariables.ACTIVITIUSER);
            String processDefinitionId = (String)map.get("processDefinitionId");
            String taskDefKey = (String)map.get("taskDefKey");
            String formIds = (String)map.get("formId");
            String formNames = (String)map.get("formName");
            String taskId = (String)map.get("taskId");
            String itemId = (String)map.get("itemId");
            String itembox = (String)map.get("itembox");
            DocumentUtil documentUtil = new DocumentUtil();
            Map<String, Object> dataMap = documentUtil.documentDetail(itemId, processDefinitionId, processSerialNumber, processInstanceId, taskDefKey, taskId, itembox, activitiUser, formIds, formNames);
            map.putAll(dataMap);
            return Y9Result.success(map, "获取成功");
        } catch (Exception e) {
            LOGGER.error("手机端跟踪查看抄送件详情", e);
        }
        return Y9Result.failure("获取失败");
    }

    /**
     * 获取抄送选人
     *
     * @param id 父节点id
     * @param principalType 选择类型 2是部门，3是群组
     * @param processInstanceId 流程实例id
     * @return Y9Result<List < Map < String, Object>>>
     */
    @RequestMapping(value = "/findCsUser")
    public Y9Result<List<Map<String, Object>>> findCsUser(@RequestParam(required = false) String id, @RequestParam(required = false) Integer principalType, @RequestParam(required = false) String processInstanceId) {
        List<Map<String, Object>> item;
        try {
            String positionId = Y9LoginUserHolder.getPositionId();
            String userId = Y9LoginUserHolder.getPersonId();
            item = itemRole4PositionApi.findCsUser(Y9LoginUserHolder.getTenantId(), userId, positionId, id, principalType, processInstanceId);
            return Y9Result.success(item, "获取成功");
        } catch (Exception e) {
            LOGGER.error("手机端跟踪获取抄送选人", e);
        }
        return Y9Result.failure("获取失败");
    }

    /**
     * 选人搜索
     *
     * @param principalType 选择类型 2是部门，3是群组
     * @param processInstanceId 流程实例id
     * @param name 搜索姓名
     * @return Y9Result<List < Map < String, Object>>>
     */
    @RequestMapping(value = "/findCsUserSearch")
    public Y9Result<List<Map<String, Object>>> findCsUserSearch(@RequestParam(required = false) String name, @RequestParam(required = false) Integer principalType, @RequestParam(required = false) String processInstanceId) {
        List<Map<String, Object>> item;
        try {
            String tenantId = Y9LoginUserHolder.getTenantId();
            String positionId = Y9LoginUserHolder.getPositionId();
            String userId = Y9LoginUserHolder.getPersonId();
            item = itemRole4PositionApi.findCsUserSearch(tenantId, userId, positionId, name, principalType, processInstanceId);
            return Y9Result.success(item, "获取成功");
        } catch (Exception e) {
            LOGGER.error("手机端跟踪选人搜索", e);
        }
        return Y9Result.failure("获取失败");
    }

    /**
     * 办件抄送列表
     *
     * @param type，“my”为我的抄送，其余为所有抄送
     * @param processInstanceId 流程实例id
     * @param rows 行数
     * @param page 页码
     * @return Y9Page<Map < String, Object>>
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/list")
    public Y9Page<Map<String, Object>> list(@RequestParam(required = false) String type, @RequestParam @NotBlank String processInstanceId, @RequestParam int rows, @RequestParam int page) {
        Map<String, Object> map;
        try {
            String tenantId = Y9LoginUserHolder.getTenantId();
            String positionId = Y9LoginUserHolder.getPositionId();
            if (type.equals("my")) {
                map = chaoSong4PositionApi.getListBySenderIdAndProcessInstanceId(tenantId, positionId, processInstanceId, "", rows, page);
            } else {
                map = chaoSong4PositionApi.getListByProcessInstanceId(tenantId, positionId, processInstanceId, "", rows, page);
            }
            if ((boolean)map.get("success")) {
                List<Map<String, Object>> list = (List<Map<String, Object>>)map.get("rows");
                return Y9Page.success(page, Integer.parseInt(map.get("totalpages").toString()), Long.parseLong(map.get("total").toString()), list, "获取成功");
            }
        } catch (Exception e) {
            LOGGER.error("手机端跟踪办件抄送列表", e);
        }
        return Y9Page.failure(page, 0, 0, new ArrayList<>(), "获取失败", GlobalErrorCodeEnum.FAILURE.getCode());
    }

    /**
     * 抄送件列表
     *
     * @param documentTitle 搜索标题
     * @param status 状态，0为未阅件，1为已阅件
     * @param rows 行数
     * @param page 页码
     * @return Y9Page<Map < String, Object>>
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/search")
    public Y9Page<Map<String, Object>> search(@RequestParam(required = false) String documentTitle, @RequestParam(required = false) Integer status, @RequestParam int rows, @RequestParam int page) {
        Map<String, Object> map = new HashMap<>(16);
        try {
            String tenantId = Y9LoginUserHolder.getTenantId();
            String positionId = Y9LoginUserHolder.getPositionId();
            if (status == 0) {
                map = chaoSong4PositionApi.getTodoList(tenantId, positionId, documentTitle, rows, page);
            } else if (status == 1) {
                map = chaoSong4PositionApi.getDoneList(tenantId, positionId, documentTitle, rows, page);
            }
            List<Map<String, Object>> list = (List<Map<String, Object>>)map.get("rows");
            return Y9Page.success(page, Integer.parseInt(map.get("totalpages").toString()), Long.parseLong(map.get("total").toString()), list, "获取成功");
        } catch (Exception e) {
            LOGGER.error("手机端跟踪查看抄送件列表", e);
        }
        return Y9Page.failure(page, 0, 0, new ArrayList<>(), "获取失败", GlobalErrorCodeEnum.FAILURE.getCode());
    }

    /**
     * 抄送发送
     *
     * @param processInstanceId 流程实例id
     * @param users 发送人员
     * @param isSendSms 是否短信提醒
     * @param isShuMing 是否署名
     * @param smsContent 短信内容
     * @return Y9Result<String>
     */
    @RequestMapping(value = "/send")
    public Y9Result<String> send(@RequestParam @NotBlank String processInstanceId, @RequestParam @NotBlank String users, @RequestParam(required = false) String isSendSms, @RequestParam(required = false) String isShuMing, @RequestParam(required = false) String smsContent) {
        Map<String, Object> map = new HashMap<>(1);
        try {
            String tenantId = Y9LoginUserHolder.getTenantId();
            String positionId = Y9LoginUserHolder.getPositionId();
            String userId = Y9LoginUserHolder.getPersonId();
            map = chaoSong4PositionApi.save(tenantId, userId, positionId, processInstanceId, users, isSendSms, isShuMing, smsContent, "");
            if ((boolean)map.get("success")) {
                return Y9Result.success("抄送成功");
            }
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            LOGGER.error("手机端跟踪查看抄送件发送", e);
        }
        return Y9Result.failure("抄送失败");
    }
}
