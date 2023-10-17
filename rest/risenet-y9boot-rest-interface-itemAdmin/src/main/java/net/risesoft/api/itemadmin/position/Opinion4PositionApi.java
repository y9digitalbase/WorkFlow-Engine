package net.risesoft.api.itemadmin.position;

import java.util.List;
import java.util.Map;

import net.risesoft.model.itemadmin.OpinionHistoryModel;
import net.risesoft.model.itemadmin.OpinionModel;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/19
 */
public interface Opinion4PositionApi {

    /**
     * 检查当前taskId任务节点是否已经签写意见
     *
     * @param tenantId 租户id
     * @param userId 人员id
     * @param processSerialNumber 流程编号
     * @param taskId 任务id
     * @return Boolean
     */
    Boolean checkSignOpinion(String tenantId, String userId, String processSerialNumber, String taskId);

    /**
     * 获取意见框历史记录数量
     *
     * @param tenantId 租户id
     * @param processSerialNumber 流程编号
     * @param opinionFrameMark 意见框id
     * @return int
     */
    int countOpinionHistory(String tenantId, String processSerialNumber, String opinionFrameMark);

    /**
     * 删除意见
     *
     * @param tenantId 租户id
     * @param id 唯一标识
     * @throws Exception Exception
     */
    void delete(String tenantId, String id) throws Exception;

    /**
     * 获取事项绑定的意见框列表
     *
     * @param tenantId 租户id
     * @param itemId 事项id
     * @param processDefinitionId 流程定义Id
     * @return List&lt;String&gt;
     */
    List<String> getBindOpinionFrame(String tenantId, String itemId, String processDefinitionId);

    /**
     * 根据id获取意见
     *
     * @param tenantId 租户id
     * @param id 唯一标识
     * @return OpinionModel
     */
    OpinionModel getById(String tenantId, String id);

    /**
     * 获取意见框历史记录
     *
     * @param tenantId 租户id
     * @param processSerialNumber 流程编号
     * @param opinionFrameMark 意见框Id
     * @return List&lt;OpinionHistoryModel&gt;
     */
    List<OpinionHistoryModel> opinionHistoryList(String tenantId, String processSerialNumber, String opinionFrameMark);

    /**
     * 获取个人意见列表
     *
     * @param tenantId 租户id
     * @param userId 人员id
     * @param processSerialNumber 流程编号
     * @param taskId 任务id
     * @param itembox 办件状态，todo（待办），doing（在办），done（办结）
     * @param opinionFrameMark 意见框Id
     * @param itemId 事项id
     * @param taskDefinitionKey 任务定义key
     * @param activitiUser activitiUser
     * @return List&lt;Map&lt;String, Object&gt;&gt;
     */
    List<Map<String, Object>> personCommentList(String tenantId, String userId, String processSerialNumber, String taskId, String itembox, String opinionFrameMark, String itemId, String taskDefinitionKey, String activitiUser);

    /**
     * 保存意见
     *
     * @param tenantId 租户id
     * @param opinion OpinionModel
     * @throws Exception Exception
     */
    void save(String tenantId, OpinionModel opinion) throws Exception;

    /**
     *
     * Description: 保存或更新意见
     *
     * @param tenantId 租户id
     * @param userId 人员id
     * @param positionId 岗位id
     * @param opinion 意见实体
     * @return OpinionModel
     * @throws Exception Exception
     */
    OpinionModel saveOrUpdate(String tenantId, String userId, String positionId, OpinionModel opinion) throws Exception;
}
