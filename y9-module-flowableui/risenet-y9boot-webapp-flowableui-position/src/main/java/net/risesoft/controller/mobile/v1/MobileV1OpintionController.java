package net.risesoft.controller.mobile.v1;

import lombok.RequiredArgsConstructor;
import net.risesoft.api.itemadmin.CommonSentencesApi;
import net.risesoft.api.itemadmin.position.Opinion4PositionApi;
import net.risesoft.model.itemadmin.OpinionModel;
import net.risesoft.pojo.Y9Result;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.json.Y9JsonUtil;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 意见相关接口
 *
 * @author zhangchongjie
 * @date 2024/01/17
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/mobile/v1/opintion")
public class MobileV1OpintionController {

    private final CommonSentencesApi commonSentencesApi;

    private final Opinion4PositionApi opinion4PositionApi;

    /**
     * 保存意见
     *
     * @param tenantId     租户id
     * @param userId       人员id
     * @param positionId   岗位id
     * @param formJsonData 意见json内容
     */
    @RequestMapping(value = "/comment/save")
    public Y9Result<String> addComment(@RequestHeader("auth-tenantId") String tenantId,
                                       @RequestHeader("auth-userId") String userId, @RequestHeader("auth-positionId") String positionId,
                                       @RequestParam String formJsonData) {
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            OpinionModel opinionModel = Y9JsonUtil.readValue(formJsonData, OpinionModel.class);
            opinionModel.setTenantId(tenantId + ":mobile");
            opinion4PositionApi.saveOrUpdate(tenantId, userId, positionId, opinionModel);
            return Y9Result.successMsg("保存成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Y9Result.failure("保存失败");
    }

    /**
     * 是否已填写意见
     *
     * @param tenantId            租户id
     * @param userId              人员id
     * @param positionId          岗位id
     * @param taskId              任务id
     * @param processSerialNumber 流程编号
     */
    @RequestMapping(value = "/comment/checkSignOpinion")
    public Y9Result<Boolean> checkSignOpinion(@RequestHeader("auth-tenantId") String tenantId,
                                              @RequestHeader("auth-userId") String userId, @RequestHeader("auth-positionId") String positionId,
                                              @RequestParam(required = false) String taskId, @RequestParam(required = false) String processSerialNumber) {
        try {
            boolean b = opinion4PositionApi.checkSignOpinion(tenantId, userId, processSerialNumber, taskId);
            return Y9Result.success(b, "获取成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Y9Result.failure("获取失败");
    }

    /**
     * 删除意见
     *
     * @param tenantId   租户id
     * @param userId     人员id
     * @param positionId 岗位id
     * @param id         意见id
     */
    @RequestMapping(value = "/comment/delete")
    public Y9Result<String> deleteComment(@RequestHeader("auth-tenantId") String tenantId,
                                          @RequestHeader("auth-userId") String userId, @RequestHeader("auth-positionId") String positionId,
                                          @RequestParam String id) {
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            opinion4PositionApi.delete(tenantId, id);
            return Y9Result.successMsg("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Y9Result.failure("删除失败");
    }

    /**
     * 获取个人常用语
     *
     * @param tenantId   租户id
     * @param userId     人员id
     * @param positionId 岗位id
     */
    @RequestMapping(value = "/personalSetup")
    public Y9Result<List<Map<String, Object>>> personalSetup(@RequestHeader("auth-tenantId") String tenantId,
                                                             @RequestHeader("auth-userId") String userId, @RequestHeader("auth-positionId") String positionId) {
        Y9LoginUserHolder.setTenantId(tenantId);
        List<Map<String, Object>> listMap = commonSentencesApi.listSentencesService(tenantId, userId);
        return Y9Result.success(listMap, "获取成功");
    }

    /**
     * 获取意见
     *
     * @param tenantId            租户id
     * @param userId              人员id
     * @param positionId          岗位id
     * @param processSerialNumber 流程编号
     * @param taskId              任务id
     * @param itembox             办件状态，待办：todo,在办：doing,办结：done
     * @param opinionFrameMark    意见框标识
     * @param itemId              事项id
     * @param taskDefinitionKey   任务key
     * @param activitiUser        当前任务受让人
     */
    @RequestMapping(value = "/personCommentList")
    public Y9Result<List<Map<String, Object>>> personCommentList(@RequestHeader("auth-tenantId") String tenantId,
                                                                 @RequestHeader("auth-userId") String userId, @RequestHeader("auth-positionId") String positionId,
                                                                 @RequestParam String processSerialNumber, @RequestParam String taskId, @RequestParam String itembox,
                                                                 @RequestParam String opinionFrameMark, @RequestParam String itemId, @RequestParam String taskDefinitionKey,
                                                                 @RequestParam String activitiUser) {
        List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
        Y9LoginUserHolder.setTenantId(tenantId);
        listMap = opinion4PositionApi.personCommentList(tenantId, userId, processSerialNumber, taskId, itembox,
                opinionFrameMark, itemId, taskDefinitionKey, activitiUser);
        return Y9Result.success(listMap, "获取成功");
    }

    /**
     * 删除常用语
     *
     * @param tenantId   租户id
     * @param userId     人员id
     * @param positionId 岗位id
     * @param id         常用语id
     */
    @RequestMapping(value = "/removeCommonSentences")
    public Y9Result<String> removeCommonSentences(@RequestHeader("auth-tenantId") String tenantId,
                                                  @RequestHeader("auth-userId") String userId, @RequestHeader("auth-positionId") String positionId,
                                                  @RequestParam String id) {
        Y9LoginUserHolder.setTenantId(tenantId);
        try {
            commonSentencesApi.delete(tenantId, id);
            return Y9Result.successMsg("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Y9Result.failure("删除失败");
    }

    /**
     * 保存常用语
     *
     * @param tenantId   租户id
     * @param userId     人员id
     * @param positionId 岗位id
     * @param content    内容
     * @param id         常用语id,新增id为空
     */
    @RequestMapping(value = "/saveCommonSentences")
    public Y9Result<String> saveCommonSentences(@RequestHeader("auth-tenantId") String tenantId,
                                                @RequestHeader("auth-userId") String userId, @RequestHeader("auth-positionId") String positionId,
                                                @RequestParam String content, @RequestParam String id) {
        Y9LoginUserHolder.setTenantId(tenantId);
        try {
            commonSentencesApi.save(tenantId, userId, id, content);
            return Y9Result.successMsg("保存成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Y9Result.failure("保存失败");
    }

    /**
     * 获取个人常用语
     *
     * @param tenantId   租户id
     * @param userId     人员id
     * @param positionId 岗位id
     */
    @RequestMapping(value = "/systemSetup")
    public Y9Result<List<Map<String, Object>>> systemSetup(@RequestHeader("auth-tenantId") String tenantId,
                                                           @RequestHeader("auth-userId") String userId, @RequestHeader("auth-positionId") String positionId) {
        Y9LoginUserHolder.setTenantId(tenantId);
        List<Map<String, Object>> listMap = commonSentencesApi.listSentencesService(tenantId, userId);
        return Y9Result.success(listMap, "获取成功");
    }
}
