package net.risesoft.controller.mobile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.risesoft.api.itemadmin.ProcessParamApi;
import net.risesoft.api.itemadmin.position.ButtonOperation4PositionApi;
import net.risesoft.api.itemadmin.position.Document4PositionApi;
import net.risesoft.api.org.PersonApi;
import net.risesoft.api.org.PositionApi;
import net.risesoft.api.processadmin.HistoricProcessApi;
import net.risesoft.api.processadmin.ProcessDefinitionApi;
import net.risesoft.api.processadmin.SpecialOperationApi;
import net.risesoft.api.processadmin.TaskApi;
import net.risesoft.api.processadmin.VariableApi;
import net.risesoft.consts.UtilConsts;
import net.risesoft.enums.ItemBoxTypeEnum;
import net.risesoft.model.itemadmin.ProcessParamModel;
import net.risesoft.model.platform.Person;
import net.risesoft.model.platform.Position;
import net.risesoft.model.processadmin.HistoricProcessInstanceModel;
import net.risesoft.model.processadmin.TaskModel;
import net.risesoft.service.ButtonOperationService;
import net.risesoft.service.MultiInstanceService;
import net.risesoft.service.Process4SearchService;
import net.risesoft.util.SysVariables;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.json.Y9JsonUtil;
import net.risesoft.y9.util.Y9Util;

/**
 * 菜单方法接口
 *
 * @author 10858
 *
 */
@RestController
@RequestMapping(value = "/mobile/buttonOperation")
public class MobileButtonOperationController {

    protected Logger log = LoggerFactory.getLogger(MobileButtonOperationController.class);

    @Autowired
    private PersonApi personManager;

    @Autowired
    private PositionApi positionApi;

    @Autowired
    private TaskApi taskManager;

    @Autowired
    private ButtonOperation4PositionApi buttonOperationManager;

    @Autowired
    private HistoricProcessApi historicProcessManager;

    @Autowired
    private Document4PositionApi documentManager;

    @Autowired
    private SpecialOperationApi specialOperationManager;

    @Autowired
    private VariableApi variableManager;

    @Autowired
    private ButtonOperationService buttonOperationService;

    @Autowired
    private Process4SearchService process4SearchService;

    @Autowired
    private ProcessParamApi processParamManager;

    @Autowired
    private ProcessDefinitionApi processDefinitionManager;

    @Autowired
    private MultiInstanceService multiInstanceService;

    /**
     * 签收：抢占式办理时，签收后，其他人不可再签收办理
     *
     * @param tenantId 租户id
     * @param userId 人员id
     * @param positionId 岗位id
     * @param taskId 任务id
     * @param response
     */
    @RequestMapping(value = "/claim")
    public void claim(@RequestHeader("auth-tenantId") String tenantId, @RequestHeader("auth-userId") String userId, @RequestHeader("auth-positionId") String positionId, @RequestParam String taskId, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>(16);
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            TaskModel task = taskManager.findById(tenantId, taskId);
            if (task != null) {
                String assigneeId = task.getAssignee();
                if (StringUtils.isBlank(assigneeId)) {
                    taskManager.claim(tenantId, positionId, taskId);
                    map.put(UtilConsts.SUCCESS, true);
                    map.put("msg", "签收成功");
                } else {
                    String assigneeName = positionApi.getPosition(tenantId, assigneeId).getData().getName();
                    map.put(UtilConsts.SUCCESS, false);
                    map.put("msg", "任务已被用户:" + assigneeName + "签收！");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "签收失败");
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 流程办结
     *
     * @param tenantId 租户id
     * @param userId 人员id
     * @param positionId 岗位id
     * @param taskId 任务id
     * @param request
     * @param response
     */
    @RequestMapping(value = "/complete")
    public void complete(@RequestHeader("auth-tenantId") String tenantId, @RequestHeader("auth-userId") String userId, @RequestHeader("auth-positionId") String positionId, @RequestParam String taskId, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>(16);
        Y9LoginUserHolder.setTenantId(tenantId);
        try {
            Person person = personManager.getPerson(tenantId, userId).getData();
            Y9LoginUserHolder.setPerson(person);

            Position position = positionApi.getPosition(tenantId, positionId).getData();
            Y9LoginUserHolder.setPosition(position);
            if (StringUtils.isNotBlank(taskId)) {
                buttonOperationService.complete(taskId, "办结", "已办结", "");
                map.put(UtilConsts.SUCCESS, true);
                map.put("msg", "办结成功");
            } else {
                map.put(UtilConsts.SUCCESS, false);
                map.put("msg", "办结失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "办结失败");
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 获取办件状态
     *
     * @param tenantId 租户id
     * @param userId 人员id
     * @param positionId 岗位id
     * @param taskId 任务id
     * @param processInstanceId 流程实例id
     * @param request
     * @param response
     */
    @RequestMapping(value = "/getItemBox")
    public void getItemBox(@RequestHeader("auth-tenantId") String tenantId, @RequestHeader("auth-userId") String userId, @RequestHeader("auth-positionId") String positionId, @RequestParam String taskId, @RequestParam String processInstanceId, HttpServletRequest request,
        HttpServletResponse response) {
        Y9LoginUserHolder.setTenantId(tenantId);
        Map<String, Object> map = new HashMap<String, Object>(16);
        String itembox = ItemBoxTypeEnum.TODO.getValue();
        map.put("itembox", itembox);
        try {
            TaskModel taskModel = taskManager.findById(tenantId, taskId);
            HistoricProcessInstanceModel hpi = historicProcessManager.getById(tenantId, processInstanceId);
            if (taskModel != null && taskModel.getId() != null) {
                itembox = ItemBoxTypeEnum.TODO.getValue();
            } else {
                if (hpi != null && hpi.getEndTime() == null) {
                    itembox = ItemBoxTypeEnum.DOING.getValue();
                    List<TaskModel> taskList = taskManager.findByProcessInstanceId(tenantId, processInstanceId);
                    taskId = taskList.get(0).getId();
                    map.put("taskId", taskId);
                } else {
                    itembox = ItemBoxTypeEnum.DONE.getValue();
                }
            }
            map.put("itembox", itembox);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 办理完成，并行处理时使用
     *
     * @param tenantId 租户id
     * @param userId 人员id
     * @param positionId 岗位id
     * @param taskId 任务id
     * @param response
     */
    @SuppressWarnings("unchecked")
    @RequestMapping("/handleParallel")
    public void handleParallel(@RequestHeader("auth-tenantId") String tenantId, @RequestHeader("auth-userId") String userId, @RequestHeader("auth-positionId") String positionId, @RequestParam String taskId, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>(16);
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            map.put(UtilConsts.SUCCESS, true);
            map.put("msg", "办理成功");
            TaskModel task = taskManager.findById(tenantId, taskId);
            if (task == null) {
                map.put(UtilConsts.SUCCESS, false);
                map.put("msg", "该件已被办理！");
            } else {
                List<TaskModel> list = taskManager.findByProcessInstanceId(tenantId, task.getProcessInstanceId());
                if (list.size() == 1) {// 并行状态且不区分主协办时，多人同时打开办理页面，当其他人都已办理完成，最后一人需提示已是并行办理的最后一人，需刷新重新办理。
                    map.put("msg", "您是并行办理的最后一人，请刷新后重新办理。");
                } else {
                    /**
                     * 改变流程变量中users的值
                     */
                    try {
                        String userObj = variableManager.getVariable(tenantId, taskId, SysVariables.USERS);
                        List<String> users = userObj == null ? new ArrayList<>() : Y9JsonUtil.readValue(userObj, List.class);
                        if (users.size() == 0) {
                            List<String> usersTemp = new ArrayList<String>();
                            for (TaskModel t : list) {
                                usersTemp.add(t.getAssignee());
                            }
                            Map<String, Object> vmap = new HashMap<String, Object>(16);
                            vmap.put(SysVariables.USERS, usersTemp);
                            variableManager.setVariables(tenantId, taskId, vmap);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    taskManager.complete(tenantId, taskId);
                }
            }
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "办理失败");
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 处理完成，串行时使用
     *
     * @param tenantId 租户id
     * @param userId 人员id
     * @param positionId 岗位id
     * @param taskId 任务id
     * @param response
     */
    @RequestMapping(value = "/handleSerial")
    public void handleSerial(@RequestHeader("auth-tenantId") String tenantId, @RequestHeader("auth-userId") String userId, @RequestHeader("auth-positionId") String positionId, @RequestParam String taskId, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>(16);
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            Position position = positionApi.getPosition(tenantId, positionId).getData();
            Y9LoginUserHolder.setPosition(position);
            TaskModel task = taskManager.findById(tenantId, taskId);
            Map<String, Object> vars = task.getVariables();// 获取流程中当前任务的所有变量
            // vars.put(SysVariables.TASKSENDER, position.getName());
            // vars.put(SysVariables.TASKSENDERID, position.getId());
            taskManager.completeWithVariables(tenantId, task.getId(), vars);
            // List<TaskModel> taskNextList1 = taskManager.findByProcessInstanceId(tenantId,
            // task.getProcessInstanceId());
            // for (TaskModel taskNext : taskNextList1) {
            // Map<String, Object> vars1 = new HashMap<String, Object>(16);
            // vars1.put(SysVariables.TASKSENDER, position.getName());
            // vars1.put(SysVariables.TASKSENDERID, position.getId());
            // variableManager.setVariablesLocal(tenantId, taskNext.getId(), vars1);
            // }
            process4SearchService.saveToDataCenter(tenantId, taskId, task.getProcessInstanceId());
            map.put(UtilConsts.SUCCESS, true);
            map.put("msg", "办理成功!");
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, true);
            map.put("msg", "办理失败!");
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 恢复待办
     *
     * @param tenantId 租户id
     * @param userId 人员id
     * @param positionId 岗位id
     * @param taskId 任务id
     * @param processInstanceId 流程实例id
     * @param desc 描述
     * @param response
     */
    @RequestMapping(value = "/multipleResumeToDo")
    public void multipleResumeToDo(@RequestHeader("auth-tenantId") String tenantId, @RequestHeader("auth-userId") String userId, @RequestHeader("auth-positionId") String positionId, @RequestParam(required = false) String processInstanceId, @RequestParam(required = false) String desc,
        HttpServletResponse response) {
        Y9LoginUserHolder.setTenantId(tenantId);
        Position position = positionApi.getPosition(tenantId, positionId).getData();
        Y9LoginUserHolder.setPosition(position);
        Map<String, Object> map = new HashMap<String, Object>(16);
        try {
            buttonOperationService.multipleResumeToDo(processInstanceId, desc);
            map.put(UtilConsts.SUCCESS, true);
            map.put("msg", "恢复待办成功");
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "恢复待办失败");
            log.error("手机端恢复待办异常");
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 拒签：抢占式办理时，拒签就把自己从多个抢占办理的人中排除掉
     *
     * @param tenantId 租户id
     * @param userId 人员id
     * @param positionId 岗位id
     * @param taskId 任务id
     * @param isLastPerson4RefuseClaim 是否最后一人拒签
     * @param response
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/refuseClaim")
    public void refuseClaim(@RequestHeader("auth-tenantId") String tenantId, @RequestHeader("auth-userId") String userId, @RequestHeader("auth-positionId") String positionId, @RequestParam String taskId, @RequestParam Boolean isLastPerson4RefuseClaim, HttpServletResponse response) {
        Y9LoginUserHolder.setTenantId(tenantId);
        Map<String, Object> map = new HashMap<String, Object>(16);
        String activitiUser = "";
        try {
            TaskModel task = taskManager.findById(tenantId, taskId);
            map.put(UtilConsts.SUCCESS, true);
            map.put("msg", "拒签成功");
            if (isLastPerson4RefuseClaim) {// 最后一人拒签，退回
                try {
                    buttonOperationManager.refuseClaimRollback(tenantId, userId, taskId);
                } catch (Exception e) {
                    taskManager.unclaim(tenantId, taskId);// 失败则撤销签收
                    map.put(UtilConsts.SUCCESS, false);
                    map.put("msg", "拒签失败");
                    e.printStackTrace();
                }
            } else {
                if (task != null) {
                    String assigneeId = task.getAssignee();
                    if (StringUtils.isBlank(assigneeId)) {
                        Map<String, Object> vars = variableManager.getVariables(tenantId, taskId);
                        ArrayList<String> users = (ArrayList<String>)vars.get(SysVariables.USERS);
                        for (Object obj : users) {
                            String user = obj.toString();
                            if (user.contains(positionId)) {
                                activitiUser = user;
                                break;
                            }
                        }
                        taskManager.deleteCandidateUser(tenantId, taskId, activitiUser);
                    } else {
                        String assigneeName = positionApi.getPosition(tenantId, assigneeId).getData().getName();
                        map.put(UtilConsts.SUCCESS, false);
                        map.put("msg", "任务已被用户:" + assigneeName + "签收！");
                    }
                }
            }
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "拒签失败");
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 重定位
     *
     * @param tenantId 租户id
     * @param userId 人员id
     * @param positionId 岗位id
     * @param taskId 任务id
     * @param repositionToTaskId 定位路由key
     * @param userChoice 人员id
     * @param response
     */
    @RequestMapping(value = "/reposition")
    public void reposition(@RequestHeader("auth-tenantId") String tenantId, @RequestHeader("auth-userId") String userId, @RequestHeader("auth-positionId") String positionId, @RequestParam String taskId, @RequestParam String repositionToTaskId, @RequestParam String userChoice,
        HttpServletResponse response) {
        Y9LoginUserHolder.setTenantId(tenantId);
        Map<String, Object> map = new HashMap<String, Object>(16);
        try {
            map.put(UtilConsts.SUCCESS, true);
            if (StringUtils.isNotBlank(taskId)) {
                specialOperationManager.reposition4Position(tenantId, positionId, taskId, repositionToTaskId, Y9Util.stringToList(userChoice, ","), "重定向", "");
                map.put("msg", "重定向成功");
            } else {
                map.put(UtilConsts.SUCCESS, false);
                map.put("msg", "重定向失败");
            }
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "重定向失败");
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 重定位
     *
     * @param tenantId 租户id
     * @param userId 人员id
     * @param positionId 岗位id
     * @param taskId 任务id
     * @param userChoice 人员id
     * @param request
     * @param response
     */
    @RequestMapping(value = "/reposition1")
    public void reposition1(@RequestHeader("auth-tenantId") String tenantId, @RequestHeader("auth-userId") String userId, @RequestHeader("auth-positionId") String positionId, @RequestParam String taskId, @RequestParam String userChoice, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>(16);
        Y9LoginUserHolder.setTenantId(tenantId);
        try {
            TaskModel task = taskManager.findById(tenantId, taskId);
            buttonOperationManager.reposition(tenantId, positionId, taskId, "", Y9Util.stringToList(userChoice, ","), "重定向", "");
            process4SearchService.saveToDataCenter(tenantId, taskId, task.getProcessInstanceId());
        } catch (Exception e) {
            e.printStackTrace();
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "重定位失败");
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 退回
     *
     * @param tenantId 租户id
     * @param userId 人员id
     * @param positionId 岗位id
     * @param taskId 任务id
     * @param response
     */
    @RequestMapping(value = "/rollback")
    public void rollback(@RequestHeader("auth-tenantId") String tenantId, @RequestHeader("auth-userId") String userId, @RequestHeader("auth-positionId") String positionId, @RequestParam String taskId, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>(16);
        map.put(UtilConsts.SUCCESS, true);
        map.put("msg", "退回成功");
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            Position position = positionApi.getPosition(tenantId, positionId).getData();
            Y9LoginUserHolder.setPosition(position);
            TaskModel task = taskManager.findById(tenantId, taskId);
            List<TaskModel> taskList = taskManager.findByProcessInstanceId(tenantId, task.getProcessInstanceId());
            String type = processDefinitionManager.getNodeType(tenantId, task.getProcessDefinitionId(), task.getTaskDefinitionKey());
            String reason = "";
            if (SysVariables.PARALLEL.equals(type) && taskList.size() > 1) {// 并行退回，并行多于2人时，退回使用减签方式
                if (StringUtils.isEmpty(reason)) {
                    reason = "未填写。";
                }
                reason = "该任务由" + position.getName() + "退回:" + reason;
                Map<String, Object> val = new HashMap<String, Object>();
                val.put("val", reason);
                variableManager.setVariableLocal(tenantId, taskId, "rollBackReason", val);
                multiInstanceService.removeExecution(task.getExecutionId(), taskId, task.getAssignee());
            } else {
                buttonOperationManager.rollBack(tenantId, positionId, taskId, reason);
            }
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "退回失败");
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 返回发起人
     *
     * @param tenantId 租户id
     * @param userId 人员id
     * @param positionId 岗位id
     * @param taskId 任务id
     * @param response
     */
    @RequestMapping(value = "/rollbackToStartor")
    public void rollbackToStartor(@RequestHeader("auth-tenantId") String tenantId, @RequestHeader("auth-userId") String userId, @RequestHeader("auth-positionId") String positionId, @RequestParam String taskId, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>(16);
        map.put(UtilConsts.SUCCESS, false);
        map.put("msg", "返回发起人失败");
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            buttonOperationManager.rollbackToStartor(tenantId, positionId, taskId, "");
            map.put(UtilConsts.SUCCESS, true);
            map.put("msg", "返回发起人成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 返回发送人
     *
     * @param tenantId 租户id
     * @param userId 人员id
     * @param positionId 岗位id
     * @param taskId 任务id
     * @param response
     */
    @RequestMapping(value = "/sendToSender")
    public void sendToSender(@RequestHeader("auth-tenantId") String tenantId, @RequestHeader("auth-userId") String userId, @RequestHeader("auth-positionId") String positionId, @RequestParam String taskId, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>(16);
        map.put(UtilConsts.SUCCESS, false);
        map.put("msg", "返回发送人失败");
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            buttonOperationManager.rollbackToSender(tenantId, positionId, taskId);
            map.put(UtilConsts.SUCCESS, true);
            map.put("msg", "返回发送人成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 发送拟稿人
     *
     * @param tenantId 租户id
     * @param userId 人员id
     * @param positionId 岗位id
     * @param taskId 任务id
     * @param response
     */
    @RequestMapping(value = "/sendToStartor")
    public void sendToStartor(@RequestHeader("auth-tenantId") String tenantId, @RequestHeader("auth-userId") String userId, @RequestHeader("auth-positionId") String positionId, @RequestParam String taskId, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>(16);
        map.put(UtilConsts.SUCCESS, false);
        map.put("msg", "发送拟稿人失败");
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            TaskModel taskModel = taskManager.findById(tenantId, taskId);
            String routeToTaskId = taskModel.getTaskDefinitionKey();
            String processInstanceId = taskModel.getProcessInstanceId();
            String processDefinitionKey = taskModel.getProcessDefinitionId().split(":")[0];
            ProcessParamModel processParamModel = processParamManager.findByProcessInstanceId(tenantId, processInstanceId);
            String itemId = processParamModel.getItemId();
            String processSerialNumber = processParamModel.getProcessSerialNumber();
            Map<String, Object> variables = new HashMap<String, Object>(16);

            String user = variableManager.getVariableLocal(tenantId, taskId, SysVariables.TASKSENDERID);
            String userChoice = "3:" + user;

            String multiInstance = processDefinitionManager.getNodeType(tenantId, taskModel.getProcessDefinitionId(), routeToTaskId);
            String sponsorHandle = "";
            if (multiInstance.equals(SysVariables.PARALLEL)) {
                sponsorHandle = "true";
            }
            documentManager.saveAndForwarding(tenantId, positionId, processInstanceId, taskId, sponsorHandle, itemId, processSerialNumber, processDefinitionKey, userChoice, "", routeToTaskId, variables);
            map.put(UtilConsts.SUCCESS, true);
            map.put("msg", "发送拟稿人成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 收回
     *
     * @param tenantId 租户id
     * @param userId 人员id
     * @param positionId 岗位id
     * @param taskId 任务id
     * @param response
     */
    @RequestMapping(value = "/takeback")
    public void takeback(@RequestHeader("auth-tenantId") String tenantId, @RequestHeader("auth-userId") String userId, @RequestHeader("auth-positionId") String positionId, @RequestParam String taskId, HttpServletResponse response) {
        Y9LoginUserHolder.setTenantId(tenantId);
        Map<String, Object> map = new HashMap<String, Object>(16);
        try {
            map.put(UtilConsts.SUCCESS, true);
            map.put("msg", "收回成功");
            buttonOperationManager.takeback(tenantId, positionId, taskId, "收回");
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "收回失败");
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 撤销签收：抢占式办理时，签收后，撤销签收可以让此公文重新抢占式办理
     *
     * @param tenantId 租户id
     * @param userId 人员id
     * @param positionId 岗位id
     * @param taskId 任务id
     * @param response
     */
    @RequestMapping(value = "/unclaim")
    public void unclaim(@RequestHeader("auth-tenantId") String tenantId, @RequestHeader("auth-userId") String userId, @RequestHeader("auth-positionId") String positionId, @RequestParam String taskId, HttpServletResponse response) {
        Y9LoginUserHolder.setTenantId(tenantId);
        Map<String, Object> map = new HashMap<String, Object>(16);
        try {
            map.put(UtilConsts.SUCCESS, true);
            if (StringUtils.isNotBlank(taskId)) {
                taskManager.unclaim(tenantId, taskId);
                map.put("msg", "撤销签收成功");
            } else {
                map.put(UtilConsts.SUCCESS, false);
                map.put("msg", "撤销签收失败");
            }
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "撤销签收失败");
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }
}
