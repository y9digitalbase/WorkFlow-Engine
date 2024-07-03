package net.risesoft.api;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.EndEvent;
import org.flowable.bpmn.model.ExclusiveGateway;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.GraphicInfo;
import org.flowable.bpmn.model.ParallelGateway;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.Execution;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.api.itemadmin.ProcessParamApi;
import net.risesoft.api.itemadmin.position.OfficeDoneInfo4PositionApi;
import net.risesoft.api.itemadmin.position.ProcessTrack4PositionApi;
import net.risesoft.api.platform.org.PersonApi;
import net.risesoft.api.processadmin.BpmnModelApi;
import net.risesoft.model.itemadmin.OfficeDoneInfoModel;
import net.risesoft.model.itemadmin.ProcessParamModel;
import net.risesoft.model.itemadmin.ProcessTrackModel;
import net.risesoft.model.platform.Person;
import net.risesoft.model.processadmin.FlowNodeModel;
import net.risesoft.model.processadmin.FlowableBpmnModel;
import net.risesoft.model.processadmin.LinkNodeModel;
import net.risesoft.model.processadmin.Y9BpmnModel;
import net.risesoft.model.processadmin.Y9FlowChartModel;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.CustomHistoricActivityService;
import net.risesoft.service.CustomHistoricProcessService;
import net.risesoft.service.CustomHistoricTaskService;
import net.risesoft.service.CustomHistoricVariableService;
import net.risesoft.service.FlowableTenantInfoHolder;
import net.risesoft.util.SysVariables;
import net.risesoft.y9.Y9Context;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.util.Y9Util;

/**
 * 流程图接口
 *
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/30
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/services/rest/bpmnModel")
public class BpmnModelApiImpl implements BpmnModelApi {

    private final CustomHistoricProcessService customHistoricProcessService;

    private final CustomHistoricTaskService customHistoricTaskService;

    private final CustomHistoricVariableService customHistoricVariableService;

    private final TaskService taskService;

    private final RuntimeService runtimeService;

    private final CustomHistoricActivityService customHistoricActivityService;

    private final RepositoryService repositoryService;

    private final PersonApi personManager;

    private final OfficeDoneInfo4PositionApi officeDoneInfoManager;

    private final ProcessParamApi processParamManager;

    private final ProcessTrack4PositionApi processTrackManager;

    /**
     * 删除模型
     *
     * @param tenantId 租户id
     * @param modelId 模型id
     * @return Y9Result<String>
     */
    @Override
    public Y9Result<Object> deleteModel(@RequestParam String tenantId, @RequestParam String modelId) {
        FlowableTenantInfoHolder.setTenantId(tenantId);
        return Y9Result.success();
    }

    /**
     * 根据Model部署流程
     *
     * @param tenantId 租户id
     * @param modelId 模型id
     * @return Y9Result<String>
     */
    @Override
    public Y9Result<Object> deployModel(@RequestParam String tenantId, @RequestParam String modelId) {
        FlowableTenantInfoHolder.setTenantId(tenantId);
        return Y9Result.success();
    }

    /**
     * 生成流程图
     *
     * @param tenantId 租户id
     * @param processInstanceId 流程实例id
     * @return Y9Result<String>
     */
    @Override
    public Y9Result<String> genProcessDiagram(@RequestParam String tenantId, @RequestParam String processInstanceId) {
        FlowableTenantInfoHolder.setTenantId(tenantId);
        HistoricProcessInstance pi = customHistoricProcessService.getById(processInstanceId);
        // 流程走完的不显示图
        if (pi == null) {
            return null;
        }
        InputStream in;
        ProcessEngine processEngine = Y9Context.getBean(ProcessEngine.class);
        BpmnModel bpmnModel = repositoryService.getBpmnModel(pi.getProcessDefinitionId());
        ProcessEngineConfiguration engConf = processEngine.getProcessEngineConfiguration();
        ProcessDiagramGenerator diagramGenerator = engConf.getProcessDiagramGenerator();
        if (pi.getEndTime() == null) {
            Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
            // 使用流程实例ID，查询正在执行的执行对象表，返回流程实例对象
            String instanceId = task.getProcessInstanceId();
            List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(instanceId).list();

            // 得到正在执行的Activity的Id
            List<String> activityIds = new ArrayList<>();
            List<String> flows = new ArrayList<>();
            for (Execution exe : executions) {
                List<String> ids = runtimeService.getActiveActivityIds(exe.getId());
                activityIds.addAll(ids);
            }
            // 获取流程图
            in = diagramGenerator.generateDiagram(bpmnModel, "png", activityIds, flows, engConf.getActivityFontName(),
                engConf.getLabelFontName(), engConf.getAnnotationFontName(), engConf.getClassLoader(), 1.0, false);
        } else {
            // 获取流程图
            in = diagramGenerator.generateDiagram(bpmnModel, "png", engConf.getActivityFontName(),
                engConf.getLabelFontName(), engConf.getAnnotationFontName(), engConf.getClassLoader(), false);
        }
        byte[] buf;
        try {
            buf = IOUtils.toByteArray(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Y9Result.success(Base64.getEncoder().encodeToString(buf));
    }

    /**
     * 获取流程图模型
     *
     * @param tenantId 租户id
     * @param processInstanceId 流程实例id
     * @return Y9Result<Y9BpmnModel>
     */
    @Override
    public Y9Result<Y9BpmnModel> getBpmnModel(@RequestParam String tenantId, @RequestParam String processInstanceId) {
        FlowableTenantInfoHolder.setTenantId(tenantId);
        HistoricProcessInstance pi = customHistoricProcessService.getById(processInstanceId);
        // 流程走完的不显示图
        if (pi == null) {
            return Y9Result.failure("流程已办结");
        }
        String txtFlowPath = "";
        List<FlowNodeModel> nodeDataArray = new ArrayList<>();
        List<LinkNodeModel> linkDataArray = new ArrayList<>();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(pi.getProcessDefinitionId());
        Map<String, GraphicInfo> infoMap = bpmnModel.getLocationMap();
        org.flowable.bpmn.model.Process process = bpmnModel.getProcesses().get(0);
        List<FlowElement> flowElements = (List<FlowElement>)process.getFlowElements();
        for (FlowElement flowElement : flowElements) {
            if (flowElement instanceof StartEvent startEvent) {
                GraphicInfo graphicInfo = infoMap.get(startEvent.getId());
                txtFlowPath = startEvent.getId();
                nodeDataArray.add(new FlowNodeModel(startEvent.getId(), "Start", "开始", "Circle", "#4fba4f", "1",
                    graphicInfo.getX() - 100 + " " + graphicInfo.getY(), ""));
                // 获取开始节点输出路线
                List<SequenceFlow> list = startEvent.getOutgoingFlows();
                for (SequenceFlow tr : list) {
                    FlowElement fe = tr.getTargetFlowElement();
                    if ((fe instanceof UserTask u)) {
                        linkDataArray.add(new LinkNodeModel(startEvent.getId(), u.getId()));
                    }
                }
            } else if (flowElement instanceof UserTask userTask) {
                GraphicInfo graphicInfo = infoMap.get(userTask.getId());
                nodeDataArray.add(new FlowNodeModel(userTask.getId(), "", userTask.getName(), "", "", "",
                    graphicInfo.getX() + " " + graphicInfo.getY(), "111111111"));
                List<SequenceFlow> list = userTask.getOutgoingFlows();
                for (SequenceFlow tr : list) {
                    FlowElement fe = tr.getTargetFlowElement();
                    if (fe instanceof ExclusiveGateway) {
                        // 目标节点时排他网关时，需要再次获取输出路线
                        ExclusiveGateway gateway = (ExclusiveGateway)fe;
                        List<SequenceFlow> outgoingFlows = gateway.getOutgoingFlows();
                        for (SequenceFlow sf : outgoingFlows) {
                            FlowElement element = sf.getTargetFlowElement();
                            if (element instanceof UserTask task) {
                                linkDataArray.add(new LinkNodeModel(userTask.getId(), task.getId()));
                            } else if (element instanceof EndEvent endEvent) {
                                linkDataArray.add(new LinkNodeModel(userTask.getId(), endEvent.getId()));
                            } else if (element instanceof ParallelGateway parallelgateway) {
                                List<SequenceFlow> outgoingFlows1 = parallelgateway.getOutgoingFlows();
                                for (SequenceFlow sf1 : outgoingFlows1) {
                                    FlowElement element1 = sf1.getTargetFlowElement();
                                    if (element1 instanceof UserTask task1) {
                                        linkDataArray.add(new LinkNodeModel(userTask.getId(), task1.getId()));
                                    }
                                }
                            }
                        }
                    } else if ((fe instanceof UserTask u)) {
                        linkDataArray.add(new LinkNodeModel(userTask.getId(), u.getId()));
                    } else if (fe instanceof EndEvent endEvent) {
                        linkDataArray.add(new LinkNodeModel(userTask.getId(), endEvent.getId()));
                    } else if (fe instanceof ParallelGateway gateway) {
                        List<SequenceFlow> outgoingFlows = gateway.getOutgoingFlows();
                        for (SequenceFlow sf : outgoingFlows) {
                            FlowElement element = sf.getTargetFlowElement();
                            if (element instanceof UserTask task) {
                                linkDataArray.add(new LinkNodeModel(userTask.getId(), task.getId()));
                            }
                        }
                    }
                }
            } else if (flowElement instanceof EndEvent endEvent) {
                GraphicInfo graphicInfo = infoMap.get(endEvent.getId());
                nodeDataArray.add(new FlowNodeModel(endEvent.getId(), "End", "结束", "Circle", "#CE0620", "4",
                    graphicInfo.getX() + " " + graphicInfo.getY(), ""));
            }
        }

        List<HistoricTaskInstance> list = customHistoricTaskService.getByProcessInstanceId(processInstanceId, "");
        for (HistoricTaskInstance task : list) {
            txtFlowPath = Y9Util.genCustomStr(txtFlowPath, task.getTaskDefinitionKey());
        }
        return Y9Result.success(new Y9BpmnModel(nodeDataArray, linkDataArray, txtFlowPath, pi.getEndTime() != null));
    }

    /**
     * 获取流程图数据
     *
     * @param tenantId 租户id
     * @param processInstanceId 流程实例id
     * @return Y9Result<Y9FlowChartModel>
     */
    @Override
    public Y9Result<Y9FlowChartModel> getFlowChart(@RequestParam String tenantId,
        @RequestParam String processInstanceId) {
        Y9FlowChartModel flowChartModel = new Y9FlowChartModel();
        FlowableTenantInfoHolder.setTenantId(tenantId);
        Y9LoginUserHolder.setTenantId(tenantId);
        List<Y9FlowChartModel> listMap = new ArrayList<>();
        String activityId = "";
        String parentId = "";
        String year = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            HistoricProcessInstance hpi = customHistoricProcessService.getById(processInstanceId);
            if (hpi == null) {
                OfficeDoneInfoModel officeDoneInfo =
                    officeDoneInfoManager.findByProcessInstanceId(tenantId, processInstanceId).getData();
                if (officeDoneInfo == null) {
                    ProcessParamModel processParam =
                        processParamManager.findByProcessInstanceId(tenantId, processInstanceId).getData();
                    year = processParam.getCreateTime().substring(0, 4);
                } else {
                    year = officeDoneInfo.getStartTime().substring(0, 4);
                }
            }
            List<HistoricActivityInstance> list =
                customHistoricActivityService.getByProcessInstanceIdAndYear(processInstanceId, year);
            list.sort((o1, o2) -> {
                if (o1.getEndTime() == null || o2.getEndTime() == null) {
                    return 0;
                }
                long endTime1 = o1.getEndTime().getTime();
                long endTime2 = o2.getEndTime().getTime();
                return Long.compare(endTime1, endTime2);
            });
            int num = 0;
            for (HistoricActivityInstance his : list) {
                String id = his.getId();
                String taskId = his.getTaskId();
                String type = his.getActivityType();
                if (type.contains(SysVariables.STARTEVENT) || type.contains("Flow")) {
                    continue;
                }
                if (type.contains(SysVariables.ENDEVENT)) {
                    num += 1;
                    String completer = listMap.get(listMap.size() - 1).getTitle();
                    if (completer.contains("主办")) {
                        completer = completer.substring(0, completer.length() - 4);
                    }
                    Y9FlowChartModel flowChart = new Y9FlowChartModel();
                    flowChart.setId(id);
                    flowChart.setName("办结");
                    flowChart.setTitle(completer);
                    flowChart.setParentId(parentId);
                    flowChart.setClassName("specialColor");
                    flowChart.setNum(num);
                    flowChart.setEndTime(his.getEndTime() != null ? his.getEndTime().getTime() : 0);
                    listMap.add(flowChart);
                    continue;
                }
                if (type.contains(SysVariables.GATEWAY)) {
                    num += 1;
                    continue;
                }
                String userId = his.getAssignee();
                Person person = personManager.get(tenantId, userId).getData();
                if ("".equals(activityId) || activityId.equals(his.getActivityId())) {

                    HistoricVariableInstance historicVariableInstance = customHistoricVariableService
                        .getByTaskIdAndVariableName(taskId, SysVariables.PARALLELSPONSOR, year);
                    Y9FlowChartModel flowChart = new Y9FlowChartModel();
                    flowChart.setId(taskId);
                    flowChart.setName(his.getActivityName());
                    flowChart.setTitle(person != null ? person.getName() : "该用户不存在");
                    if (historicVariableInstance != null) {
                        flowChart.setTitle(person != null ? person.getName() + "(主办)" : "该用户不存在");
                    }
                    flowChart.setParentId(parentId);
                    flowChart.setClassName(his.getEndTime() != null ? "serverColor" : "specialColor");
                    flowChart.setNum(num);
                    flowChart.setEndTime(his.getEndTime() != null ? his.getEndTime().getTime() : 0);
                    listMap.add(flowChart);
                    activityId = his.getActivityId();
                    parentId = taskId;
                } else {
                    num += 1;
                    activityId = his.getActivityId();
                    HistoricVariableInstance historicVariableInstance = customHistoricVariableService
                        .getByTaskIdAndVariableName(taskId, SysVariables.PARALLELSPONSOR, year);
                    Y9FlowChartModel flowChart = new Y9FlowChartModel();
                    flowChart.setId(taskId);
                    flowChart.setName(his.getActivityName());
                    flowChart.setTitle(person != null ? person.getName() : "该用户不存在");
                    if (historicVariableInstance != null) {
                        flowChart.setTitle(person != null ? person.getName() + "(主办)" : "该用户不存在");
                    }
                    flowChart.setParentId(parentId);
                    flowChart.setClassName(his.getEndTime() != null ? "serverColor" : "specialColor");
                    flowChart.setNum(num);
                    flowChart.setEndTime(his.getEndTime() != null ? his.getEndTime().getTime() : 0);
                    listMap.add(flowChart);
                }

                List<ProcessTrackModel> ptList = processTrackManager.findByTaskIdAsc(tenantId, taskId).getData();
                String parentId0 = taskId;
                for (int j = 0; j < ptList.size(); j++) {
                    num += 1;
                    ProcessTrackModel pt = ptList.get(j);
                    if (j != 0) {
                        parentId0 = pt.getId();
                    }
                    Y9FlowChartModel flowChart = new Y9FlowChartModel();
                    flowChart.setId(pt.getId());
                    flowChart.setName(pt.getTaskDefName());
                    flowChart.setTitle(pt.getSenderName());
                    flowChart.setParentId(parentId0);
                    flowChart.setClassName(StringUtils.isNotBlank(pt.getEndTime()) ? "serverColor" : "specialColor");
                    flowChart.setNum(num);
                    flowChart
                        .setEndTime(StringUtils.isNotBlank(pt.getEndTime()) ? sdf.parse(pt.getEndTime()).getTime() : 0);
                    listMap.add(flowChart);
                    if (j == ptList.size() - 1) {
                        parentId = parentId0;
                    }
                }
            }
            int oldNum = 0;
            int newNum = 0;
            for (int i = 0; i < listMap.size(); i++) {
                Y9FlowChartModel y9FlowChartModel = listMap.get(i);
                int currNum = y9FlowChartModel.getNum();
                if (currNum == 0) {
                    parentId = y9FlowChartModel.getId();
                    y9FlowChartModel.setParentId("");
                }
                if (currNum != oldNum) {
                    y9FlowChartModel.setParentId(parentId);
                    if (newNum == 0) {
                        newNum = currNum;
                    }
                    if (newNum != currNum) {
                        oldNum = newNum;
                        newNum = currNum;
                        parentId = listMap.get(i - 1).getId();
                        y9FlowChartModel.setParentId(parentId);
                    }
                }
            }
            parentId = "0";
            List<Y9FlowChartModel> childrenMap = new ArrayList<>();
            for (int i = listMap.size() - 1; i >= 0; i--) {
                Y9FlowChartModel y9FlowChartModel = listMap.get(i);
                String id = y9FlowChartModel.getId();
                if (StringUtils.isNotBlank(parentId) && !parentId.equals(id)) {
                    parentId = y9FlowChartModel.getParentId();
                    childrenMap.add(y9FlowChartModel);
                } else {
                    y9FlowChartModel.setChildren(childrenMap);
                    y9FlowChartModel.setCollapsed(false);
                    parentId = y9FlowChartModel.getParentId();
                    childrenMap = new ArrayList<>();
                    childrenMap.add(y9FlowChartModel);
                    if ("".equals(parentId)) {
                        flowChartModel = y9FlowChartModel;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("获取流程图数据失败", e);
        }
        return Y9Result.success(flowChartModel);
    }

    /**
     * 获取模型列表
     *
     * @param tenantId 租户id
     * @return Y9Result<List<FlowableBpmnModel>>
     */
    @Override
    public Y9Result<List<FlowableBpmnModel>> getModelList(@RequestParam String tenantId) {
        FlowableTenantInfoHolder.setTenantId(tenantId);
        List<Map<String, Object>> items = new ArrayList<>();
        return Y9Result.success(items, "获取成功");
    }

    /**
     * 获取流程设计模型xml
     *
     * @param tenantId 租户id
     * @param modelId 模型id
     * @return Y9Result<FlowableBpmnModel>
     */
    @Override
    public Y9Result<FlowableBpmnModel> getModelXml(@RequestParam String tenantId, @RequestParam String modelId) {
        FlowableTenantInfoHolder.setTenantId(tenantId);
        Map<String, Object> map = new HashMap<>();
        return Y9Result.success(map, "获取成功");
    }

    /**
     * 导入流程模型
     *
     * @param tenantId 租户id
     * @param userId 用户id
     * @param file 模型文件
     * @return Y9Result<Object>
     */
    @Override
    public Y9Result<Object> importProcessModel(@RequestParam String tenantId, @RequestParam String userId,
        @RequestParam MultipartFile file) {
        Map<String, Object> map = new HashMap<>(16);
        map.put("success", false);
        map.put("msg", "导入失败");
        return map;
    }

    /**
     * 保存模型xml
     *
     * @param tenantId 租户id
     * @param userId 用户id
     * @param modelId 模型id
     * @param file 模型文件
     * @return Y9Result<String>
     */
    @Override
    public Y9Result<Object> saveModelXml(@RequestParam String tenantId, @RequestParam String userId,
        @RequestParam String modelId, @RequestParam MultipartFile file) {
        return Y9Result.failure("保存失败");
    }
}
