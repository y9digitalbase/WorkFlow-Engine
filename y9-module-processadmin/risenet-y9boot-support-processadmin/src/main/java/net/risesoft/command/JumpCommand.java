package net.risesoft.command;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.flowable.bpmn.model.Activity;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.Process;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.FlowableEngineAgenda;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.engine.impl.bpmn.behavior.MultiInstanceActivityBehavior;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityManager;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.flowable.engine.impl.util.ProcessDefinitionUtil;
import org.flowable.identitylink.service.IdentityLinkService;
import org.flowable.task.service.TaskService;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.flowable.variable.service.VariableService;

import net.risesoft.model.user.UserInfo;
import net.risesoft.util.SysVariables;
import net.risesoft.y9.Y9LoginUserHolder;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/30
 */
public class JumpCommand implements Command<Void> {
    /**
     * 当前任务id
     */
    protected String taskId;
    /**
     * 目标任务节点id
     */
    protected String targetNodeId;

    /**
     * 办理人:如果是普通任务，users只能是一个人，否则会出现签收的情况
     */
    protected List<String> users;

    /**
     * 原因
     */
    protected String reason;

    public JumpCommand(String taskId, String targetNodeId, List<String> users, String reason) {
        this.taskId = taskId;
        this.targetNodeId = targetNodeId;
        this.users = users;
        this.reason = reason;
    }

    @Override
    public Void execute(CommandContext commandContext) {
        ExecutionEntityManager executionEntityManager = CommandContextUtil.getExecutionEntityManager();
        TaskService taskService = CommandContextUtil.getTaskService();
        IdentityLinkService identityLinkService = CommandContextUtil.getIdentityLinkService();
        VariableService variableService = CommandContextUtil.getVariableService();
        /**
         * 根据taskId获取执行实例信息
         */
        TaskEntity taskEntity = taskService.getTask(taskId);
        String executionId = taskEntity.getExecutionId();
        ExecutionEntity executionEntity = executionEntityManager.findById(executionId);
        /**
         * 获取当前流程信息
         */
        Process process = ProcessDefinitionUtil.getProcess(executionEntity.getProcessDefinitionId());
        /**
         * 获取当前节点信息
         */
        Activity flowElement = (Activity)process.getFlowElement(taskEntity.getTaskDefinitionKey());
        Object currentBehavior = flowElement.getBehavior();
        /**
         * 根据是否是多实例，执行对应的跳转操作,多实例，一定存在parentExecutionEntity
         */
        if ((currentBehavior instanceof MultiInstanceActivityBehavior)) {
            ExecutionEntity parentExecutionEntity = executionEntity.getParent();
            executionEntityManager.deleteChildExecutions(parentExecutionEntity, reason, false);
        } else {
            identityLinkService.deleteIdentityLinksByTaskId(taskId);
            variableService.deleteVariablesByExecutionId(executionId);
            taskService.deleteTask(taskEntity, true);
            CommandContextUtil.getHistoryManager().recordTaskEnd(taskEntity, executionEntity,
                Y9LoginUserHolder.getPersonId(), reason, new Date());
            CommandContextUtil.getActivityInstanceEntityManager().recordActivityEnd(executionEntity, reason);
        }

        /**
         * 获取目标节点的信息，并设置目标节点为当前执行实体的当前节点
         */
        FlowElement targetFlowElement = process.getFlowElement(targetNodeId);

        /**
         * 设置新任务的发送人和办理人-开始
         */
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        String taskSender = userInfo.getName(), userId = userInfo.getPersonId();
        String taskSenderId = userId, user = null;
        Map<String, Object> vars = new HashMap<>(16);
        vars.put(SysVariables.TASKSENDER, taskSender);
        vars.put(SysVariables.TASKSENDERID, taskSenderId);
        if (users.size() == 1) {
            user = users.get(0);
        }
        vars.put(SysVariables.USER, user);
        vars.put(SysVariables.USERS, users);
        /**
         * 设置新任务的发送人和办理人-结束
         */

        FlowableEngineAgenda flowableEngineAgenda = CommandContextUtil.getAgenda();
        if (currentBehavior instanceof MultiInstanceActivityBehavior) {
            ExecutionEntity parentExecutionEntity = executionEntity.getParent();
            parentExecutionEntity.setCurrentFlowElement(targetFlowElement);
            parentExecutionEntity.setActive(true);
            parentExecutionEntity.setMultiInstanceRoot(false);
            parentExecutionEntity.setVariables(vars);
            executionEntityManager.update(parentExecutionEntity);
            flowableEngineAgenda.planContinueProcessInCompensation(parentExecutionEntity);
        } else {
            executionEntity.setCurrentFlowElement(targetFlowElement);
            executionEntity.setVariables(vars);
            flowableEngineAgenda.planContinueProcessInCompensation(executionEntity);
        }
        /**
         * 触发退回任务删除事件
         */
        ProcessEngineConfigurationImpl processEngineConfiguration =
            CommandContextUtil.getProcessEngineConfiguration(commandContext);
        processEngineConfiguration.getListenerNotificationHelper().executeTaskListeners(taskEntity,
            TaskListener.EVENTNAME_DELETE);
        return null;
    }
}
