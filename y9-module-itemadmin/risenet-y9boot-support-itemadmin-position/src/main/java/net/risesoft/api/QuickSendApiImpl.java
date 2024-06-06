package net.risesoft.api;

import jakarta.validation.constraints.NotBlank;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import net.risesoft.api.itemadmin.QuickSendApi;
import net.risesoft.service.QuickSendService;
import net.risesoft.y9.Y9LoginUserHolder;

/**
 * 快速发送设置接口
 * @author zhangchongjie
 * @date 2023/09/07
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/services/rest/quickSend", produces = MediaType.APPLICATION_JSON_VALUE)
public class QuickSendApiImpl implements QuickSendApi {

    private final QuickSendService quickSendService;

    /**
     *  获取快速发送设置
     * @param tenantId 租户id
     * @param positionId 岗位id
     * @param itemId 事项id
     * @param taskKey 任务key
     * @return String
     */
    @Override
    public String getAssignee(@NotBlank String tenantId, @NotBlank String positionId, @NotBlank String itemId,
        @NotBlank String taskKey) {
        Y9LoginUserHolder.setTenantId(tenantId);
        Y9LoginUserHolder.setPositionId(positionId);
        return quickSendService.getAssignee(itemId, taskKey);
    }

    /**
     *  保存快速发送设置
     * @param tenantId 租户id
     * @param positionId 岗位id
     * @param itemId 事项id
     * @param taskKey 任务key
     * @param assignee 快速发送人
     */
    @Override
    public void saveOrUpdate(@NotBlank String tenantId, @NotBlank String positionId, @NotBlank String itemId,
        @NotBlank String taskKey, String assignee) {
        Y9LoginUserHolder.setTenantId(tenantId);
        Y9LoginUserHolder.setPositionId(positionId);
        quickSendService.saveOrUpdate(itemId, taskKey, assignee);
    }

}
