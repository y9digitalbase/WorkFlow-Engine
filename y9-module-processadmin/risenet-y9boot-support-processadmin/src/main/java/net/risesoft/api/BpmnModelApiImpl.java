package net.risesoft.api;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.converter.BpmnXMLConverter;
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
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.Execution;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.validation.ProcessValidator;
import org.flowable.validation.ProcessValidatorFactory;
import org.flowable.validation.ValidationError;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;

import net.risesoft.api.itemadmin.OfficeDoneInfoApi;
import net.risesoft.api.itemadmin.ProcessParamApi;
import net.risesoft.api.itemadmin.ProcessTrackApi;
import net.risesoft.api.platform.org.PersonApi;
import net.risesoft.api.processadmin.BpmnModelApi;
import net.risesoft.model.itemadmin.OfficeDoneInfoModel;
import net.risesoft.model.itemadmin.ProcessParamModel;
import net.risesoft.model.itemadmin.ProcessTrackModel;
import net.risesoft.model.platform.Person;
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

    private final OfficeDoneInfoApi officeDoneInfoManager;

    private final ProcessParamApi processParamManager;

    private final ProcessTrackApi processTrackManager;

    /**
     * 删除模型
     *
     * @param tenantId 租户id
     * @param modelId  模型id
     * @return Y9Result<String>
     */
    @Override
    @RequestMapping(value = "/deleteModel", method = RequestMethod.POST, produces = "application/json")
    public Y9Result<String> deleteModel(@RequestParam String tenantId, @RequestParam String modelId) {
        FlowableTenantInfoHolder.setTenantId(tenantId);
        return Y9Result.successMsg("删除成功");
    }

    /**
     * 根据Model部署流程
     *
     * @param tenantId 租户id
     * @param modelId  模型id
     * @return Y9Result<String>
     */
    @Override
    @RequestMapping(value = "/deployModel", method = RequestMethod.POST, produces = "application/json")
    public Y9Result<String> deployModel(@RequestParam String tenantId, @RequestParam String modelId) {
        FlowableTenantInfoHolder.setTenantId(tenantId);
        return Y9Result.successMsg("部署成功");
    }

    /**
     * 生成流程图
     *
     * @param tenantId          租户id
     * @param processInstanceId 流程实例id
     * @return byte[]
     * @throws Exception Exception
     */
    @Override
    @PostMapping(value = "/genProcessDiagram", produces = MediaType.APPLICATION_JSON_VALUE)
    public byte[] genProcessDiagram(@RequestParam String tenantId, @RequestParam String processInstanceId)
        throws Exception {
        FlowableTenantInfoHolder.setTenantId(tenantId);
        HistoricProcessInstance pi = customHistoricProcessService.getById(processInstanceId);
        // 流程走完的不显示图
        if (pi == null) {
            return null;
        }
        InputStream in;
        ProcessEngine processEngine = Y9Context.getBean(ProcessEngine.class);
        BpmnModel bpmnModel = repositoryService.getBpmnModel(pi.getProcessDefinitionId());
        ProcessEngineConfiguration engconf = processEngine.getProcessEngineConfiguration();
        ProcessDiagramGenerator diagramGenerator = engconf.getProcessDiagramGenerator();
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
            in = diagramGenerator.generateDiagram(bpmnModel, "png", activityIds, flows, engconf.getActivityFontName(),
                engconf.getLabelFontName(), engconf.getAnnotationFontName(), engconf.getClassLoader(), 1.0, false);
        } else {
            // 获取流程图
            in = diagramGenerator.generateDiagram(bpmnModel, "png", engconf.getActivityFontName(),
                engconf.getLabelFontName(), engconf.getAnnotationFontName(), engconf.getClassLoader(), false);
        }

        byte[] buf = new byte[1024];
        int legth;
        try {
            ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
            while ((legth = in.read(buf)) != -1) {
                swapStream.write(buf, 0, legth);
            }
            return swapStream.toByteArray();
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * 获取流程图模型
     *
     * @param tenantId          租户id
     * @param processInstanceId 流程实例id
     * @return Map<String, Object>
     */
    @Override
    @GetMapping(value = "/getBpmnModel", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getBpmnModel(@RequestParam String tenantId, @RequestParam String processInstanceId)
        {
        Map<String, Object> map = new HashMap<>(16);
        FlowableTenantInfoHolder.setTenantId(tenantId);
        HistoricProcessInstance pi = customHistoricProcessService.getById(processInstanceId);
        // 流程走完的不显示图
        if (pi == null) {
            map.put("success", false);
            return map;
        }
        String txtFlowPath = "";
        List<Map<String, Object>> nodeDataArray = new ArrayList<>();
        List<Map<String, Object>> linkDataArray = new ArrayList<>();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(pi.getProcessDefinitionId());
        Map<String, GraphicInfo> infoMap = bpmnModel.getLocationMap();
        org.flowable.bpmn.model.Process process = bpmnModel.getProcesses().get(0);
        List<FlowElement> flowElements = (List<FlowElement>) process.getFlowElements();
        for (FlowElement flowElement : flowElements) {
            Map<String, Object> nodeMap = new HashMap<>(16);
            if (flowElement instanceof StartEvent startEvent) {
                GraphicInfo graphicInfo = infoMap.get(startEvent.getId());
                txtFlowPath = startEvent.getId();
                nodeMap.put("key", startEvent.getId());
                nodeMap.put("text", "开始");
                nodeMap.put("figure", "Circle");
                nodeMap.put("fill", "#4fba4f");
                nodeMap.put("category", "Start");
                nodeMap.put("stepType", "1");
                nodeMap.put("loc", graphicInfo.getX() - 100 + " " + graphicInfo.getY());
                nodeDataArray.add(nodeMap);
                // 获取开始节点输出路线
                List<SequenceFlow> list = startEvent.getOutgoingFlows();
                for (SequenceFlow tr : list) {
                    FlowElement fe = tr.getTargetFlowElement();
                    if ((fe instanceof UserTask u)) {
                        Map<String, Object> linkMap = new HashMap<>(16);
                        linkMap.put("from", startEvent.getId());
                        linkMap.put("to", u.getId());
                        linkDataArray.add(linkMap);
                    }
                }
            } else if (flowElement instanceof UserTask userTask) {
                GraphicInfo graphicInfo = infoMap.get(userTask.getId());
                nodeMap.put("key", userTask.getId());
                nodeMap.put("text", userTask.getName());
                nodeMap.put("remark", "111111111");
                nodeMap.put("loc", graphicInfo.getX() + " " + graphicInfo.getY());
                nodeDataArray.add(nodeMap);
                List<SequenceFlow> list = userTask.getOutgoingFlows();
                for (SequenceFlow tr : list) {
                    FlowElement fe = tr.getTargetFlowElement();
                    if (fe instanceof ExclusiveGateway) {
                        // 目标节点时排他网关时，需要再次获取输出路线
                        ExclusiveGateway gateway = (ExclusiveGateway) fe;
                        List<SequenceFlow> outgoingFlows = gateway.getOutgoingFlows();
                        for (SequenceFlow sf : outgoingFlows) {
                            FlowElement element = sf.getTargetFlowElement();
                            if (element instanceof UserTask task) {
                                Map<String, Object> linkMap = new HashMap<>(16);
                                linkMap.put("from", userTask.getId());
                                linkMap.put("to", task.getId());
                                linkDataArray.add(linkMap);
                            } else if (element instanceof EndEvent endEvent) {
                                Map<String, Object> linkMap = new HashMap<>(16);
                                linkMap.put("from", userTask.getId());
                                linkMap.put("to", endEvent.getId());
                                linkDataArray.add(linkMap);
                            } else if (element instanceof ParallelGateway parallelgateway) {
                                List<SequenceFlow> outgoingFlows1 = parallelgateway.getOutgoingFlows();
                                for (SequenceFlow sf1 : outgoingFlows1) {
                                    FlowElement element1 = sf1.getTargetFlowElement();
                                    if (element1 instanceof UserTask task1) {
                                        Map<String, Object> linkMap = new HashMap<>(16);
                                        linkMap.put("from", userTask.getId());
                                        linkMap.put("to", task1.getId());
                                        linkDataArray.add(linkMap);
                                    }
                                }
                            }
                        }
                    } else if ((fe instanceof UserTask u)) {
                        Map<String, Object> linkMap = new HashMap<>(16);
                        linkMap.put("from", userTask.getId());
                        linkMap.put("to", u.getId());
                        linkDataArray.add(linkMap);
                    } else if (fe instanceof EndEvent endEvent) {
                        Map<String, Object> linkMap = new HashMap<>(16);
                        linkMap.put("from", userTask.getId());
                        linkMap.put("to", endEvent.getId());
                        linkDataArray.add(linkMap);
                    } else if (fe instanceof ParallelGateway gateway) {
                        List<SequenceFlow> outgoingFlows = gateway.getOutgoingFlows();
                        for (SequenceFlow sf : outgoingFlows) {
                            FlowElement element = sf.getTargetFlowElement();
                            if (element instanceof UserTask task) {
                                Map<String, Object> linkMap = new HashMap<>(16);
                                linkMap.put("from", userTask.getId());
                                linkMap.put("to", task.getId());
                                linkDataArray.add(linkMap);
                            }
                        }
                    }
                }
            } else if (flowElement instanceof EndEvent endEvent) {
                GraphicInfo graphicInfo = infoMap.get(endEvent.getId());
                nodeMap.put("key", endEvent.getId());
                nodeMap.put("category", "End");
                nodeMap.put("text", "结束");
                nodeMap.put("figure", "Circle");
                nodeMap.put("fill", "#CE0620");
                nodeMap.put("stepType", "4");
                nodeMap.put("loc", graphicInfo.getX() + " " + graphicInfo.getY());
                nodeDataArray.add(nodeMap);
            }
        }

        List<HistoricTaskInstance> list = customHistoricTaskService.getByProcessInstanceId(processInstanceId, "");
        for (HistoricTaskInstance task : list) {
            txtFlowPath = Y9Util.genCustomStr(txtFlowPath, task.getTaskDefinitionKey());
        }
        map.put("nodeDataArray", nodeDataArray);
        map.put("linkDataArray", linkDataArray);
        map.put("txtFlowPath", txtFlowPath);
        map.put("isCompleted", pi.getEndTime() != null);
        map.put("success", true);
        return map;
    }

    /**
     * 获取流程图数据
     *
     * @param tenantId          租户id
     * @param processInstanceId 流程实例id
     * @return Map<String, Object>
     */
    @Override
    @GetMapping(value = "/getFlowChart", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getFlowChart(@RequestParam String tenantId, @RequestParam String processInstanceId)
        {
        Map<String, Object> resMap = new HashMap<>(16);
        FlowableTenantInfoHolder.setTenantId(tenantId);
        Y9LoginUserHolder.setTenantId(tenantId);
        List<Map<String, Object>> listMap = new ArrayList<>();
        String activityId = "";
        String parentId = "";
        String year = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            HistoricProcessInstance hpi = customHistoricProcessService.getById(processInstanceId);
            if (hpi == null) {
                OfficeDoneInfoModel officeDoneInfo =
                    officeDoneInfoManager.findByProcessInstanceId(tenantId, processInstanceId);
                if (officeDoneInfo == null) {
                    ProcessParamModel processParam =
                        processParamManager.findByProcessInstanceId(tenantId, processInstanceId);
                    year = processParam.getCreateTime().substring(0, 4);
                } else {
                    year = officeDoneInfo.getStartTime().substring(0, 4);
                }
            }
            List<HistoricActivityInstance> list =
                customHistoricActivityService.getByProcessInstanceIdAndYear(processInstanceId, year);
            list.sort((o1, o2) -> {
                try {
                    if (o1.getEndTime() == null || o2.getEndTime() == null) {
                        return 0;
                    }
                    long endTime1 = o1.getEndTime().getTime();
                    long endTime2 = o2.getEndTime().getTime();
                    return Long.compare(endTime1, endTime2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return -1;
            });
            int num = 0;
            for (HistoricActivityInstance his : list) {
                String id = his.getId();
                String taskId = his.getTaskId();
                String type = his.getActivityType();
                if (type.contains(SysVariables.STARTEVENT) || type.contains("Flow")
                    ) {
                    continue;
                }
                if (type.contains(SysVariables.ENDEVENT)) {
                    num += 1;
                    String completer = (String) listMap.get(listMap.size() - 1).get("title");
                    if (completer.contains("主办")) {
                        completer = completer.substring(0, completer.length() - 4);
                    }
                    Map<String, Object> map = new HashMap<>(16);
                    map.put("id", id);
                    map.put("name", "办结");
                    map.put("title", completer);
                    map.put("parentId", parentId);
                    map.put("className", "specialColor");
                    map.put("num", num);
                    map.put("endTime", his.getEndTime() != null ? his.getEndTime().getTime() : 0);
                    listMap.add(map);
                    continue;
                }
                if (type.contains(SysVariables.GATEWAY)) {
                    num += 1;
                    continue;
                }
                String userId = his.getAssignee();
                Person person = personManager.get(tenantId, userId).getData();
                if ("".equals(activityId) || activityId.equals(his.getActivityId())) {
                    Map<String, Object> map = new HashMap<>(16);
                    map.put("id", taskId);
                    map.put("name", his.getActivityName());
                    map.put("title", person != null ? person.getName() : "该用户不存在");
                    HistoricVariableInstance historicVariableInstance = customHistoricVariableService
                        .getByTaskIdAndVariableName(taskId, SysVariables.PARALLELSPONSOR, year);
                    if (historicVariableInstance != null) {
                        map.put("title", person != null ? person.getName() + "(主办)" : "该用户不存在");
                    }
                    map.put("parentId", parentId);
                    map.put("className", his.getEndTime() != null ? "serverColor" : "specialColor");
                    map.put("endTime", his.getEndTime() != null ? his.getEndTime().getTime() : 0);
                    map.put("num", num);
                    listMap.add(map);
                    activityId = his.getActivityId();
                    parentId = taskId;
                } else {
                    num += 1;
                    activityId = his.getActivityId();
                    Map<String, Object> map = new HashMap<>(16);
                    map.put("id", taskId);
                    map.put("name", his.getActivityName());
                    map.put("title", person != null ? person.getName() : "该用户不存在");
                    HistoricVariableInstance historicVariableInstance = customHistoricVariableService
                        .getByTaskIdAndVariableName(taskId, SysVariables.PARALLELSPONSOR, year);
                    if (historicVariableInstance != null) {
                        map.put("title", person != null ? person.getName() + "(主办)" : "该用户不存在");
                    }
                    map.put("parentId", parentId);
                    map.put("className", his.getEndTime() != null ? "serverColor" : "specialColor");
                    map.put("endTime", his.getEndTime() != null ? his.getEndTime().getTime() : 0);
                    map.put("num", num);
                    listMap.add(map);
                }

                List<ProcessTrackModel> ptList = processTrackManager.findByTaskIdAsc(tenantId, userId, taskId);
                String parentId0 = taskId;
                for (int j = 0; j < ptList.size(); j++) {
                    num += 1;
                    ProcessTrackModel pt = ptList.get(j);
                    if (j != 0) {
                        parentId0 = pt.getId();
                    }
                    Map<String, Object> map = new HashMap<>(16);
                    map.put("id", pt.getId());
                    map.put("name", pt.getTaskDefName());
                    map.put("title", pt.getSenderName());
                    map.put("parentId", parentId0);
                    map.put("className", StringUtils.isNotBlank(pt.getEndTime()) ? "serverColor" : "specialColor");
                    map.put("endTime",
                        StringUtils.isNotBlank(pt.getEndTime()) ? sdf.parse(pt.getEndTime()).getTime() : 0);
                    map.put("num", num);
                    listMap.add(map);
                    if (j == ptList.size() - 1) {
                        parentId = parentId0;
                    }
                }
            }
            int oldnum = 0;
            int newnum = 0;
            for (int i = 0; i < listMap.size(); i++) {
                Map<String, Object> map = listMap.get(i);
                int currnum = (int) map.get("num");
                if (currnum == 0) {
                    parentId = (String) map.get("id");
                    map.put("parentId", "");
                }
                if (currnum != oldnum) {
                    map.put("parentId", parentId);
                    if (newnum == 0) {
                        newnum = currnum;
                    }
                    if (newnum != currnum) {
                        oldnum = newnum;
                        newnum = currnum;
                        parentId = (String) listMap.get(i - 1).get("id");
                        map.put("parentId", parentId);
                    }
                }
            }
            parentId = "0";
            List<Map<String, Object>> childrenMap = new ArrayList<>();
            for (int i = listMap.size() - 1; i >= 0; i--) {
                Map<String, Object> map = listMap.get(i);
                String id = (String) map.get("id");
                if (StringUtils.isNotBlank(parentId) && !parentId.equals(id)) {
                    parentId = (String) map.get("parentId");
                    childrenMap.add(map);
                } else {
                    map.put("children", childrenMap);
                    map.put("collapsed", false);
                    parentId = (String) map.get("parentId");
                    childrenMap = new ArrayList<>();
                    childrenMap.add(map);
                    if ("".equals(parentId)) {
                        resMap = map;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resMap;
    }

    /**
     * 获取模型列表
     *
     * @param tenantId 租户id
     * @return Y9Result<List<Map<String, Object>>>
     */
    @Override
    @RequestMapping(value = "/getModelList", method = RequestMethod.GET, produces = "application/json")
    public Y9Result<List<Map<String, Object>>> getModelList(@RequestParam String tenantId) {
        FlowableTenantInfoHolder.setTenantId(tenantId);
        List<Map<String, Object>> items = new ArrayList<>();
        return Y9Result.success(items, "获取成功");
    }

    /**
     * 获取流程设计模型xml
     *
     * @param tenantId 租户id
     * @param modelId  模型id
     * @return Y9Result<Map<String, Object>>
     */
    @Override
    @RequestMapping(value = "/getModelXml")
    public Y9Result<Map<String, Object>> getModelXml(@RequestParam String tenantId, @RequestParam String modelId) {
        FlowableTenantInfoHolder.setTenantId(tenantId);
        Map<String, Object> map = new HashMap<>();
        return Y9Result.success(map, "获取成功");
    }

    /**
     * 导入流程模型
     *
     * @param tenantId 租户id
     * @param userId   用户id
     * @param file     模型文件
     * @return Map<String, Object>
     */
    @Override
    @RequestMapping(value = "/import")
    public Map<String, Object> importProcessModel(@RequestParam String tenantId, @RequestParam String userId,
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
     * @param userId   用户id
     * @param modelId  模型id
     * @param file     模型文件
     * @return Y9Result<String>
     */
    @Override
    @RequestMapping(value = "/saveModelXml")
    public Y9Result<String> saveModelXml(@RequestParam String tenantId, @RequestParam String userId,
        @RequestParam String modelId, @RequestParam MultipartFile file) {
        return Y9Result.failure("保存失败");
    }
}
