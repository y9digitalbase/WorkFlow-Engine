package net.risesoft.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.risesoft.api.itemadmin.OpinionApi;
import net.risesoft.api.org.DepartmentApi;
import net.risesoft.api.org.OrgUnitApi;
import net.risesoft.api.org.OrganizationApi;
import net.risesoft.api.org.PersonApi;
import net.risesoft.api.permission.PersonRoleApi;
import net.risesoft.consts.UtilConsts;
import net.risesoft.enums.platform.OrgTypeEnum;
import net.risesoft.enums.platform.OrgTreeTypeEnum;
import net.risesoft.model.itemadmin.OpinionHistoryModel;
import net.risesoft.model.itemadmin.OpinionModel;
import net.risesoft.model.platform.OrgUnit;
import net.risesoft.model.platform.Organization;
import net.risesoft.model.platform.Person;
import net.risesoft.model.user.UserInfo;
import net.risesoft.pojo.Y9Result;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.json.Y9JsonUtil;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2023/01/03
 */
@RestController
@RequestMapping("/vue/opinion")
public class OpinionRestController {

    private static final Logger log = LoggerFactory.getLogger(OpinionRestController.class);

    @Autowired
    private OpinionApi opinionManager;

    @Autowired
    private OrgUnitApi orgUnitApi;

    @Autowired
    private DepartmentApi departmentApi;

    @Autowired
    private PersonApi personApi;

    @Autowired
    private OrganizationApi organizationApi;

    @Autowired
    private PersonRoleApi personRoleApi;

    @RequestMapping(value = "/checkSignOpinion")
    public Map<String, Object> checkSignOpinion(@RequestParam(required = false) String taskId,
        @RequestParam(required = false) String processSerialNumber) {
        Map<String, Object> map = new HashMap<String, Object>(16);
        try {
            UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
            String userId = userInfo.getPersonId(), tenantId = userInfo.getTenantId();
            Boolean checkSignOpinion = opinionManager.checkSignOpinion(tenantId, userId, processSerialNumber, taskId);
            map.put("checkSignOpinion", checkSignOpinion);
        } catch (Exception e) {
            log.error("查询" + taskId + "是否签写意见失败！");
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 获取意见框历史记录数量
     *
     * @param processSerialNumber 流程编号
     * @param opinionFrameMark 意见框标识
     * @return
     */
    @RequestMapping(value = "/countOpinionHistory", method = RequestMethod.GET, produces = "application/json")
    public Y9Result<Integer> countOpinionHistory(@RequestParam(required = true) String processSerialNumber,
        @RequestParam(required = true) String opinionFrameMark) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        Integer num = opinionManager.countOpinionHistory(tenantId, processSerialNumber, opinionFrameMark);
        return Y9Result.success(num, "获取成功");
    }

    /**
     * 删除意见
     *
     * @param id 意见id
     * @return
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST, produces = "application/json")
    public Y9Result<String> delete(@RequestParam(required = true) String id) {
        try {
            UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
            String userId = userInfo.getPersonId(), tenantId = userInfo.getTenantId();
            opinionManager.delete(tenantId, userId, id);
            return Y9Result.successMsg("刪除成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Y9Result.failure("删除失败");
    }

    /**
     * 委办局树搜索
     *
     * @param name 人员名称
     * @return
     */
    @RequestMapping(value = "/bureauTreeSearch", method = RequestMethod.GET, produces = "application/json")
    public Y9Result<List<Map<String, Object>>> deptTreeSearch(@RequestParam(required = false) String name) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        List<Map<String, Object>> item = new ArrayList<Map<String, Object>>();
        OrgUnit bureau = personApi.getBureau(tenantId, Y9LoginUserHolder.getUserInfo().getPersonId()).getData();
        if (bureau != null) {
            List<OrgUnit> orgUnitList = new ArrayList<OrgUnit>();
            orgUnitList =
                orgUnitApi.treeSearchByDn(tenantId, name, OrgTreeTypeEnum.TREE_TYPE_PERSON, bureau.getDn()).getData();
            for (OrgUnit orgUnit : orgUnitList) {
                Map<String, Object> map = new HashMap<String, Object>(16);
                map.put("id", orgUnit.getId());
                map.put("name", orgUnit.getName());
                map.put("orgType", orgUnit.getOrgType());
                map.put("parentId", orgUnit.getParentId());
                map.put("isParent", true);
                if (OrgTypeEnum.PERSON.equals(orgUnit.getOrgType())) {
                    Person per = personApi.getPerson(Y9LoginUserHolder.getTenantId(), orgUnit.getId()).getData();
                    if (per.getDisabled()) {
                        continue;
                    }
                    map.put("sex", per.getSex());
                    map.put("duty", per.getDuty());
                    map.put("isParent", false);
                    map.put("parentId", orgUnit.getParentId());
                }
                item.add(map);
            }
        }
        return Y9Result.success(item, "获取成功");
    }

    /**
     * 获取事项绑定的意见框列表
     *
     * @param itemId 事项id
     * @param processDefinitionId 流程定义id
     * @return
     */
    @RequestMapping(value = "/getBindOpinionFrame", method = RequestMethod.GET, produces = "application/json")
    public Y9Result<List<String>> getBindOpinionFrame(@RequestParam(required = true) String itemId,
        @RequestParam(required = true) String processDefinitionId) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        List<String> list = opinionManager.getBindOpinionFrame(tenantId, itemId, processDefinitionId);
        return Y9Result.success(list, "获取成功");
    }

    /**
     * 获取委办局树
     *
     * @param id 父节点id
     * @return
     */
    @RequestMapping(value = "/getBureauTree", method = RequestMethod.GET, produces = "application/json")
    public Y9Result<List<Map<String, Object>>> getBureauTree(@RequestParam(required = false) String id) {
        List<Map<String, Object>> item = new ArrayList<Map<String, Object>>();
        String tenantId = Y9LoginUserHolder.getTenantId();
        if (StringUtils.isBlank(id)) {
            OrgUnit orgUnit = personApi.getBureau(tenantId, Y9LoginUserHolder.getUserInfo().getPersonId()).getData();
            if (orgUnit != null) {
                Map<String, Object> m = new HashMap<String, Object>(16);
                id = orgUnit.getId();
                m.put("id", orgUnit.getId());
                m.put("name", orgUnit.getName());
                m.put("parentId", orgUnit.getParentId());
                m.put("orgType", orgUnit.getOrgType());
                m.put("isParent", true);
                item.add(m);
            }
        } else {
            List<OrgUnit> list = orgUnitApi.getSubTree(tenantId, id, OrgTreeTypeEnum.TREE_TYPE_ORG).getData();
            for (OrgUnit orgUnit : list) {
                Map<String, Object> m = new HashMap<String, Object>(16);
                m.put("id", orgUnit.getId());
                m.put("name", orgUnit.getName());
                m.put("parentId", orgUnit.getParentId());
                m.put("orgType", orgUnit.getOrgType());
                m.put("isParent", true);
                if (orgUnit.getOrgType().equals(OrgTypeEnum.PERSON)) {
                    m.put("isParent", false);
                    Person person = personApi.getPerson(tenantId, orgUnit.getId()).getData();
                    if (person.getDisabled()) {
                        continue;
                    }
                    m.put("sex", person.getSex());
                }
                if (orgUnit.getOrgType().equals(OrgTypeEnum.PERSON)
                    || orgUnit.getOrgType().equals(OrgTypeEnum.DEPARTMENT)) {
                    item.add(m);
                }
            }
        }
        return Y9Result.success(item, "获取成功");
    }

    public OrgUnit getParent(String tenantId, String nodeId, String parentId) {
        Organization parent = organizationApi.getOrganization(tenantId, parentId).getData();
        return parent.getId() != null ? parent : departmentApi.getDepartment(tenantId, parentId).getData();
    }

    /**
     * 获取意见框历史记录
     *
     * @param processSerialNumber 流程编号
     * @param opinionFrameMark 意见框标识
     * @return
     */
    @RequestMapping(value = "/opinionHistoryList", method = RequestMethod.GET, produces = "application/json")
    public Y9Result<List<OpinionHistoryModel>> opinionHistoryList(
        @RequestParam(required = true) String processSerialNumber,
        @RequestParam(required = true) String opinionFrameMark) {
        List<OpinionHistoryModel> list = new ArrayList<OpinionHistoryModel>();
        String tenantId = Y9LoginUserHolder.getTenantId();
        list = opinionManager.opinionHistoryList(tenantId, processSerialNumber, opinionFrameMark);
        return Y9Result.success(list, "获取成功");
    }

    /**
     * 获取新增或编辑意见前数据
     *
     * @param id 意见id
     * @return
     */
    @RequestMapping(value = "/newOrModify/personalComment", method = RequestMethod.GET, produces = "application/json")
    public Y9Result<Map<String, Object>> personalComment(@RequestParam(required = false) String id) {
        Map<String, Object> map = new HashMap<String, Object>(16);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        String userId = userInfo.getPersonId(), tenantId = userInfo.getTenantId();
        map.put("date", sdf.format(new Date()));
        OpinionModel opinion = new OpinionModel();
        if (StringUtils.isNotBlank(id)) {
            opinion = opinionManager.getById(tenantId, userId, id);
            map.put("opinion", opinion);
            map.put("date", opinion.getCreateDate());
        }
        boolean hasRole = personRoleApi
            .hasRole(Y9LoginUserHolder.getTenantId(), "itemAdmin", "", "代录意见角色", userInfo.getPersonId()).getData();
        map.put("hasRole", hasRole);
        return Y9Result.success(map, "获取成功");
    }

    /**
     * 获取意见列表
     *
     * @param processSerialNumber 流程编号
     * @param taskId 任务id
     * @param itembox 办件状态
     * @param opinionFrameMark 意见框标识
     * @param itemId 事项id
     * @param taskDefinitionKey 任务key
     * @param activitiUser 办理人
     * @return
     */
    @RequestMapping(value = "/personCommentList", method = RequestMethod.GET, produces = "application/json")
    public Y9Result<List<Map<String, Object>>> personCommentList(
        @RequestParam(required = true) String processSerialNumber, @RequestParam(required = false) String taskId,
        @RequestParam(required = true) String itembox, @RequestParam(required = true) String opinionFrameMark,
        @RequestParam(required = true) String itemId, @RequestParam(required = false) String taskDefinitionKey,
        @RequestParam(required = false) String activitiUser) {
        List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        String userId = userInfo.getPersonId(), tenantId = userInfo.getTenantId();
        listMap = opinionManager.personCommentList(tenantId, userId, processSerialNumber, taskId, itembox,
            opinionFrameMark, itemId, taskDefinitionKey, activitiUser);
        return Y9Result.success(listMap, "获取成功");
    }

    public void recursionUpToOrg(String tenantId, String nodeId, String parentId, List<OrgUnit> orgUnitList,
        boolean isParent) {
        OrgUnit parent = getParent(tenantId, nodeId, parentId);
        if (isParent) {
            parent.setDescription("parent");
        }
        if (orgUnitList.size() == 0) {
            orgUnitList.add(parent);
        } else {
            String add = "true";
            for (OrgUnit orgUnit : orgUnitList) {
                if (orgUnit.getId().equals(parent.getId())) {
                    add = "false";
                    break;
                }
            }
            if (add == UtilConsts.TRUE) {
                orgUnitList.add(parent);
            }
        }
        if (parent.getOrgType().equals(OrgTypeEnum.DEPARTMENT)) {
            if (parent.getId().equals(nodeId)) {
                return;
            }
            recursionUpToOrg(tenantId, nodeId, parent.getParentId(), orgUnitList, true);
        }
    }

    /**
     * 保存意见
     *
     * @param opinion 意见实体
     * @return
     */
    @RequestMapping(value = "/saveOrUpdate", method = RequestMethod.POST, produces = "application/json")
    public Y9Result<OpinionModel> save(String jsonData) {
        try {
            UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
            String userId = userInfo.getPersonId(), tenantId = userInfo.getTenantId();
            OpinionModel opinion = Y9JsonUtil.readValue(jsonData, OpinionModel.class);
            OpinionModel opinionModel = opinionManager.saveOrUpdate(tenantId, userId, opinion);
            return Y9Result.success(opinionModel, "保存成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Y9Result.failure("保存失败");
    }
}