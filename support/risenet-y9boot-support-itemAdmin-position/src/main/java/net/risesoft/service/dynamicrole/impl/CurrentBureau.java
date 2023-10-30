package net.risesoft.service.dynamicrole.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.risesoft.api.org.DepartmentApi;
import net.risesoft.api.org.OrgUnitApi;
import net.risesoft.api.org.PositionApi;
import net.risesoft.entity.ProcessParam;
import net.risesoft.model.OrgUnit;
import net.risesoft.model.Position;
import net.risesoft.service.ProcessParamService;
import net.risesoft.service.dynamicrole.AbstractDynamicRoleMember;
import net.risesoft.util.SysVariables;
import net.risesoft.y9.Y9LoginUserHolder;

/**
 * 当前人员所在委办局和当前历程参与过的所有的委办局
 * 
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/20
 */
@Service
public class CurrentBureau extends AbstractDynamicRoleMember {

    @Autowired
    private PositionApi positionManager;

    @Autowired
    private DepartmentApi departmentManager;

    @Autowired
    private OrgUnitApi orgUnitManager;

    @Autowired
    private ProcessParamService processParamService;

    @Override
    public List<OrgUnit> getOrgUnitList() {
        String tenantId = Y9LoginUserHolder.getTenantId();
        String positionId = Y9LoginUserHolder.getPositionId();
        List<OrgUnit> orgUnitList = new ArrayList<OrgUnit>();
        Position position = positionManager.getPosition(tenantId, positionId).getData();
        OrgUnit orgUnit = departmentManager.getBureau(tenantId, position.getParentId()).getData();
        orgUnitList.add(orgUnit);
        return orgUnitList;
    }

    @Override
    public List<OrgUnit> getOrgUnitList(String processInstanceId) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        String positionId = Y9LoginUserHolder.getPositionId();
        List<OrgUnit> orgUnitList = new ArrayList<OrgUnit>();
        Position position = positionManager.getPosition(tenantId, positionId).getData();
        OrgUnit orgUnit = departmentManager.getBureau(tenantId, position.getParentId()).getData();
        if (StringUtils.isNotBlank(processInstanceId)) {
            ProcessParam processParam = processParamService.findByProcessInstanceId(processInstanceId);
            if (null != processParam && StringUtils.isNotBlank(processParam.getBureauIds())) {
                String[] bureauIds = processParam.getBureauIds().split(SysVariables.SEMICOLON);
                for (String bureauId : bureauIds) {
                    if (!bureauId.equals(orgUnit.getId())) {
                        orgUnitList.add(orgUnitManager.getOrgUnit(tenantId, bureauId).getData());
                    }
                }
            }
        }
        orgUnitList.add(orgUnit);
        return orgUnitList;
    }
}