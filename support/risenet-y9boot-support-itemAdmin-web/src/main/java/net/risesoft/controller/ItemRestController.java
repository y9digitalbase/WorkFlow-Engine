package net.risesoft.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import net.risesoft.api.org.DepartmentApi;
import net.risesoft.api.org.OrganizationApi;
import net.risesoft.api.resource.AppIconApi;
import net.risesoft.consts.UtilConsts;
import net.risesoft.entity.SpmApproveItem;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.model.AppIcon;
import net.risesoft.model.Department;
import net.risesoft.model.Organization;
import net.risesoft.model.processadmin.ProcessDefinitionModel;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.SpmApproveItemService;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.json.Y9JsonUtil;

import y9.client.rest.processadmin.RepositoryApiClient;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/22
 */
@RestController
@RequestMapping(value = "/vue/item")
public class ItemRestController {

    @Autowired
    private SpmApproveItemService spmApproveItemService;

    @Autowired
    private RepositoryApiClient repositoryManager;

    @Autowired
    private OrganizationApi organizationManager;

    @Autowired
    private DepartmentApi departmentManager;

    @Autowired
    private AppIconApi appIconApi;

    /**
     * 删除事项
     *
     * @param id 事项id
     * @return
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public Y9Result<String> delete(@RequestParam(required = true) String id) {
        Map<String, Object> map = spmApproveItemService.delete(id);
        if ((boolean)map.get(UtilConsts.SUCCESS)) {
            return Y9Result.successMsg((String)map.get("msg"));
        }
        return Y9Result.failure((String)map.get("msg"));
    }

    @SuppressWarnings("unused")
    private boolean deleteDirectory(String sPath) {
        if (!sPath.endsWith(File.separator)) {
            sPath = sPath + File.separator;
        }
        File dirFile = new File(sPath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean flag = true;
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) {
                    break;
                }
            } else {
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) {
                    break;
                }
            }
        }
        if (!flag) {
            return false;
        }
        if (dirFile.delete()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean deleteFile(String sPath) {
        boolean flag = false;
        File file = new File(sPath);
        if (file.isFile() && file.exists()) {
            file.delete();
            flag = true;
        }
        return flag;
    }

    /**
     * 获取部门
     *
     * @param id
     * @param name
     * @return
     */
    @RequestMapping(value = "/getDept")
    @ResponseBody
    public String getDept(@RequestParam(required = true) String id, @RequestParam(required = false) String name) {
        StringBuffer sb = new StringBuffer();
        getJson(sb, id);
        String json = "[" + sb.substring(0, sb.lastIndexOf(",")).toString() + "]";
        return json;
    }

    public void getJson(StringBuffer sb, String deptId) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        if (StringUtils.isBlank(deptId)) {
            List<Organization> orgList = organizationManager.listAllOrganizations(tenantId);
            if (orgList != null && orgList.size() > 0) {
                List<Department> deptList = organizationManager.listDepartments(tenantId, orgList.get(0).getId());
                for (Department dept : deptList) {
                    List<Department> subDeptList = departmentManager.listSubDepartments(tenantId, dept.getId());
                    boolean isParent = false;
                    if (subDeptList != null && subDeptList.size() > 0) {
                        isParent = true;
                    }
                    sb.append("{ id:'" + dept.getId() + "', pId:'" + orgList.get(0).getId() + "', name:'"
                        + dept.getName() + "', isParent: " + isParent + "},");
                }
            }
        } else {
            List<Department> deptList = departmentManager.listSubDepartments(tenantId, deptId);
            for (Department dept : deptList) {
                List<Department> subDeptList = departmentManager.listSubDepartments(tenantId, dept.getId());
                boolean isParent = false;
                if (subDeptList != null && subDeptList.size() > 0) {
                    isParent = true;
                }
                sb.append("{ id:'" + dept.getId() + "', pId:'" + deptId + "', name:'" + dept.getName() + "', isParent: "
                    + isParent + "},");
            }
        }
    }

    /**
     * 事项列表
     *
     * @return
     */
    @SuppressWarnings({"unchecked"})
    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Y9Result<List<SpmApproveItem>> list() {
        Map<String, Object> map = spmApproveItemService.list();
        List<SpmApproveItem> list = (List<SpmApproveItem>)map.get("rows");
        return Y9Result.success(list, "获取成功");
    }

    /**
     * 获取新增或修改数据
     *
     * @param id 事项id
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/newOrModify", method = RequestMethod.GET, produces = "application/json")
    public Y9Result<Map<String, Object>> newOrModify(@RequestParam(required = false) String id) {
        Map<String, Object> map = new HashMap<String, Object>(16);
        String tenantId = Y9LoginUserHolder.getTenantId();
        SpmApproveItem item = new SpmApproveItem();
        item.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
        if (StringUtils.isNotBlank(id)) {
            item = spmApproveItemService.findById(id);
        }
        map.put("item", item);
        List<Map<String, Object>> workflowList = new ArrayList<Map<String, Object>>();
        List<ProcessDefinitionModel> pdModelList = repositoryManager.getLatestProcessDefinitionList(tenantId);
        for (ProcessDefinitionModel pdModel : pdModelList) {
            Map<String, Object> row = new HashMap<String, Object>(16);
            row.put("id", pdModel.getKey());
            row.put("name", pdModel.getName());
            workflowList.add(row);
        }
        map.put("workflowList", workflowList);
        return Y9Result.success(map, "获取成功");
    }

    /**
     * 发布为应用系统
     *
     * @param itemId 事项id
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/publishToSystemApp", method = RequestMethod.POST, produces = "application/json")
    public Y9Result<String> publishToSystemApp(@RequestParam(required = true) String itemId) {
        Map<String, Object> map = spmApproveItemService.publishToSystemApp(itemId);
        if ((boolean)map.get(UtilConsts.SUCCESS)) {
            return Y9Result.successMsg((String)map.get("msg"));
        }
        return Y9Result.failure((String)map.get("msg"));
    }

    /**
     * 图片文件读取
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/readAppIconFile", method = RequestMethod.GET, produces = "application/json")
    public Y9Result<Map<String, Object>> readAppIconFile() {
        List<Map<String, String>> iconList = null;
        List<AppIcon> list = appIconApi.listAllIcon();
        iconList = new ArrayList<Map<String, String>>();
        if (list != null) {
            for (AppIcon appicon : list) {
                Map<String, String> filemap = new HashMap<String, String>(16);
                filemap.put("path", appicon.getPath());
                filemap.put("name", appicon.getName());
                filemap.put("iconData", appicon.getIconData());
                iconList.add(filemap);
            }
        }
        Map<String, Object> map = new HashMap<String, Object>(16);
        map.put("iconList", iconList);
        return Y9Result.success(map, "获取成功");
    }

    /**
     * 保存事项
     *
     * @param item 事项信息
     * @return
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public Y9Result<String> save(String itemJson) {
        SpmApproveItem item = Y9JsonUtil.readValue(itemJson, SpmApproveItem.class);
        Map<String, Object> map = spmApproveItemService.save(item);
        if ((boolean)map.get(UtilConsts.SUCCESS)) {
            return Y9Result.successMsg((String)map.get("msg"));
        }
        return Y9Result.failure((String)map.get("msg"));
    }

    /**
     * 图标搜索
     *
     * @param name 搜索词
     * @return
     */
    @RequestMapping(value = "/searchAppIcon", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Y9Result<Map<String, Object>> searchAppIcon(@RequestParam(required = false) String name) {
        List<AppIcon> list = appIconApi.searchAppIcon(name);
        List<Map<String, String>> iconList = new ArrayList<Map<String, String>>();
        if (list != null) {
            for (AppIcon appicon : list) {
                Map<String, String> filemap = new HashMap<String, String>(16);
                filemap.put("path", appicon.getPath());
                filemap.put("name", appicon.getName());
                filemap.put("iconData", appicon.getIconData());
                iconList.add(filemap);
            }
        }
        Map<String, Object> map = new HashMap<String, Object>(16);
        map.put("iconList", iconList);
        return Y9Result.success(map, "获取成功");
    }

}
