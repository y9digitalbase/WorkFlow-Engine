package net.risesoft.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

import net.risesoft.api.org.ManagerApi;
import net.risesoft.api.org.PersonApi;
import net.risesoft.entity.TaoHongTemplate;
import net.risesoft.entity.TaoHongTemplateType;
import net.risesoft.model.Manager;
import net.risesoft.model.OrgUnit;
import net.risesoft.model.user.UserInfo;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.TaoHongTemplateService;
import net.risesoft.service.TaoHongTemplateTypeService;
import net.risesoft.y9.Y9LoginUserHolder;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/22
 */
@RestController
@RequestMapping(value = "/vue/taoHongTemplate")
@Slf4j
public class TaoHongTemplateRestContronller {

    @Autowired
    private TaoHongTemplateService taoHongTemplateService;

    @Autowired
    private TaoHongTemplateTypeService taoHongTemplateTypeService;

    @Autowired
    private PersonApi personManager;

    @Autowired
    private ManagerApi managerApi;

    @Resource(name = "jdbcTemplate4Tenant")
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取委办局树
     *
     * @param name 部门名称
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/bureauTree", method = RequestMethod.GET, produces = "application/json")
    public Y9Result<List<Map<String, Object>>> bureauTree(@RequestParam(required = false) String name) {
        List<Map<String, Object>> listMap = new ArrayList<>();
        name = StringUtils.isBlank(name) ? "" : name;
        List<Map<String, Object>> orgUnitList = jdbcTemplate.queryForList(
            " SELECT ID,NAME,PARENT_ID FROM Y9_ORG_DEPARTMENT where bureau = 1 and deleted = 0 and name like '%" + name
                + "%' and disabled = 0 order by tab_Index asc");
        for (Map<String, Object> dept : orgUnitList) {
            Map<String, Object> map = new HashMap<>(16);
            map.put("id", dept.get("ID").toString());
            map.put("name", dept.get("NAME").toString());
            map.put("parentId", dept.get("PARENT_ID").toString());
            listMap.add(map);
        }
        return Y9Result.success(listMap, "获取成功");
    }

    /**
     * 下载套红
     *
     * @param templateGuid 模板id
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/download")
    public void download(@RequestParam String templateGuid, HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        try {
            TaoHongTemplate taoHongTemplate = taoHongTemplateService.findOne(templateGuid);
            byte[] b = taoHongTemplate.getTemplateContent();
            int length = b.length;
            String filename = taoHongTemplate.getTemplateFileName();
            String userAgent = "User-Agent", firefox = "firefox", msie = "MSIE";
            if (request.getHeader(userAgent).toLowerCase().indexOf(firefox) > 0) {
                filename = new String(filename.getBytes("UTF-8"), "ISO8859-1");
            } else if (request.getHeader(userAgent).toUpperCase().indexOf(msie) > 0) {
                filename = URLEncoder.encode(filename, "UTF-8");
            } else {
                filename = URLEncoder.encode(filename, "UTF-8");
            }
            response.setContentType("application/octet-stream");
            response.setHeader("Content-disposition", "attachment; filename=" + filename);
            response.setHeader("Content-Length", String.valueOf(length));
            IOUtils.write(b, response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取套红列表
     *
     * @param name 委办局名称
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getList", method = RequestMethod.GET, produces = "application/json")
    public Y9Result<List<Map<String, Object>>> getList(@RequestParam(required = false) String name) {
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<TaoHongTemplate> list = new ArrayList<>();
        if (userInfo.isGlobalManager()) {
            list = taoHongTemplateService.findByTenantId(Y9LoginUserHolder.getTenantId(),
                StringUtils.isBlank(name) ? "%%" : "%" + name + "%");
        } else {
            OrgUnit orgUnit = personManager.getBureau(Y9LoginUserHolder.getTenantId(), userInfo.getPersonId());
            list = taoHongTemplateService.findByBureauGuid(orgUnit.getId());
        }
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> map = new HashMap<>(16);
            map.put("templateGuid", list.get(i).getTemplateGuid());
            map.put("templateFileName", list.get(i).getTemplateFileName());
            map.put("bureauName", list.get(i).getBureauName());
            map.put("templateType", list.get(i).getTemplateType());
            map.put("uploadTime", sdf.format(list.get(i).getUploadTime()));

            String userId = list.get(i).getUserId();
            Manager manger = managerApi.getManager(Y9LoginUserHolder.getTenantId(), userId);
            map.put("userName", manger != null ? manger.getName() : "人员不存在");
            map.put("tabIndex", list.get(i).getTabIndex());
            items.add(map);
        }
        return Y9Result.success(items, "获取成功");
    }

    /**
     * 获取新增编辑信息
     *
     * @param id 套红id
     * @return
     */
    @RequestMapping(value = "/newOrModify", method = RequestMethod.GET, produces = "application/json")
    public Y9Result<Map<String, Object>> newOrModify(@RequestParam(required = false) String id) {
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        String tenantId = Y9LoginUserHolder.getTenantId(), personId = userInfo.getPersonId();
        Map<String, Object> map = new HashMap<>(16);
        List<TaoHongTemplateType> typeList = new ArrayList<>();
        map.put("tenantManager", userInfo.isGlobalManager());
        if (userInfo.isGlobalManager()) {
            typeList = taoHongTemplateTypeService.findAll();
        } else {
            OrgUnit orgUnit = personManager.getBureau(tenantId, personId);
            map.put("bureauGuid", orgUnit.getId());
            map.put("bureauName", orgUnit.getName());
            typeList = taoHongTemplateTypeService.findByBureauId(orgUnit.getId());
        }
        map.put("typeList", typeList);
        if (StringUtils.isNotEmpty(id)) {
            TaoHongTemplate taoHongTemplate = taoHongTemplateService.findOne(id);
            map.put("taoHongTemplate", taoHongTemplate);
        }
        return Y9Result.success(map, "获取成功");
    }

    /**
     * 删除套红
     *
     * @param ids 套红ids
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/removeTaoHongTemplate", method = RequestMethod.POST, produces = "application/json")
    public Y9Result<String> removeTaoHongTemplate(@RequestParam String[] ids) {
        taoHongTemplateService.removeTaoHongTemplate(ids);
        return Y9Result.successMsg("删除成功");
    }

    /**
     * 保存套红信息
     *
     * @param taoHongInfo 套红信息
     * @param file 文件
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/saveOrUpdate", method = RequestMethod.POST, produces = "application/json")
    public Y9Result<String> saveOrUpdate(@RequestParam(required = false) String templateGuid,
        @RequestParam String bureauGuid, @RequestParam String bureauName, @RequestParam String templateType,
        MultipartFile file) {
        try {
            TaoHongTemplate taoHong = new TaoHongTemplate();
            taoHong.setBureauGuid(bureauGuid);
            taoHong.setBureauName(bureauName);
            taoHong.setTemplateGuid(templateGuid);
            taoHong.setTemplateType(templateType);
            LOGGER.debug("###################### bureauGuid:{},bureauName:{}", taoHong.getBureauGuid(),
                taoHong.getBureauName());
            if (file != null) {
                String[] fileName = file.getOriginalFilename().split("\\\\");
                taoHong.setTemplateContent(file.getBytes());
                if (fileName.length > 1) {
                    taoHong.setTemplateFileName(fileName[fileName.length - 1]);
                } else {
                    taoHong.setTemplateFileName(file.getOriginalFilename());
                }
            }
            taoHongTemplateService.saveOrUpdate(taoHong);
            return Y9Result.successMsg("保存成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Y9Result.failure("保存失败");
    }

}
