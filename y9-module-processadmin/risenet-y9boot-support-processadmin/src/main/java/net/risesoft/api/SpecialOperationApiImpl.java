package net.risesoft.api;

import java.util.List;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.api.platform.org.PersonApi;
import net.risesoft.api.platform.org.PositionApi;
import net.risesoft.api.processadmin.SpecialOperationApi;
import net.risesoft.model.platform.Person;
import net.risesoft.model.platform.Position;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.FlowableTenantInfoHolder;
import net.risesoft.service.OperationService;
import net.risesoft.y9.Y9LoginUserHolder;

/**
 * 退回，收回，重定向，特殊办结接口
 *
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/30
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/services/rest/specialOperation")
public class SpecialOperationApiImpl implements SpecialOperationApi {

    private final OperationService operationService;

    private final PersonApi personManager;

    private final PositionApi positionManager;

    /**
     * 重定向
     *
     * @param tenantId 租户id
     * @param userId 人员id
     * @param taskId 任务id
     * @param targetTaskDefineKey 任务key
     * @param users 人员id集合
     * @param reason 重定向原因
     * @param sponsorGuid 主办人id
     * @return Y9Result<Object>
     */
    @Override
    public Y9Result<Object> reposition(@RequestParam String tenantId, @RequestParam String userId,
        @RequestParam String taskId, @RequestParam String targetTaskDefineKey, @RequestBody List<String> users,
        String reason, String sponsorGuid) {
        FlowableTenantInfoHolder.setTenantId(tenantId);
        Person person = personManager.get(tenantId, userId).getData();
        Y9LoginUserHolder.setPerson(person);
        Y9LoginUserHolder.setTenantId(tenantId);
        operationService.reposition(taskId, targetTaskDefineKey, users, reason, sponsorGuid);
        return Y9Result.success();
    }

    /**
     * 重定向(岗位)
     *
     * @param tenantId 租户id
     * @param positionId 岗位id
     * @param taskId 任务id
     * @param repositionToTaskId 任务key
     * @param userChoice 岗位id集合
     * @param reason 重定向原因
     * @param sponsorGuid 主办人id
     * @return Y9Result<Object>
     */
    @Override
    public Y9Result<Object> reposition4Position(@RequestParam String tenantId, @RequestParam String positionId,
        @RequestParam String taskId, @RequestParam String repositionToTaskId,
        @RequestParam("userChoice") List<String> userChoice, @RequestParam String reason,
        @RequestParam String sponsorGuid) {
        FlowableTenantInfoHolder.setTenantId(tenantId);
        Y9LoginUserHolder.setTenantId(tenantId);
        Position position = positionManager.get(tenantId, positionId).getData();
        Y9LoginUserHolder.setPosition(position);
        operationService.reposition4Position(taskId, repositionToTaskId, userChoice, reason, sponsorGuid);
        return Y9Result.success();
    }

    /**
     * 退回办件
     *
     * @param tenantId 租户id
     * @param userId 人员id
     * @param taskId 任务id
     * @param reason 退回的原因
     * @return Y9Result<Object>
     */
    @Override
    public Y9Result<Object> rollBack(@RequestParam String tenantId, @RequestParam String userId, String taskId,
        @RequestParam String reason) {
        FlowableTenantInfoHolder.setTenantId(tenantId);
        Person person = personManager.get(tenantId, userId).getData();
        Y9LoginUserHolder.setPerson(person);
        Y9LoginUserHolder.setTenantId(tenantId);
        operationService.rollBack(taskId, reason);
        return Y9Result.success();
    }

    /**
     * 退回（岗位）
     *
     * @param tenantId 租户id
     * @param positionId 岗位id
     * @param taskId 任务id
     * @param reason 退回的原因
     * @return Y9Result<Object>
     */
    @Override
    public Y9Result<Object> rollBack4Position(@RequestParam String tenantId, @RequestParam String positionId,
        @RequestParam String taskId, @RequestParam String reason) {
        FlowableTenantInfoHolder.setTenantId(tenantId);
        Y9LoginUserHolder.setTenantId(tenantId);
        Position position = positionManager.get(tenantId, positionId).getData();
        Y9LoginUserHolder.setPosition(position);
        operationService.rollBack4Position(taskId, reason);
        return Y9Result.success();
    }

    /**
     * 发回给发送人/岗位
     *
     * @param tenantId 租户id
     * @param positionId 岗位id
     * @param taskId 任务id
     * @return Y9Result<Object>
     */
    @Override
    public Y9Result<Object> rollbackToSender4Position(@RequestParam String tenantId, @RequestParam String positionId,
        @RequestParam String taskId) {
        FlowableTenantInfoHolder.setTenantId(tenantId);
        Y9LoginUserHolder.setTenantId(tenantId);
        Position position = positionManager.get(tenantId, positionId).getData();
        Y9LoginUserHolder.setPosition(position);
        operationService.rollbackToSender4Position(taskId);
        return Y9Result.success();
    }

    /**
     * 返回拟稿人/岗位
     *
     * @param tenantId 租户id
     * @param positionId 岗位id
     * @param taskId 任务id
     * @param reason 原因
     * @return Y9Result<Object>
     */
    @Override
    public Y9Result<Object> rollbackToStartor4Position(@RequestParam String tenantId, @RequestParam String positionId,
        @RequestParam String taskId, @RequestParam String reason) {
        FlowableTenantInfoHolder.setTenantId(tenantId);
        Y9LoginUserHolder.setTenantId(tenantId);
        Position position = positionManager.get(tenantId, positionId).getData();
        Y9LoginUserHolder.setPosition(position);
        operationService.rollbackToStartor4Position(taskId, reason);
        return Y9Result.success();
    }

    /**
     * 特殊办结/岗位
     *
     * @param tenantId 租户id
     * @param positionId 岗位id
     * @param taskId 任务id
     * @param reason 原因
     * @return Y9Result<Object>
     */
    @Override
    public Y9Result<Object> specialComplete4Position(@RequestParam String tenantId, @RequestParam String positionId,
        @RequestParam String taskId, @RequestParam String reason) {
        FlowableTenantInfoHolder.setTenantId(tenantId);
        Y9LoginUserHolder.setTenantId(tenantId);
        Position position = positionManager.get(tenantId, positionId).getData();
        Y9LoginUserHolder.setPosition(position);
        operationService.specialComplete4Position(taskId, reason);
        return Y9Result.success();
    }

    /**
     * 收回办件
     *
     * @param tenantId 租户id
     * @param userId 人员id
     * @param taskId 任务id
     * @param reason 收回的原因
     * @return Y9Result<Object>
     */
    @Override
    public Y9Result<Object> takeBack(@RequestParam String tenantId, @RequestParam String userId,
        @RequestParam String taskId, @RequestParam String reason) {
        FlowableTenantInfoHolder.setTenantId(tenantId);
        Person person = personManager.get(tenantId, userId).getData();
        Y9LoginUserHolder.setPerson(person);
        Y9LoginUserHolder.setTenantId(tenantId);
        operationService.takeBack(taskId, reason);
        return Y9Result.success();
    }

    /**
     * 收回(岗位)
     *
     * @param tenantId 租户id
     * @param positionId 岗位id
     * @param taskId 任务id
     * @param reason 收回的原因
     * @return Y9Result<Object>
     */
    @Override
    public Y9Result<Object> takeBack4Position(@RequestParam String tenantId, @RequestParam String positionId,
        @RequestParam String taskId, @RequestParam String reason) {
        FlowableTenantInfoHolder.setTenantId(tenantId);
        Y9LoginUserHolder.setTenantId(tenantId);
        Position position = positionManager.get(tenantId, positionId).getData();
        Y9LoginUserHolder.setPosition(position);
        operationService.takeBack4Position(taskId, reason);
        return Y9Result.success();
    }
}
