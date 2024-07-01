package net.risesoft.controller;

import java.text.SimpleDateFormat;
import java.util.Date;

import jakarta.validation.constraints.NotBlank;

import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.api.itemadmin.ProcessParamApi;
import net.risesoft.api.itemadmin.position.OfficeDoneInfo4PositionApi;
import net.risesoft.api.itemadmin.position.OfficeFollow4PositionApi;
import net.risesoft.api.platform.org.OrgUnitApi;
import net.risesoft.api.processadmin.HistoricProcessApi;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.model.itemadmin.OfficeDoneInfoModel;
import net.risesoft.model.itemadmin.OfficeFollowModel;
import net.risesoft.model.itemadmin.ProcessParamModel;
import net.risesoft.model.platform.OrgUnit;
import net.risesoft.model.platform.Position;
import net.risesoft.model.processadmin.HistoricProcessInstanceModel;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;
import net.risesoft.y9.Y9LoginUserHolder;

/**
 * 我的关注
 *
 * @author zhangchongjie
 * @date 2024/06/05
 */
@Validated
@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/vue/officeFollow")
public class OfficeFollowRestController {

    private final OrgUnitApi orgUnitApi;

    private final OfficeFollow4PositionApi officeFollow4PositionApi;

    private final ProcessParamApi processParamApi;

    private final HistoricProcessApi historicProcessApi;

    private final OfficeDoneInfo4PositionApi officeDoneInfo4PositionApi;

    /**
     * 取消关注
     *
     * @param processInstanceIds 流程实例ids，逗号隔开
     * @return Y9Result<String>
     */
    @RequestMapping(value = "/delOfficeFollow", method = RequestMethod.POST, produces = "application/json")
    public Y9Result<String> delOfficeFollow(@RequestParam String processInstanceIds) {
        try {
            Y9Result<Object> y9Result;
            String tenantId = Y9LoginUserHolder.getTenantId();
            y9Result = officeFollow4PositionApi.delOfficeFollow(tenantId, Y9LoginUserHolder.getPositionId(),
                processInstanceIds);
            if (y9Result.isSuccess()) {
                return Y9Result.successMsg("取消关注成功");
            }
        } catch (Exception e) {
            LOGGER.error("取消关注失败", e);
        }
        return Y9Result.failure("取消关注失败");
    }

    /**
     * 获取我的关注列表
     *
     * @param page 页码
     * @param rows 条数
     * @param searchName 搜索词
     * @return Y9Page<OfficeFollowModel>
     */
    @RequestMapping(value = "/followList", method = RequestMethod.GET, produces = "application/json")
    public Y9Page<OfficeFollowModel> followList(@RequestParam Integer page, @RequestParam Integer rows,
        @RequestParam(required = false) String searchName) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        return officeFollow4PositionApi.getOfficeFollowList(tenantId, Y9LoginUserHolder.getPositionId(), searchName,
            page, rows);
    }

    /**
     * 获取左侧关注菜单数字
     *
     * @return Y9Result<Integer>
     */
    @RequestMapping(value = "/getFollowCount", method = RequestMethod.GET, produces = "application/json")
    public Y9Result<Integer> getFollowCount() {
        String tenantId = Y9LoginUserHolder.getTenantId();
        int followCount =
            officeFollow4PositionApi.getFollowCount(tenantId, Y9LoginUserHolder.getPositionId()).getData();
        return Y9Result.success(followCount, "获取成功");
    }

    /**
     * 保存关注
     *
     * @param processInstanceId 流程实例id
     * @return Y9Result<String>
     */
    @RequestMapping(value = "/saveOfficeFollow", method = RequestMethod.POST, produces = "application/json")
    public Y9Result<String> saveOfficeFollow(@RequestParam @NotBlank String processInstanceId) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Position position = Y9LoginUserHolder.getPosition();
            String positionId = position.getId(), tenantId = Y9LoginUserHolder.getTenantId();
            OfficeFollowModel officeFollow = new OfficeFollowModel();
            if (StringUtils.isNotBlank(processInstanceId)) {
                ProcessParamModel processParamModel =
                    processParamApi.findByProcessInstanceId(tenantId, processInstanceId).getData();
                officeFollow.setGuid(Y9IdGenerator.genId(IdType.SNOWFLAKE));
                OrgUnit orgUnit = orgUnitApi.getBureau(tenantId, position.getParentId()).getData();
                officeFollow.setBureauId(orgUnit != null ? orgUnit.getId() : "");
                officeFollow.setBureauName(orgUnit != null ? orgUnit.getName() : "");
                officeFollow.setCreateTime(sdf.format(new Date()));
                officeFollow.setDocumentTitle(processParamModel.getTitle());
                officeFollow.setFileType(processParamModel.getItemName());
                officeFollow.setHandleTerm("");
                officeFollow.setItemId(processParamModel.getItemId());
                officeFollow.setJinjichengdu(processParamModel.getCustomLevel());
                officeFollow.setNumbers(processParamModel.getCustomNumber());
                officeFollow.setProcessInstanceId(processInstanceId);
                officeFollow.setProcessSerialNumber(processParamModel.getProcessSerialNumber());
                officeFollow.setSendDept("");
                officeFollow.setSystemName(processParamModel.getSystemName());
                HistoricProcessInstanceModel historicProcessInstanceModel =
                    historicProcessApi.getById(tenantId, processInstanceId).getData();
                if (historicProcessInstanceModel == null) {
                    OfficeDoneInfoModel officeDoneInfoModel =
                        officeDoneInfo4PositionApi.findByProcessInstanceId(tenantId, processInstanceId).getData();
                    officeFollow.setStartTime(officeDoneInfoModel != null ? officeDoneInfoModel.getStartTime() : "");
                } else {
                    officeFollow.setStartTime(sdf.format(historicProcessInstanceModel.getStartTime()));
                }
                officeFollow.setUserId(positionId);
                officeFollow.setUserName(position.getName());
                Y9Result<Object> y9Result = officeFollow4PositionApi.saveOfficeFollow(tenantId, officeFollow);
                if (y9Result.isSuccess()) {
                    return Y9Result.successMsg("关注成功");
                }
            }
        } catch (Exception e) {
            LOGGER.error("关注失败", e);
        }
        return Y9Result.failure("关注失败");
    }

}
