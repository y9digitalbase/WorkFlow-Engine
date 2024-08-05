package net.risesoft.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import net.risesoft.api.platform.org.OrgUnitApi;
import net.risesoft.api.platform.permission.RoleApi;
import net.risesoft.api.platform.resource.AppApi;
import net.risesoft.api.platform.resource.SystemApi;
import net.risesoft.controller.vo.NodeTreeVO;
import net.risesoft.enums.platform.OrgTreeTypeEnum;
import net.risesoft.enums.platform.OrgTypeEnum;
import net.risesoft.enums.platform.RoleTypeEnum;
import net.risesoft.model.platform.App;
import net.risesoft.model.platform.OrgUnit;
import net.risesoft.model.platform.Role;
import net.risesoft.model.platform.System;
import net.risesoft.pojo.Y9Result;
import net.risesoft.y9.Y9Context;
import net.risesoft.y9.Y9LoginUserHolder;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/20
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/vue/role", produces = MediaType.APPLICATION_JSON_VALUE)
public class RoleRestController {

    private final RoleApi roleManager;

    private final OrgUnitApi orgUnitManager;

    private final SystemApi systemEntityManager;

    private final AppApi appApi;

    /**
     * 获取系统角色
     *
     * @param id 节点id
     * @return
     */
    @GetMapping(value = "/findRole")
    public Y9Result<List<NodeTreeVO>> findAll(@RequestParam(required = false) String id) {
        List<NodeTreeVO> listMap = new ArrayList<>();
        if (StringUtils.isBlank(id)) {
            System system = systemEntityManager.getByName(Y9Context.getSystemName()).getData();
            List<App> appList = appApi.listBySystemId(system.getId()).getData();
            for (App app : appList) {
                NodeTreeVO map = new NodeTreeVO();
                map.setId(app.getId());
                map.setName(app.getName());
                map.setParentId(app.getId());
                map.setIsParent(true);
                map.setOrgType("App");
                listMap.add(map);
            }
        } else {
            List<Role> listRole = roleManager.listRoleByParentId(id).getData();
            if (listRole != null) {
                for (Role role : listRole) {
                    NodeTreeVO map = new NodeTreeVO();
                    map.setId(role.getId());
                    map.setName(role.getName());
                    map.setParentId(id);
                    map.setGuidPath(role.getGuidPath());
                    if (RoleTypeEnum.ROLE.equals(role.getType())) {
                        map.setIsParent(false);
                        map.setOrgType("role");
                    } else {
                        List<Role> list = roleManager.listRoleByParentId(role.getId()).getData();
                        boolean isP = false;
                        if (list != null) {
                            isP = !list.isEmpty();
                        }
                        if (isP) {
                            map.setChkDisabled(true);
                        }
                        map.setIsParent(isP);
                        map.setOrgType("folder");
                    }
                    listMap.add(map);
                }
            }
        }
        return Y9Result.success(listMap, "获取成功");
    }

    @GetMapping(value = "/findRoleMember")
    public Y9Result<List<NodeTreeVO>> findRoleMember(@RequestParam String roleId) {
        List<NodeTreeVO> listMap = new ArrayList<>();
        String tenantId = Y9LoginUserHolder.getTenantId();
        List<OrgUnit> list = roleManager.listOrgUnitsById(tenantId, roleId, OrgTypeEnum.POSITION).getData();
        for (OrgUnit orgUnit : list) {
            NodeTreeVO map = new NodeTreeVO();
            map.setId(orgUnit.getId());
            map.setName(orgUnit.getName());
            map.setParentId(roleId);
            map.setGuidPath(orgUnit.getGuidPath());
            map.setOrgType(orgUnit.getOrgType().getValue());
            map.setIsParent(false);
            if (orgUnit.getOrgType().getValue().equals(OrgTypeEnum.DEPARTMENT.getValue())
                || orgUnit.getOrgType().getValue().equals(OrgTypeEnum.GROUP.getValue())) {
                map.setIsParent(true);
            }
            listMap.add(map);
        }
        return Y9Result.success(listMap, "获取成功");
    }

    @GetMapping(value = "/findRoleMember1")
    public Y9Result<List<NodeTreeVO>> findRoleMember1(@RequestParam(required = false) String roleId,
        @RequestParam(required = false) String id, @RequestParam(required = false) OrgTreeTypeEnum treeType) {
        List<NodeTreeVO> listMap = new ArrayList<>();
        String tenantId = Y9LoginUserHolder.getTenantId();
        if (StringUtils.isNotBlank(roleId) && StringUtils.isBlank(id)) {
            List<OrgUnit> list = roleManager.listOrgUnitsById(tenantId, roleId, OrgTypeEnum.PERSON).getData();
            for (OrgUnit orgUnit : list) {
                NodeTreeVO map = new NodeTreeVO();
                map.setId(orgUnit.getId());
                map.setName(orgUnit.getName());
                map.setParentId(roleId);
                map.setGuidPath(orgUnit.getGuidPath());
                map.setOrgType(orgUnit.getOrgType().getValue());
                map.setIsParent(false);
                if (orgUnit.getOrgType().getValue().equals(OrgTypeEnum.DEPARTMENT.getValue())
                    || orgUnit.getOrgType().getValue().equals(OrgTypeEnum.GROUP.getValue())) {
                    map.setIsParent(true);
                }
                listMap.add(map);
            }
        } else {
            List<OrgUnit> list = orgUnitManager.getSubTree(tenantId, id, treeType).getData();
            for (OrgUnit orgUnit : list) {
                NodeTreeVO map = new NodeTreeVO();
                map.setId(orgUnit.getId());
                map.setName(orgUnit.getName());
                map.setParentId(id);
                map.setGuidPath(orgUnit.getGuidPath());
                map.setOrgType(orgUnit.getOrgType().getValue());
                map.setIsParent(false);
                if (orgUnit.getOrgType().getValue().equals(OrgTypeEnum.DEPARTMENT.getValue())
                    || orgUnit.getOrgType().getValue().equals(OrgTypeEnum.GROUP.getValue())) {
                    map.setIsParent(true);
                }
                listMap.add(map);
            }
        }
        return Y9Result.success(listMap, "获取成功");
    }

}
