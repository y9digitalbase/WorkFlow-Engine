package net.risesoft.service.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.risesoft.api.platform.permission.PersonRoleApi;
import net.risesoft.api.processadmin.RepositoryApi;
import net.risesoft.consts.UtilConsts;
import net.risesoft.entity.SpmApproveItem;
import net.risesoft.entity.Y9FormItemBind;
import net.risesoft.entity.Y9PreFormItemBind;
import net.risesoft.entity.form.Y9FieldPerm;
import net.risesoft.entity.form.Y9Form;
import net.risesoft.entity.form.Y9FormField;
import net.risesoft.entity.form.Y9Table;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.model.itemadmin.Y9FormFieldModel;
import net.risesoft.model.processadmin.ProcessDefinitionModel;
import net.risesoft.model.user.UserInfo;
import net.risesoft.repository.form.Y9FieldPermRepository;
import net.risesoft.repository.form.Y9FormRepository;
import net.risesoft.service.FormDataService;
import net.risesoft.service.SpmApproveItemService;
import net.risesoft.service.Y9FormItemBindService;
import net.risesoft.service.Y9PreFormItemBindService;
import net.risesoft.service.form.Y9FormFieldService;
import net.risesoft.service.form.Y9FormService;
import net.risesoft.service.form.Y9TableService;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.json.Y9JsonUtil;
import net.risesoft.y9.util.Y9BeanUtil;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/20
 */
@Service
@Transactional(value = "rsTenantTransactionManager", readOnly = true)
public class FormDataServiceImpl implements FormDataService {

    private final JdbcTemplate jdbcTemplate;

    private final SpmApproveItemService spmApproveItemService;

    private final Y9FormItemBindService y9FormItemBindService;

    private final Y9PreFormItemBindService y9PreFormItemBindService;

    private final Y9FormFieldService y9FormFieldService;

    private final Y9FormService y9FormService;

    private final Y9FormRepository y9FormRepository;

    private final RepositoryApi repositoryManager;

    private final Y9FieldPermRepository y9FieldPermRepository;

    private final PersonRoleApi personRoleApi;

    private final Y9TableService y9TableService;

    public FormDataServiceImpl(@Qualifier("jdbcTemplate4Tenant") JdbcTemplate jdbcTemplate, SpmApproveItemService spmApproveItemService, Y9FormItemBindService y9FormItemBindService, Y9PreFormItemBindService y9PreFormItemBindService, Y9FormFieldService y9FormFieldService, Y9FormService y9FormService, Y9FormRepository y9FormRepository, RepositoryApi repositoryManager, Y9FieldPermRepository y9FieldPermRepository, PersonRoleApi personRoleApi, Y9TableService y9TableService) {
        this.jdbcTemplate = jdbcTemplate;
        this.spmApproveItemService = spmApproveItemService;
        this.y9FormItemBindService = y9FormItemBindService;
        this.y9PreFormItemBindService = y9PreFormItemBindService;
        this.y9FormFieldService = y9FormFieldService;
        this.y9FormService = y9FormService;
        this.y9FormRepository = y9FormRepository;
        this.repositoryManager = repositoryManager;
        this.y9FieldPermRepository = y9FieldPermRepository;
        this.personRoleApi = personRoleApi;
        this.y9TableService = y9TableService;
    }

    @Override
    @Transactional
    public Map<String, Object> delChildTableRow(String formId, String tableId, String guid) {
        return y9FormService.delChildTableRow(formId, tableId, guid);
    }

    @Override
    @Transactional
    public Map<String, Object> delPreFormData(String formId, String guid) {
        return y9FormService.delPreFormData(formId, guid);
    }

    @Override
    public List<Map<String, Object>> getAllFieldPerm(String formId, String taskDefKey, String processDefinitionId) {
        List<String> list = y9FieldPermRepository.findByFormId(formId);
        List<Map<String, Object>> listMap = new ArrayList<>();
        for (String fieldName : list) {
            Map<String, Object> map = this.getFieldPerm(formId, fieldName, taskDefKey, processDefinitionId);
            if (map != null) {
                listMap.add(map);
            }
        }
        return listMap;
    }

    @Override
    public Map<String, Object> getBindPreFormByItemId(String itemId) {
        Map<String, Object> map = new HashMap<>(16);
        map.put(UtilConsts.SUCCESS, true);
        map.put("formId", "");
        map.put("formName", "");
        try {
            Y9PreFormItemBind item = y9PreFormItemBindService.findByItemId(itemId);
            if (item != null) {
                map.put("formId", item.getFormId());
                map.put("formName", item.getFormName());
            }
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public List<Map<String, Object>> getChildTableData(String formId, String tableId, String processSerialNumber) throws Exception {
        return y9FormService.getChildTableData(formId, tableId, processSerialNumber);
    }

    @Override
    public Map<String, Object> getData(String tenantId, String itemId, String processSerialNumber) {
        Map<String, Object> retMap = new HashMap<>(16);
        try {
            SpmApproveItem item = spmApproveItemService.findById(itemId);
            String processDefineKey = item.getWorkflowGuid();
            ProcessDefinitionModel processDefinition = repositoryManager.getLatestProcessDefinitionByKey(tenantId, processDefineKey);
            List<Y9FormItemBind> formList = y9FormItemBindService.findByItemIdAndProcDefIdAndTaskDefKeyIsNull(itemId, processDefinition.getId());
            List<Map<String, Object>> list = null;
            for (Y9FormItemBind bind : formList) {
                String formId = bind.getFormId();
                // 获取表单绑定的表,可能多个
                List<String> tableNameList = y9FormRepository.findBindTableName(formId);
                for (String tableName : tableNameList) {
                    Y9Table y9Table = y9TableService.findByTableName(tableName);
                    // 只获取主表
                    if (y9Table.getTableType() == 1) {
                        list = jdbcTemplate.queryForList("SELECT * FROM " + tableName.toUpperCase() + " WHERE GUID=?", processSerialNumber);
                        if (list.size() > 0) {
                            retMap.putAll(list.get(0));
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return retMap;
    }

    @Override
    public Map<String, Object> getFieldPerm(String formId, String fieldName, String taskDefKey, String processDefinitionId) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        Y9LoginUserHolder.setTenantId(tenantId);
        Map<String, Object> resMap = new HashMap<>(16);
        // 写权限
        resMap.put("writePerm", false);
        resMap.put("fieldName", fieldName);
        try {
            Y9FieldPerm y9FieldPerm = y9FieldPermRepository.findByFormIdAndFieldNameAndTaskDefKey(formId, fieldName, taskDefKey);
            if (y9FieldPerm != null) {
                resMap.putAll(getFieldPerm(y9FieldPerm));
            } else {
                resMap = null;
                return resMap;
            }
            resMap.put(UtilConsts.SUCCESS, true);
        } catch (Exception e) {
            resMap.put(UtilConsts.SUCCESS, false);
            e.printStackTrace();
        }
        return resMap;
    }

    /**
     * 解析权限 Description:
     *
     * @param y9FieldPerm
     * @return
     */
    public Map<String, Object> getFieldPerm(Y9FieldPerm y9FieldPerm) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        UserInfo person = Y9LoginUserHolder.getUserInfo();
        Map<String, Object> resMap = new HashMap<>(16);
        // 绑定了角色
        if (StringUtils.isNotBlank(y9FieldPerm.getWriteRoleId())) {
            resMap.put("writePerm", false);
            String roleId = y9FieldPerm.getWriteRoleId();
            String[] roleIds = roleId.split(",");
            for (String id : roleIds) {
                boolean b = personRoleApi.hasRole(tenantId, id, person.getPersonId()).getData();
                if (b) {
                    resMap.put("writePerm", true);
                    break;
                }
            }
        } else {// 未绑定角色，默认该节点所有人都有写权限
            // 写权限
            resMap.put("writePerm", true);
        }
        return resMap;
    }

    @Override
    public List<Y9FormFieldModel> getFormField(String itemId) {
        List<Y9FormFieldModel> list = new ArrayList<Y9FormFieldModel>();
        try {
            SpmApproveItem item = spmApproveItemService.findById(itemId);
            String processDefineKey = item.getWorkflowGuid();
            ProcessDefinitionModel processDefinition = repositoryManager.getLatestProcessDefinitionByKey(Y9LoginUserHolder.getTenantId(), processDefineKey);
            List<Y9FormItemBind> formList = y9FormItemBindService.findByItemIdAndProcDefIdAndTaskDefKeyIsNull(itemId, processDefinition.getId());
            for (Y9FormItemBind form : formList) {
                List<Y9FormField> formElementList = y9FormFieldService.findByFormId(form.getFormId());
                for (Y9FormField formElement : formElementList) {
                    if (StringUtils.isNotBlank(formElement.getQuerySign()) && formElement.getQuerySign().equals("1")) {
                        Y9FormFieldModel model = new Y9FormFieldModel();
                        Y9BeanUtil.copyProperties(formElement, model);
                        if (!list.contains(model)) {
                            list.add(model);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Map<String, String>> getFormFieldDefine(String formId) {
        List<Map<String, String>> listMap = new ArrayList<Map<String, String>>();
        try {
            List<Y9FormField> formElementList = y9FormFieldService.findByFormId(formId);
            for (Y9FormField formElement : formElementList) {
                Map<String, String> map = new HashMap<String, String>(16);
                String formCtrltype = formElement.getFieldType();
                String disChinaName = formElement.getFieldCnName();
                String formCtrlName = formElement.getFieldName();
                String columnName = formElement.getFieldName();
                map.put("formCtrltype", formCtrltype);
                map.put("disChinaName", disChinaName);
                map.put("formCtrlName", formCtrlName);
                map.put("columnName", columnName);
                if (!listMap.contains(map)) {
                    listMap.add(map);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listMap;
    }

    @Override
    public String getFormJson(String formId) {
        String formJson = "";
        try {
            Y9Form y9Form = y9FormRepository.findById(formId).orElse(null);
            formJson = y9Form.getFormJson();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return formJson;
    }

    @Override
    public Map<String, Object> getFromData(String formId, String processSerialNumber) {
        Map<String, Object> map = new HashMap<>(16);
        try {
            map = y9FormService.getFormData(formId, processSerialNumber);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public List<Map<String, Object>> getPreFormDataByFormId(String formId) {
        return y9FormService.getFormDataList(formId);
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional
    public String saveAFormData(String itemId, String formdata, String formId) throws Exception {
        try {
            Map<String, Object> mapFormJsonData = Y9JsonUtil.readValue(formdata, Map.class);
            List<Map<String, Object>> listMap = new ArrayList<>();
            Map<String, Object> map = new HashMap<>(16);
            map.put("name", "form_Id");
            map.put("value", formId);
            listMap.add(map);
            for (String columnName : mapFormJsonData.keySet()) {
                // 根据数据库表名获取列名
                String value = mapFormJsonData.get(columnName).toString();
                map = new HashMap<>(16);
                map.put("name", columnName);
                map.put("value", value);
                listMap.add(map);
            }
            formdata = Y9JsonUtil.writeValueAsString(listMap);
            map = y9FormService.saveFormData(formdata);// 保存前置表单数据
            if (!(boolean)map.get(UtilConsts.SUCCESS)) {
                throw new Exception("FormDataService savePreFormData前置表单 error0");
            }

            // 获取事项绑定主表信息
            SpmApproveItem item = spmApproveItemService.findById(itemId);
            String processDefineKey = item.getWorkflowGuid();
            ProcessDefinitionModel processDefinition = repositoryManager.getLatestProcessDefinitionByKey(Y9LoginUserHolder.getTenantId(), processDefineKey);
            List<Y9FormItemBind> list = y9FormItemBindService.findByItemIdAndProcDefIdAndTaskDefKeyIsNull(itemId, processDefinition.getId());
            String bindFormId = "";
            for (Y9FormItemBind form : list) {
                bindFormId = form.getFormId();
            }
            String processSerialNumber = Y9IdGenerator.genId(IdType.SNOWFLAKE);
            if (!bindFormId.equals("")) {
                List<Map<String, Object>> list1 = Y9JsonUtil.readValue(formdata, List.class);
                for (Map<String, Object> map1 : list1) {
                    if (map1.get("name").equals("form_Id")) {// 重设表单id
                        map1.put("value", bindFormId);
                    } else if (map1.get("name").equals("guid")) {// 重设主键id
                        map1.put("value", processSerialNumber);
                    } else if (map1.get("name").equals("GUID")) {// 重设主键id
                        map1.put("value", processSerialNumber);
                    }
                }
                formdata = Y9JsonUtil.writeValueAsString(list1);
                map = y9FormService.saveFormData(formdata);// 保存主表信息
                if (!(boolean)map.get(UtilConsts.SUCCESS)) {
                    throw new Exception("FormDataService savePreFormData主表 error0");
                }
                return processSerialNumber;// 返回主表主键id
            }
        } catch (Exception e) {
            System.out.println("****************************formdata:" + formdata);
            final Writer result = new StringWriter();
            final PrintWriter print = new PrintWriter(result);
            e.printStackTrace(print);
            String msg = result.toString();
            System.out.println(msg);
            throw new Exception("FormDataService savePreFormData error1");
        }
        return "";
    }

    @Override
    @Transactional
    public void saveChildTableData(String formId, String tableId, String processSerialNumber, String jsonData) throws Exception {
        try {
            Map<String, Object> map = new HashMap<>(16);
            map = y9FormService.saveChildTableData(formId, tableId, processSerialNumber, jsonData);
            if (!(boolean)map.get(UtilConsts.SUCCESS)) {
                throw new Exception("FormDataService saveFormData error");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("FormDataService saveFormData error");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional
    public void saveFormData(String formdata, String formId) throws Exception {
        try {
            Map<String, Object> mapFormJsonData = Y9JsonUtil.readValue(formdata, Map.class);
            List<Map<String, Object>> listMap = new ArrayList<>();
            Map<String, Object> map = new HashMap<>(16);
            map.put("name", "form_Id");
            map.put("value", formId);
            listMap.add(map);
            for (String columnName : mapFormJsonData.keySet()) {
                // 根据数据库表名获取列名
                String value = mapFormJsonData.get(columnName).toString();
                map = new HashMap<>(16);
                map.put("name", columnName);
                map.put("value", value);
                listMap.add(map);
            }
            formdata = Y9JsonUtil.writeValueAsString(listMap);
            map = y9FormService.saveFormData(formdata);
            if (!(boolean)map.get(UtilConsts.SUCCESS)) {
                throw new Exception("FormDataService saveFormData error0");
            }
        } catch (Exception e) {
            System.out.println("****************************formdata:" + formdata);
            final Writer result = new StringWriter();
            final PrintWriter print = new PrintWriter(result);
            e.printStackTrace(print);
            String msg = result.toString();
            System.out.println(msg);
            throw new Exception("FormDataService saveFormData error1");
        }
    }
}
