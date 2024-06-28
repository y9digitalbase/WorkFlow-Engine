package net.risesoft.api.itemadmin.position;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import net.risesoft.model.itemadmin.LinkInfoModel;
import net.risesoft.pojo.Y9Result;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/19
 */
@Validated
public interface ItemLink4PositionApi {

    /**
     * 获取有权限的事项绑定链接
     *
     * @param tenantId 租户id
     * @param positionId 岗位id
     * @param itemId 事项id
     * @return Y9Result<List<LinkInfoModel>>
     */
    @GetMapping("/getItemLinkList")
    Y9Result<List<LinkInfoModel>> getItemLinkList(@RequestParam("tenantId") @NotBlank String tenantId,
        @RequestParam("positionId") @NotBlank String positionId, @RequestParam("itemId") @NotBlank String itemId);

}
