package net.risesoft.api.processadmin;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import net.risesoft.model.processadmin.FlowableBpmnModel;
import net.risesoft.model.processadmin.Y9BpmnModel;
import net.risesoft.model.processadmin.Y9FlowChartModel;
import net.risesoft.pojo.Y9Result;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/19
 */
public interface BpmnModelApi {

    /**
     * 删除模型
     *
     * @param tenantId 租户id
     * @param modelId 模型id
     * @return {@code Y9Result<Boolean>}
     */
    @PostMapping(value = "/deleteModel")
    Y9Result<Object> deleteModel(@RequestParam("tenantId") String tenantId, @RequestParam("modelId") String modelId);

    /**
     * 根据Model部署流程
     *
     * @param tenantId 租户id
     * @param modelId 模型id
     * @return Y9Result<Object>
     */
    @PostMapping(value = "/deployModel")
    Y9Result<Object> deployModel(@RequestParam("tenantId") String tenantId, @RequestParam("modelId") String modelId);

    /**
     * 生成流程图
     *
     * @param tenantId 租户id
     * @param processInstanceId 流程实例id
     * @return Y9Result<String>
     */
    @PostMapping("/genProcessDiagram")
    Y9Result<String> genProcessDiagram(@RequestParam("tenantId") String tenantId,
        @RequestParam("processInstanceId") String processInstanceId);

    /**
     * 获取流程图模型
     *
     * @param tenantId 租户id
     * @param processInstanceId 流程实例id
     * @return Y9Result<Y9BpmnModel>
     */
    @GetMapping("/getBpmnModel")
    Y9Result<Y9BpmnModel> getBpmnModel(@RequestParam("tenantId") String tenantId,
        @RequestParam("processInstanceId") String processInstanceId);

    /**
     * 获取流程图数据
     *
     * @param tenantId 租户id
     * @param processInstanceId 流程实例id
     * @return Y9Result<Y9FlowChartModel>
     */
    @GetMapping("/getFlowChart")
    Y9Result<Y9FlowChartModel> getFlowChart(@RequestParam("tenantId") String tenantId,
        @RequestParam("processInstanceId") String processInstanceId);

    /**
     * 获取模型列表
     *
     * @param tenantId 租户id
     * @return Y9Result<List<FlowableBpmnModel>>
     */
    @GetMapping(value = "/getModelList")
    Y9Result<List<FlowableBpmnModel>> getModelList(@RequestParam("tenantId") String tenantId);

    /**
     * 获取流程设计模型xml
     *
     * @param tenantId 租户id
     * @param modelId 模型id
     * @return Y9Result<FlowableBpmnModel>
     */
    @GetMapping(value = "/getModelXml")
    Y9Result<FlowableBpmnModel> getModelXml(@RequestParam("tenantId") String tenantId,
        @RequestParam("modelId") String modelId);

    /**
     * 导入流程模板
     *
     * @param tenantId 租户id
     * @param userId 用户id
     * @param file 导入的xml文件
     * @return Y9Result<Object>
     */
    @PostMapping(value = "/import")
    Y9Result<Object> importProcessModel(@RequestParam("tenantId") String tenantId,
        @RequestParam("userId") String userId, @RequestParam("file") MultipartFile file);

    /**
     * 保存模型xml
     *
     * @param tenantId 租户id
     * @param userId 用户id
     * @param modelId 模板id
     * @param file 模型文件
     * @return Y9Result<Object>
     */
    @PostMapping(value = "/saveModelXml")
    Y9Result<Object> saveModelXml(@RequestParam("tenantId") String tenantId, @RequestParam("userId") String userId,
        @RequestParam("modelId") String modelId, @RequestParam("file") MultipartFile file);

}
