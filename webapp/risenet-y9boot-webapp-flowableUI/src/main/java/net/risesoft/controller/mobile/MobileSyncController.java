package net.risesoft.controller.mobile;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

import net.risesoft.api.itemadmin.OfficeDoneInfoApi;
import net.risesoft.api.itemadmin.ProcessParamApi;
import net.risesoft.enums.DialectEnum;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.model.itemadmin.OfficeDoneInfoModel;
import net.risesoft.model.itemadmin.ProcessParamModel;
import net.risesoft.util.DbMetaDataUtil;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.json.Y9JsonUtil;
import net.risesoft.y9.util.Y9Util;

/**
 * 同步办结件至数据中心
 *
 * @author qinman
 * @author zhangchongjie
 * @date 2023/01/03
 */
@RestController
@RequestMapping("/mobile/sync")
@Slf4j
public class MobileSyncController {

    @Resource(name = "jdbcTemplate4Tenant")
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private OfficeDoneInfoApi officeDoneInfoManager;

    @Autowired
    private ProcessParamApi processParamManager;

    /**
     * 删除正在运行的数据
     *
     * @param tenantId
     * @param request
     * @param response
     */
    @ResponseBody
    @RequestMapping(value = "/deleteTableData")
    public void deleteTableData(String tenantId, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> resMap = new HashMap<String, Object>(16);
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            String sql = "SELECT" + "	P .PROC_INST_ID_" + " FROM" + "	ACT_HI_PROCINST P" + " WHERE" + "	(P.END_TIME_ is not null or P.DELETE_REASON_ = '已删除')" + " ORDER BY" + "	P .START_TIME_ DESC";
            List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
            LOGGER.info("*********************共{}条数据***************************", list.size());
            int i = 0;
            for (Map<String, Object> map : list) {
                String processInstanceId = "";
                try {
                    processInstanceId = (String)map.get("PROC_INST_ID_");
                    String sql3 = "DELETE from ACT_HI_TASKINST where PROC_INST_ID_ = '" + processInstanceId + "'";
                    // 删除历史任务
                    jdbcTemplate.execute(sql3);

                    sql3 = "DELETE" + " FROM" + "	ACT_GE_BYTEARRAY" + " WHERE" + "	ID_ IN (" + "		SELECT" + "			*" + "		FROM ( " + "         SELECT" + "			b.ID_" + "		  FROM" + "			 ACT_GE_BYTEARRAY b"
                        + "		  LEFT JOIN ACT_HI_VARINST v ON v.BYTEARRAY_ID_ = b.ID_" + "		  WHERE" + "			 v.PROC_INST_ID_ = '" + processInstanceId + "'" + "		  AND v.NAME_ = 'users'" + "       ) TT" + "	)";
                    // 删除二进制数据表
                    jdbcTemplate.execute(sql3);

                    sql3 = "DELETE from ACT_HI_VARINST where PROC_INST_ID_ = '" + processInstanceId + "'";
                    // 删除历史变量
                    jdbcTemplate.execute(sql3);

                    sql3 = "DELETE from ACT_HI_IDENTITYLINK where PROC_INST_ID_ = '" + processInstanceId + "'";
                    // 删除历史参与人
                    jdbcTemplate.execute(sql3);

                    sql3 = "DELETE from ACT_HI_ACTINST where PROC_INST_ID_ = '" + processInstanceId + "'";
                    // 删除历史节点
                    jdbcTemplate.execute(sql3);

                    sql3 = "DELETE from ACT_HI_PROCINST where PROC_INST_ID_ = '" + processInstanceId + "'";
                    // 删除流程实例
                    jdbcTemplate.execute(sql3);
                } catch (Exception e) {
                    i = i + 1;
                    e.printStackTrace();
                }
            }
            LOGGER.info("********************同步失败{}条数据***************************", i);
            resMap.put("总数", list.size());
            resMap.put("同步失败", i);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(resMap));
    }

    private String getActGeBytearraySql(String year, String processInstanceId) {
        String sql = "INSERT INTO ACT_GE_BYTEARRAY_" + year + " (" + "	ID_," + "	REV_," + "	NAME_," + "	DEPLOYMENT_ID_," + "	BYTES_," + "	GENERATED_" + " ) SELECT " + "	b.ID_," + "	b.REV_," + "	b.NAME_," + "	b.DEPLOYMENT_ID_," + "	b.BYTES_," + "	b.GENERATED_" + " FROM"
            + "	ACT_GE_BYTEARRAY b" + " LEFT JOIN ACT_HI_VARINST v ON v.BYTEARRAY_ID_ = b.ID_" + " WHERE" + "	v.PROC_INST_ID_ = '" + processInstanceId + "'" + " AND v.NAME_ = 'users'";
        return sql;
    }

    private String getActHiActinstSql(String year, String processInstanceId) {
        String sql3 = "INSERT INTO ACT_HI_ACTINST_" + year + " (" + "	ID_," + "	REV_," + "	PROC_DEF_ID_," + "	PROC_INST_ID_," + "	EXECUTION_ID_," + "	ACT_ID_," + "	TASK_ID_," + "	CALL_PROC_INST_ID_," + "	ACT_NAME_," + "	ACT_TYPE_," + "	ASSIGNEE_," + "	START_TIME_," + "	END_TIME_,"
            + "	DURATION_," + "	DELETE_REASON_," + "	TENANT_ID_" + " ) SELECT" + "	ID_," + "	REV_," + "	PROC_DEF_ID_," + "	PROC_INST_ID_," + "	EXECUTION_ID_," + "	ACT_ID_," + "	TASK_ID_," + "	CALL_PROC_INST_ID_," + "	ACT_NAME_," + "	ACT_TYPE_," + "	ASSIGNEE_," + "	START_TIME_,"
            + "	END_TIME_," + "	DURATION_," + "	DELETE_REASON_," + "	TENANT_ID_" + " FROM" + "	ACT_HI_ACTINST A" + " WHERE" + "	A.PROC_INST_ID_ = '" + processInstanceId + "'";
        return sql3;
    }

    private String getActHiIdentiyLinkSql(String year, String processInstanceId) {
        String sql3 = "INSERT INTO ACT_HI_IDENTITYLINK_" + year + " (" + "	ID_," + "	GROUP_ID_," + "	TYPE_," + "	USER_ID_," + "	TASK_ID_," + "	CREATE_TIME_," + "	PROC_INST_ID_," + "	SCOPE_ID_," + "	SCOPE_TYPE_," + "	SCOPE_DEFINITION_ID_" + " ) SELECT" + "	ID_," + "	GROUP_ID_," + "	TYPE_,"
            + "	USER_ID_," + "	TASK_ID_," + "	CREATE_TIME_," + "	PROC_INST_ID_," + "	SCOPE_ID_," + "	SCOPE_TYPE_," + "	SCOPE_DEFINITION_ID_" + " FROM" + "	ACT_HI_IDENTITYLINK i" + " WHERE" + "	i.PROC_INST_ID_ = '" + processInstanceId + "'";
        return sql3;
    }

    private String getActHiProcinstSql(String year, String processInstanceId) {
        String sql = "INSERT INTO ACT_HI_PROCINST_" + year + " (" + "	ID_," + "	REV_," + "	PROC_INST_ID_," + "	BUSINESS_KEY_," + "	PROC_DEF_ID_," + "	START_TIME_," + "	END_TIME_," + "	DURATION_," + "	START_USER_ID_," + "	START_ACT_ID_," + "	END_ACT_ID_," + "	SUPER_PROCESS_INSTANCE_ID_,"
            + "	DELETE_REASON_," + "	TENANT_ID_," + "	NAME_," + "	CALLBACK_ID_," + "	CALLBACK_TYPE_" + ") SELECT" + "	ID_," + "	REV_," + "	PROC_INST_ID_," + "	BUSINESS_KEY_," + "	PROC_DEF_ID_," + "	START_TIME_," + "	END_TIME_," + "	DURATION_," + "	START_USER_ID_,"
            + "	START_ACT_ID_," + "	END_ACT_ID_," + "	SUPER_PROCESS_INSTANCE_ID_," + "	DELETE_REASON_," + "	TENANT_ID_," + "	NAME_," + "	CALLBACK_ID_," + "	CALLBACK_TYPE_" + " FROM" + "	ACT_HI_PROCINST RES" + " WHERE" + "	RES.PROC_INST_ID_ = '" + processInstanceId + "'";
        return sql;
    }

    private String getActHiTaskinstSql(String year, String processInstanceId) {
        String sql = "INSERT INTO ACT_HI_TASKINST_" + year + " (" + "	ID_," + "	REV_," + "	PROC_DEF_ID_," + "	TASK_DEF_ID_," + "	TASK_DEF_KEY_," + "	PROC_INST_ID_," + "	EXECUTION_ID_," + "	SCOPE_ID_," + "	SUB_SCOPE_ID_," + "	SCOPE_TYPE_," + "	SCOPE_DEFINITION_ID_," + "	PARENT_TASK_ID_,"
            + "	NAME_," + "	DESCRIPTION_," + "	OWNER_," + "	ASSIGNEE_," + "	START_TIME_," + "	CLAIM_TIME_," + "	END_TIME_," + "	DURATION_," + "	DELETE_REASON_," + "	PRIORITY_," + "	DUE_DATE_," + "	FORM_KEY_," + "	CATEGORY_," + "	TENANT_ID_," + "	LAST_UPDATED_TIME_" + " ) SELECT"
            + "	ID_," + "	REV_," + "	PROC_DEF_ID_," + "	TASK_DEF_ID_," + "	TASK_DEF_KEY_," + "	PROC_INST_ID_," + "	EXECUTION_ID_," + "	SCOPE_ID_," + "	SUB_SCOPE_ID_," + "	SCOPE_TYPE_," + "	SCOPE_DEFINITION_ID_," + "	PARENT_TASK_ID_," + "	NAME_," + "	DESCRIPTION_," + "	OWNER_,"
            + "	ASSIGNEE_," + "	START_TIME_," + "	CLAIM_TIME_," + "	END_TIME_," + "	DURATION_," + "	DELETE_REASON_," + "	PRIORITY_," + "	DUE_DATE_," + "	FORM_KEY_," + "	CATEGORY_," + "	TENANT_ID_," + "	LAST_UPDATED_TIME_" + " FROM" + "	ACT_HI_TASKINST T" + " WHERE"
            + "	T .PROC_INST_ID_ = '" + processInstanceId + "'";
        return sql;
    }

    private String getActHiVarinstSql(String year, String processInstanceId) {
        String sql3 = "INSERT INTO ACT_HI_VARINST_" + year + " (" + "	ID_," + "	REV_," + "	PROC_INST_ID_," + "	EXECUTION_ID_," + "	TASK_ID_," + "	NAME_," + "	VAR_TYPE_," + "	SCOPE_ID_," + "	SUB_SCOPE_ID_," + "	SCOPE_TYPE_," + "	BYTEARRAY_ID_," + "	DOUBLE_," + "	LONG_," + "	TEXT_,"
            + "	TEXT2_," + "	CREATE_TIME_," + "	LAST_UPDATED_TIME_" + " ) SELECT" + "	ID_," + "	REV_," + "	PROC_INST_ID_," + "	EXECUTION_ID_," + "	TASK_ID_," + "	NAME_," + "	VAR_TYPE_," + "	SCOPE_ID_," + "	SUB_SCOPE_ID_," + "	SCOPE_TYPE_," + "	BYTEARRAY_ID_," + "	DOUBLE_," + "	LONG_,"
            + "	TEXT_," + "	TEXT2_," + "	CREATE_TIME_," + "	LAST_UPDATED_TIME_" + " FROM" + "	ACT_HI_VARINST v" + " WHERE" + "	v.PROC_INST_ID_ = '" + processInstanceId + "' and v.NAME_ not in ('nrOfActiveInstances','nrOfCompletedInstances','nrOfInstances','loopCounter','elementUser')";
        return sql3;
    }

    @SuppressWarnings("unused")
    private String getffActRuExecutionSql(String year, String processInstanceId) {
        String sql3 = "INSERT INTO FF_ACT_RU_EXECUTION_" + year + " (" + "	ID_," + "	ACT_ID_," + "	IS_ACTIVE_," + "	BUSINESS_KEY_," + "	CACHED_ENT_STATE_," + "	CALLBACK_ID_," + "	CALLBACK_TYPE_," + "	IS_CONCURRENT_," + "	IS_COUNT_ENABLED_," + "	DEADLETTER_JOB_COUNT_,"
            + "	IS_EVENT_SCOPE_," + "	EVT_SUBSCR_COUNT_," + "	ID_LINK_COUNT_," + "	JOB_COUNT_," + "	LOCK_TIME_," + "	IS_MI_ROOT_," + "	NAME_," + "	PARENT_ID_," + "	PROC_DEF_ID_," + "	PROC_INST_ID_," + "	REV_," + "	ROOT_PROC_INST_ID_," + "	IS_SCOPE_," + "	START_ACT_ID_,"
            + "	START_TIME_," + "	START_USER_ID_," + "	SUPER_EXEC_," + "	SUSP_JOB_COUNT_," + "	SUSPENSION_STATE_," + "	TASK_COUNT_," + "	TENANT_ID_," + "	TIMER_JOB_COUNT_," + "	VAR_COUNT_" + " ) SELECT" + "	ID_," + "	ACT_ID_," + "	IS_ACTIVE_," + "	BUSINESS_KEY_,"
            + "	CACHED_ENT_STATE_," + "	CALLBACK_ID_," + "	CALLBACK_TYPE_," + "	IS_CONCURRENT_," + "	IS_COUNT_ENABLED_," + "	DEADLETTER_JOB_COUNT_," + "	IS_EVENT_SCOPE_," + "	EVT_SUBSCR_COUNT_," + "	ID_LINK_COUNT_," + "	JOB_COUNT_," + "	LOCK_TIME_," + "	IS_MI_ROOT_," + "	NAME_,"
            + "	PARENT_ID_," + "	PROC_DEF_ID_," + "	PROC_INST_ID_," + "	REV_," + "	ROOT_PROC_INST_ID_," + "	IS_SCOPE_," + "	START_ACT_ID_," + "	START_TIME_," + "	START_USER_ID_," + "	SUPER_EXEC_," + "	SUSP_JOB_COUNT_," + "	SUSPENSION_STATE_," + "	TASK_COUNT_," + "	TENANT_ID_,"
            + "	TIMER_JOB_COUNT_," + "	VAR_COUNT_" + " FROM" + "	FF_ACT_RU_EXECUTION_BACKUP F" + " WHERE" + "	F.PROC_INST_ID_ = '" + processInstanceId + "'";
        return sql3;
    }

    /**
     * 同步年度办结件至数据中心
     *
     * @param tenantId
     * @param request
     * @param response
     */
    @ResponseBody
    @RequestMapping(value = "/tongbu2DataCenter")
    public void tongbu2DataCenter(String tenantId, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> resMap = new HashMap<String, Object>(16);
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            String sql = "SELECT" + "	P .PROC_INST_ID_,TO_CHAR(P .START_TIME_,'yyyy-MM-dd HH:mi:ss') as START_TIME_," + " TO_CHAR(P .END_TIME_,'yyyy-MM-dd HH:mi:ss') as END_TIME_,P.PROC_DEF_ID_" + " FROM" + "	ACT_HI_PROCINST_2020 P" + " WHERE" + "	P .END_TIME_ IS NOT NULL"
                + " and P.DELETE_REASON_ is null" + " ORDER BY" + "	P .END_TIME_ DESC";
            DataSource dataSource = jdbcTemplate.getDataSource();
            DbMetaDataUtil dbMetaDataUtil = new DbMetaDataUtil();
            String dialectName = dbMetaDataUtil.getDatabaseDialectName(dataSource);
            if (DialectEnum.MYSQL.getValue().equals(dialectName)) {
                sql = "SELECT" + "	P .PROC_INST_ID_,SUBSTRING(P.START_TIME_,1,19) as START_TIME_,SUBSTRING(P .END_TIME_,1,19) as END_TIME_,P.PROC_DEF_ID_" + " FROM" + "	ACT_HI_PROCINST_2023 P" + " WHERE" + "	P .END_TIME_ IS NOT NULL" + " and P.DELETE_REASON_ is null" + " ORDER BY"
                    + "	P .END_TIME_ DESC";
            }
            List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
            LOGGER.info("*********************共{}条数据***************************", list.size());
            int i = 0;
            for (Map<String, Object> map : list) {
                String processInstanceId = "";
                try {
                    processInstanceId = (String)map.get("PROC_INST_ID_");
                    String processDefinitionId = (String)map.get("PROC_DEF_ID_");
                    String startTime = (String)map.get("START_TIME_");
                    String endTime = (String)map.get("END_TIME_");
                    ProcessParamModel processParamModel = processParamManager.findByProcessInstanceId(tenantId, processInstanceId);
                    OfficeDoneInfoModel officeDoneInfo = new OfficeDoneInfoModel();
                    officeDoneInfo.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
                    if (processParamModel != null) {
                        officeDoneInfo.setBureauId(StringUtils.isBlank(processParamModel.getBureauIds()) ? "" : processParamModel.getBureauIds());
                        officeDoneInfo.setDeptId(StringUtils.isBlank(processParamModel.getDeptIds()) ? "" : processParamModel.getDeptIds());
                        officeDoneInfo.setCreatUserId(StringUtils.isBlank(processParamModel.getStartor()) ? "" : processParamModel.getStartor());
                        officeDoneInfo.setCreatUserName(StringUtils.isBlank(processParamModel.getStartorName()) ? "" : processParamModel.getStartorName());
                        officeDoneInfo.setDocNumber(StringUtils.isBlank(processParamModel.getCustomNumber()) ? "" : processParamModel.getCustomNumber());
                        officeDoneInfo.setItemId(StringUtils.isBlank(processParamModel.getItemId()) ? "" : processParamModel.getItemId());
                        officeDoneInfo.setItemName(StringUtils.isBlank(processParamModel.getItemName()) ? "" : processParamModel.getItemName());
                        officeDoneInfo.setProcessSerialNumber(StringUtils.isBlank(processParamModel.getProcessSerialNumber()) ? "" : processParamModel.getProcessSerialNumber());
                        officeDoneInfo.setSystemCnName(StringUtils.isBlank(processParamModel.getSystemCnName()) ? "" : processParamModel.getSystemCnName());
                        officeDoneInfo.setSystemName(StringUtils.isBlank(processParamModel.getSystemName()) ? "" : processParamModel.getSystemName());
                        officeDoneInfo.setTitle(StringUtils.isBlank(processParamModel.getTitle()) ? "" : processParamModel.getTitle());
                        officeDoneInfo.setUrgency(StringUtils.isBlank(processParamModel.getCustomLevel()) ? "" : processParamModel.getCustomLevel());
                        officeDoneInfo.setUserComplete(StringUtils.isBlank(processParamModel.getCompleter()) ? "" : processParamModel.getCompleter());
                    }

                    // 处理委托人
                    // sql = "SELECT e.OWNERID from FF_ENTRUSTDETAIL e where e.PROCESSINSTANCEID = '" +
                    // processInstanceId + "'";
                    // List<Map<String, Object>> list2 = jdbcTemplate.queryForList(sql);
                    // String entrustUserId = "";
                    // for (Map<String, Object> m : list2) {
                    // String ownerId = (String)m.get("OWNERID");
                    // if (!entrustUserId.contains(ownerId)) {
                    // entrustUserId = Y9Util.genCustomStr(entrustUserId, ownerId);
                    // }
                    // }
                    // officeDoneInfo.setEntrustUserId(entrustUserId);

                    // 处理参与人
                    sql = "SELECT i.USER_ID_ from ACT_HI_IDENTITYLINK_2023 i where i.PROC_INST_ID_ = '" + processInstanceId + "'";
                    List<Map<String, Object>> list3 = jdbcTemplate.queryForList(sql);
                    String allUserId = "";
                    for (Map<String, Object> m : list3) {
                        String userId = (String)m.get("USER_ID_");
                        if (!allUserId.contains(userId)) {
                            allUserId = Y9Util.genCustomStr(allUserId, userId);
                        }
                    }
                    officeDoneInfo.setAllUserId(allUserId);
                    officeDoneInfo.setEndTime(endTime);
                    officeDoneInfo.setProcessDefinitionId(processDefinitionId);
                    officeDoneInfo.setProcessDefinitionKey(processDefinitionId.split(":")[0]);
                    officeDoneInfo.setProcessInstanceId(processInstanceId);
                    officeDoneInfo.setStartTime(startTime);
                    officeDoneInfo.setTenantId(tenantId);
                    officeDoneInfoManager.saveOfficeDone(tenantId, officeDoneInfo);
                } catch (Exception e) {
                    i = i + 1;
                    e.printStackTrace();
                }
            }
            LOGGER.info("********************同步失败{}条数据***************************", i);
            resMap.put("总数", list.size());
            resMap.put("同步失败", i);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(resMap));
    }

    /**
     * 同步办结件至数据中心
     *
     * @param tenantId
     * @param request
     * @param response
     */
    @ResponseBody
    @RequestMapping(value = "/tongbu2DataCenter0")
    public void tongbu2DataCenter0(String tenantId, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> resMap = new HashMap<String, Object>(16);
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            String sql = "SELECT" + "	P .PROC_INST_ID_,TO_CHAR(P .START_TIME_,'yyyy-MM-dd HH:mi:ss') as START_TIME_," + " TO_CHAR(P .END_TIME_,'yyyy-MM-dd HH:mi:ss') as END_TIME_,P.PROC_DEF_ID_" + " FROM" + "	ACT_HI_PROCINST P" + " WHERE" + "	P .END_TIME_ IS NOT NULL"
                + " and P.DELETE_REASON_ is null" + " ORDER BY" + "	P .END_TIME_ DESC";
            DataSource dataSource = jdbcTemplate.getDataSource();
            DbMetaDataUtil dbMetaDataUtil = new DbMetaDataUtil();
            String dialectName = dbMetaDataUtil.getDatabaseDialectName(dataSource);
            if (DialectEnum.MYSQL.getValue().equals(dialectName)) {
                sql = "SELECT" + "	P .PROC_INST_ID_,SUBSTRING(P.START_TIME_,1,19) as START_TIME_,SUBSTRING(P .END_TIME_,1,19) as END_TIME_,P.PROC_DEF_ID_" + " FROM" + "	ACT_HI_PROCINST P" + " WHERE" + "	P .END_TIME_ IS NOT NULL" + " and P.DELETE_REASON_ is null" + " ORDER BY"
                    + "	P .END_TIME_ DESC";
            }
            List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
            LOGGER.info("*********************共{}条数据***************************", list.size());
            int i = 0;
            for (Map<String, Object> map : list) {
                String processInstanceId = "";
                try {
                    processInstanceId = (String)map.get("PROC_INST_ID_");
                    String processDefinitionId = (String)map.get("PROC_DEF_ID_");
                    String startTime = (String)map.get("START_TIME_");
                    String endTime = (String)map.get("END_TIME_");
                    ProcessParamModel processParamModel = processParamManager.findByProcessInstanceId(tenantId, processInstanceId);
                    OfficeDoneInfoModel officeDoneInfo = new OfficeDoneInfoModel();
                    officeDoneInfo.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
                    if (processParamModel != null) {
                        officeDoneInfo.setBureauId(StringUtils.isBlank(processParamModel.getBureauIds()) ? "" : processParamModel.getBureauIds());
                        officeDoneInfo.setDeptId(StringUtils.isBlank(processParamModel.getDeptIds()) ? "" : processParamModel.getDeptIds());
                        officeDoneInfo.setCreatUserId(StringUtils.isBlank(processParamModel.getStartor()) ? "" : processParamModel.getStartor());
                        officeDoneInfo.setCreatUserName(StringUtils.isBlank(processParamModel.getStartorName()) ? "" : processParamModel.getStartorName());
                        officeDoneInfo.setDocNumber(StringUtils.isBlank(processParamModel.getCustomNumber()) ? "" : processParamModel.getCustomNumber());
                        officeDoneInfo.setItemId(StringUtils.isBlank(processParamModel.getItemId()) ? "" : processParamModel.getItemId());
                        officeDoneInfo.setItemName(StringUtils.isBlank(processParamModel.getItemName()) ? "" : processParamModel.getItemName());
                        officeDoneInfo.setProcessSerialNumber(StringUtils.isBlank(processParamModel.getProcessSerialNumber()) ? "" : processParamModel.getProcessSerialNumber());
                        officeDoneInfo.setSystemCnName(StringUtils.isBlank(processParamModel.getSystemCnName()) ? "" : processParamModel.getSystemCnName());
                        officeDoneInfo.setSystemName(StringUtils.isBlank(processParamModel.getSystemName()) ? "" : processParamModel.getSystemName());
                        officeDoneInfo.setTitle(StringUtils.isBlank(processParamModel.getTitle()) ? "" : processParamModel.getTitle());
                        officeDoneInfo.setUrgency(StringUtils.isBlank(processParamModel.getCustomLevel()) ? "" : processParamModel.getCustomLevel());
                        officeDoneInfo.setUserComplete(StringUtils.isBlank(processParamModel.getCompleter()) ? "" : processParamModel.getCompleter());
                    }

                    // 处理委托人
                    sql = "SELECT e.OWNERID from FF_ENTRUSTDETAIL e where e.PROCESSINSTANCEID = '" + processInstanceId + "'";
                    List<Map<String, Object>> list2 = jdbcTemplate.queryForList(sql);
                    String entrustUserId = "";
                    for (Map<String, Object> m : list2) {
                        String ownerId = (String)m.get("OWNERID");
                        if (!entrustUserId.contains(ownerId)) {
                            entrustUserId = Y9Util.genCustomStr(entrustUserId, ownerId);
                        }
                    }
                    officeDoneInfo.setEntrustUserId(entrustUserId);

                    // 处理参与人
                    sql = "SELECT i.USER_ID_ from ACT_HI_IDENTITYLINK i where i.PROC_INST_ID_ = '" + processInstanceId + "'";
                    List<Map<String, Object>> list3 = jdbcTemplate.queryForList(sql);
                    String allUserId = "";
                    for (Map<String, Object> m : list3) {
                        String userId = (String)m.get("USER_ID_");
                        if (!allUserId.contains(userId)) {
                            allUserId = Y9Util.genCustomStr(allUserId, userId);
                        }
                    }
                    officeDoneInfo.setAllUserId(allUserId);
                    officeDoneInfo.setEndTime(endTime);
                    officeDoneInfo.setProcessDefinitionId(processDefinitionId);
                    officeDoneInfo.setProcessDefinitionKey(processDefinitionId.split(":")[0]);
                    officeDoneInfo.setProcessInstanceId(processInstanceId);
                    officeDoneInfo.setStartTime(startTime);
                    officeDoneInfo.setTenantId(tenantId);
                    officeDoneInfoManager.saveOfficeDone(tenantId, officeDoneInfo);
                } catch (Exception e) {
                    i = i + 1;
                    e.printStackTrace();
                }
            }
            LOGGER.info("********************同步失败{}条数据***************************", i);
            resMap.put("总数", list.size());
            resMap.put("同步失败", i);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(resMap));
    }

    /**
     * 结转数据至年度表
     *
     * @param tenantId
     * @param request
     * @param response
     */
    @ResponseBody
    @RequestMapping(value = "/tongbu2YearTable")
    public void tongbu2YearTable(String tenantId, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> resMap = new HashMap<String, Object>(16);
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            String sql = "SELECT" + "	P .PROC_INST_ID_,TO_CHAR(P .START_TIME_,'yyyy-MM-dd HH:mi:ss') as START_TIME_" + " FROM" + "	ACT_HI_PROCINST P" + " WHERE" + "	P .END_TIME_ IS NOT NULL" + " and P.DELETE_REASON_ is null" + " ORDER BY" + "	P .END_TIME_ DESC";
            DataSource dataSource = jdbcTemplate.getDataSource();
            DbMetaDataUtil dbMetaDataUtil = new DbMetaDataUtil();
            String dialectName = dbMetaDataUtil.getDatabaseDialectName(dataSource);
            if (dialectName.equals(DialectEnum.MYSQL.getValue())) {
                sql = "SELECT" + "	P .PROC_INST_ID_,SUBSTRING(P.START_TIME_,1,19) as START_TIME_" + " FROM" + "	ACT_HI_PROCINST P" + " WHERE" + "	P .END_TIME_ IS NOT NULL" + " and P.DELETE_REASON_ is null" + " ORDER BY" + "	P .END_TIME_ DESC";
            }
            List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
            LOGGER.info("*********************共{}条数据***************************", list.size());
            int i = 0;
            for (Map<String, Object> map : list) {
                String processInstanceId = "";
                try {
                    processInstanceId = (String)map.get("PROC_INST_ID_");
                    String startTime = (String)map.get("START_TIME_");
                    String year = startTime.substring(0, 4);

                    String sql3 = getActHiTaskinstSql(year, processInstanceId);
                    // 同步历史任务
                    jdbcTemplate.execute(sql3);

                    sql3 = getActHiVarinstSql(year, processInstanceId);
                    // 同步历史变量
                    jdbcTemplate.execute(sql3);

                    sql3 = getActGeBytearraySql(year, processInstanceId);
                    // 同步二进制数据表
                    jdbcTemplate.execute(sql3);

                    sql3 = getActHiIdentiyLinkSql(year, processInstanceId);
                    // 同步历史参与人
                    jdbcTemplate.execute(sql3);

                    sql3 = getActHiActinstSql(year, processInstanceId);
                    // 同步历史节点
                    jdbcTemplate.execute(sql3);

                    // sql3 = getffActRuExecutionSql(year,PROC_INST_ID_);
                    // jdbcTemplate.execute(sql3);//同步备份执行实例

                    sql3 = getActHiProcinstSql(year, processInstanceId);
                    // 同步流程实例
                    jdbcTemplate.execute(sql3);

                } catch (Exception e) {
                    i = i + 1;
                    e.printStackTrace();
                }
            }
            LOGGER.info("********************同步失败{}条数据***************************", i);
            resMap.put("总数", list.size());
            resMap.put("同步失败", i);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(resMap));
    }
}
