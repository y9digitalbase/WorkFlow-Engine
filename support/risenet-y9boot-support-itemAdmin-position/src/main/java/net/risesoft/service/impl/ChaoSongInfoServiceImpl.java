package net.risesoft.service.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.api.org.DepartmentApi;
import net.risesoft.api.org.OrganizationApi;
import net.risesoft.api.org.PositionApi;
import net.risesoft.api.processadmin.HistoricProcessApi;
import net.risesoft.api.processadmin.TaskApi;
import net.risesoft.api.sms.SmsHttpApi;
import net.risesoft.api.todo.TodoTaskApi;
import net.risesoft.consts.UtilConsts;
import net.risesoft.entity.ErrorLog;
import net.risesoft.entity.ProcessParam;
import net.risesoft.enums.ItemBoxTypeEnum;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.model.platform.Department;
import net.risesoft.model.platform.OrgUnit;
import net.risesoft.model.platform.Position;
import net.risesoft.model.itemadmin.ErrorLogModel;
import net.risesoft.model.processadmin.HistoricProcessInstanceModel;
import net.risesoft.model.processadmin.TaskModel;
import net.risesoft.nosql.elastic.entity.ChaoSongInfo;
import net.risesoft.nosql.elastic.entity.OfficeDoneInfo;
import net.risesoft.nosql.elastic.repository.ChaoSongInfoRepository;
import net.risesoft.service.AsyncHandleService;
import net.risesoft.service.ChaoSongInfoService;
import net.risesoft.service.DocumentService;
import net.risesoft.service.ErrorLogService;
import net.risesoft.service.OfficeDoneInfoService;
import net.risesoft.service.OfficeFollowService;
import net.risesoft.service.ProcessParamService;
import net.risesoft.service.SpmApproveItemService;
import net.risesoft.util.SysVariables;
import net.risesoft.util.Y9EsIndexConst;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.configuration.Y9Properties;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/20
 */
@Service(value = "chaoSongInfoService")
@Slf4j
@RequiredArgsConstructor
public class ChaoSongInfoServiceImpl implements ChaoSongInfoService {

    private static IndexCoordinates INDEX = IndexCoordinates.of(Y9EsIndexConst.CHAONSONG_INFO);

    @Autowired
    private ChaoSongInfoRepository chaoSongInfoRepository;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private SpmApproveItemService spmApproveitemService;

    @Autowired
    private ProcessParamService processParamService;

    @Autowired
    private TaskApi taskManager;

    @Autowired
    private HistoricProcessApi historicProcessManager;

    @Autowired
    private DepartmentApi departmentManager;

    @Autowired
    private OrganizationApi organizationManager;

    @Autowired
    private PositionApi positionManager;

    @Autowired
    private SmsHttpApi smsHttpManager;

    @Autowired
    private OfficeDoneInfoService officeDoneInfoService;

    @Autowired
    private Y9Properties y9Conf;

    @Autowired
    private OfficeFollowService officeFollowService;

    @Autowired
    private AsyncHandleService asyncHandleService;

    @Autowired
    private ErrorLogService errorLogService;

    private final ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private TodoTaskApi todoTaskManager;

    @Override
    @Transactional(readOnly = false)
    public void changeChaoSongState(String id, String type) {
        String opinionState = "";
        if (ItemBoxTypeEnum.ADD.getValue().equals(type)) {
            opinionState = "1";
        }
        ChaoSongInfo chaoSongInfo = chaoSongInfoRepository.findById(id).orElse(null);
        chaoSongInfo.setOpinionState(opinionState);
        chaoSongInfoRepository.save(chaoSongInfo);
    }

    @Override
    public void changeStatus(String id) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ChaoSongInfo chaoSong = chaoSongInfoRepository.findById(id).orElse(null);
        if (chaoSong != null) {
            chaoSong.setStatus(1);
            chaoSong.setReadTime(sdf.format(new Date()));
            chaoSongInfoRepository.save(chaoSong);
            try {
                todoTaskManager.deleteTodoTask(Y9LoginUserHolder.getTenantId(), id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void changeStatus(String[] ids) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (String id : ids) {
            ChaoSongInfo chaoSong = chaoSongInfoRepository.findById(id).orElse(null);
            if (chaoSong != null) {
                chaoSong.setStatus(1);
                chaoSong.setReadTime(sdf.format(new Date()));
                chaoSongInfoRepository.save(chaoSong);
            }
            try {
                todoTaskManager.deleteTodoTask(Y9LoginUserHolder.getTenantId(), id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int countByProcessInstanceId(String userId, String processInstanceId) {
        return chaoSongInfoRepository.countBySenderIdIsNotAndProcessInstanceId(userId, processInstanceId);
    }

    @Override
    public int countByUserIdAndProcessInstanceId(String userId, String processInstanceId) {
        return chaoSongInfoRepository.countBySenderIdAndProcessInstanceId(userId, processInstanceId);
    }

    @Override
    public void deleteById(String id) {
        chaoSongInfoRepository.deleteById(id);
    }

    @Override
    public void deleteByIds(String[] ids) {
        for (String id : ids) {
            chaoSongInfoRepository.deleteById(id);
            try {
                todoTaskManager.deleteTodoTask(Y9LoginUserHolder.getTenantId(), id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    @Transactional(readOnly = false)
    public boolean deleteByProcessInstanceId(String processInstanceId) {
        try {
            chaoSongInfoRepository.deleteByProcessInstanceIdAndTenantId(processInstanceId,
                Y9LoginUserHolder.getTenantId());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Map<String, Object> detail(String processInstanceId, Integer status, boolean mobile) {
        Map<String, Object> returnMap = new HashMap<>(16);
        String tenantId = Y9LoginUserHolder.getTenantId();
        String itembox = ItemBoxTypeEnum.DOING.getValue(), taskId = "";
        List<TaskModel> taskList = taskManager.findByProcessInstanceId(tenantId, processInstanceId);
        if (taskList.size() <= 0) {
            itembox = ItemBoxTypeEnum.DONE.getValue();
        }
        if (ItemBoxTypeEnum.DOING.getValue().equals(itembox)) {
            taskId = taskList.get(0).getId();
            TaskModel task = taskManager.findById(tenantId, taskId);
            processInstanceId = task.getProcessInstanceId();
        }
        String processSerialNumber = "", processDefinitionId = "", taskDefinitionKey = "", processDefinitionKey = "",
            activitiUser = "";
        String itemboxStr = itembox;
        String startor = "";
        ProcessParam processParam = processParamService.findByProcessInstanceId(processInstanceId);
        HistoricProcessInstanceModel hpi = historicProcessManager.getById(tenantId, processInstanceId);
        if (hpi == null) {
            OfficeDoneInfo officeDoneInfo = officeDoneInfoService.findByProcessInstanceId(processInstanceId);
            if (officeDoneInfo == null) {
                String year = processParam.getCreateTime().substring(0, 4);
                hpi = historicProcessManager.getByIdAndYear(tenantId, processInstanceId, year);
                processDefinitionId = hpi.getProcessDefinitionId();
                processDefinitionKey = processDefinitionId.split(SysVariables.COLON)[0];
            } else {
                processDefinitionId = officeDoneInfo.getProcessDefinitionId();
                processDefinitionKey = officeDoneInfo.getProcessDefinitionKey();
            }
        } else {
            processDefinitionId = hpi.getProcessDefinitionId();
            processDefinitionKey = processDefinitionId.split(SysVariables.COLON)[0];
        }
        startor = processParam.getStartor();
        processSerialNumber = processParam.getProcessSerialNumber();
        if (StringUtils.isNotEmpty(taskId)) {
            if (taskId.contains(SysVariables.COMMA)) {
                taskId = taskId.split(SysVariables.COMMA)[0];
            }
            TaskModel taskTemp = taskManager.findById(tenantId, taskId);
            taskDefinitionKey = taskTemp.getTaskDefinitionKey();
        }
        Position position = positionManager.getPosition(tenantId, Y9LoginUserHolder.getPositionId()).getData();
        returnMap.put("title", processParam.getTitle());
        returnMap.put("startor", startor);
        returnMap.put("itembox", itembox);
        returnMap.put("control", itemboxStr);
        returnMap.put("currentUser", position.getName());
        returnMap.put(SysVariables.PROCESSSERIALNUMBER, processSerialNumber);
        returnMap.put("processDefinitionKey", processDefinitionKey);
        returnMap.put("processDefinitionId", processDefinitionId);
        returnMap.put("processInstanceId", processInstanceId);
        returnMap.put("taskDefKey", taskDefinitionKey);
        returnMap.put("taskId", taskId);
        returnMap.put(SysVariables.ACTIVITIUSER, activitiUser);
        returnMap = spmApproveitemService.findById(processParam.getItemId(), returnMap);
        returnMap = documentService.genDocumentModel(processParam.getItemId(), processDefinitionKey,
            processDefinitionId, taskDefinitionKey, mobile, returnMap);
        String menuName = "打印,抄送,关注,返回";
        String menuKey = "17,18,follow,03";
        if (status == 1) {
            menuName = "打印,抄送,关注,返回";
            menuKey = "17,18,follow,03";
        }
        returnMap.put("menuName", menuName);
        returnMap.put("menuKey", menuKey);
        return returnMap;
    }

    @Override
    public ChaoSongInfo findOne(String id) {
        return chaoSongInfoRepository.findById(id).orElse(null);
    }

    @Override
    public int getAllCountByUserId(String userId) {
        return chaoSongInfoRepository.countByUserIdAndTenantId(userId, Y9LoginUserHolder.getTenantId());
    }

    @Override
    public Map<String, Object> getAllList(String positionId, String documentTitle, int rows, int page) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        Map<String, Object> retMap = new HashMap<String, Object>(16);
        if (page < 1) {
            page = 1;
        }
        int totalPages = 1;
        long total = 0;
        Pageable pageable = PageRequest.of((page < 1) ? 0 : page - 1, rows, Sort.by(Sort.Direction.DESC, "createTime"));
        Criteria criteria = new Criteria();
        criteria.and("tenantId").is(Y9LoginUserHolder.getTenantId());
        criteria.and("userId").is(positionId);
        if (StringUtils.isNotBlank(documentTitle)) {
            criteria.and("title").contains(documentTitle);
        }
        Query query = new CriteriaQuery(criteria).setPageable(pageable);
        SearchHits<ChaoSongInfo> searchHits = elasticsearchTemplate.search(query, ChaoSongInfo.class, INDEX);
        List<ChaoSongInfo> csList = searchHits.stream()
            .map(org.springframework.data.elasticsearch.core.SearchHit::getContent).collect(Collectors.toList());
        totalPages = (int)searchHits.getTotalHits() / rows;
        totalPages = searchHits.getTotalHits() % rows == 0 ? totalPages : totalPages + 1;
        total = searchHits.getTotalHits();
        SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int num = (page - 1) * rows;
        HistoricProcessInstanceModel hpi = null;
        ProcessParam processParam = null;
        List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
        for (ChaoSongInfo cs : csList) {
            Map<String, Object> map = new HashMap<String, Object>(16);
            map.put("id", cs.getId());
            try {
                String processInstanceId = cs.getProcessInstanceId();
                map.put("createTime", sdf.format(sdf1.parse(cs.getCreateTime())));
                processParam = processParamService.findByProcessInstanceId(processInstanceId);
                map.put("processInstanceId", processInstanceId);
                map.put("senderName", cs.getSenderName());
                map.put("sendDeptId", cs.getSendDeptId());
                map.put("sendDeptName", cs.getSendDeptName());
                map.put("readTime", sdf.format(sdf1.parse(cs.getReadTime())));
                map.put("title", processParam.getTitle());
                map.put("status", cs.getStatus());
                map.put("banjie", false);
                map.put("itemId", cs.getItemId());
                map.put("itemName", cs.getItemName());
                map.put("processSerialNumber", processParam.getProcessSerialNumber());
                map.put("number", processParam.getCustomNumber());
                map.put("level", processParam.getCustomLevel());
                int chaosongNum =
                    chaoSongInfoRepository.countBySenderIdAndProcessInstanceId(positionId, processInstanceId);
                map.put("chaosongNum", chaosongNum);
                hpi = historicProcessManager.getById(tenantId, processInstanceId);
                boolean banjie = hpi == null || (hpi != null && hpi.getEndTime() != null);
                if (banjie) {
                    map.put("banjie", true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            map.put("serialNumber", num + 1);
            num += 1;
            listMap.add(map);
        }
        retMap.put("currpage", page);
        retMap.put("totalpages", totalPages);
        retMap.put("total", total);
        retMap.put("rows", listMap);
        return retMap;
    }

    @Override
    public int getDone4OpinionCountByUserId(String userId) {
        return chaoSongInfoRepository.countByUserIdAndOpinionStateAndTenantId(userId, "1",
            Y9LoginUserHolder.getTenantId());
    }

    @Override
    public int getDoneCountByUserId(String userId) {
        return chaoSongInfoRepository.countByUserIdAndStatus(userId, 1);
    }

    @Override
    public Map<String, Object> getDoneList(String positionId, String documentTitle, int rows, int page) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        Map<String, Object> retMap = new HashMap<String, Object>(16);
        if (page < 1) {
            page = 1;
        }
        int totalPages = 1;
        long total = 0;
        Pageable pageable = PageRequest.of((page < 1) ? 0 : page - 1, rows, Sort.by(Sort.Direction.DESC, "createTime"));
        Criteria criteria = new Criteria();
        criteria.and("tenantId").is(Y9LoginUserHolder.getTenantId());
        criteria.and("userId").is(positionId);
        criteria.and("status").is(1);
        if (StringUtils.isNotBlank(documentTitle)) {
            criteria.and("title").contains(documentTitle);
        }
        Query query = new CriteriaQuery(criteria).setPageable(pageable);
        SearchHits<ChaoSongInfo> searchHits = elasticsearchTemplate.search(query, ChaoSongInfo.class, INDEX);
        List<ChaoSongInfo> csList = searchHits.stream()
            .map(org.springframework.data.elasticsearch.core.SearchHit::getContent).collect(Collectors.toList());
        totalPages = (int)searchHits.getTotalHits() / rows;
        totalPages = searchHits.getTotalHits() % rows == 0 ? totalPages : totalPages + 1;
        total = searchHits.getTotalHits();
        SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int num = (page - 1) * rows;
        HistoricProcessInstanceModel hpi = null;
        ProcessParam processParam = null;
        List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
        for (ChaoSongInfo cs : csList) {
            Map<String, Object> map = new HashMap<String, Object>(16);
            map.put("id", cs.getId());
            try {
                String processInstanceId = cs.getProcessInstanceId();
                map.put("createTime", sdf.format(sdf1.parse(cs.getCreateTime())));
                processParam = processParamService.findByProcessInstanceId(processInstanceId);
                map.put("processInstanceId", processInstanceId);
                map.put("senderName", cs.getSenderName());
                map.put("sendDeptId", cs.getSendDeptId());
                map.put("sendDeptName", cs.getSendDeptName());
                map.put("readTime", sdf.format(sdf1.parse(cs.getReadTime())));
                map.put("title", processParam.getTitle());
                map.put("status", cs.getStatus());
                map.put("banjie", false);
                map.put("itemId", cs.getItemId());
                map.put("itemName", cs.getItemName());
                map.put("processSerialNumber", processParam.getProcessSerialNumber());
                map.put("number", processParam.getCustomNumber());
                map.put("level", processParam.getCustomLevel());
                int chaosongNum =
                    chaoSongInfoRepository.countBySenderIdAndProcessInstanceId(positionId, processInstanceId);
                map.put("chaosongNum", chaosongNum);
                hpi = historicProcessManager.getById(tenantId, processInstanceId);
                boolean banjie = hpi == null || (hpi != null && hpi.getEndTime() != null);
                if (banjie) {
                    map.put("banjie", true);
                }
                int countFollow = officeFollowService.countByProcessInstanceId(processInstanceId);
                map.put("follow", countFollow > 0 ? true : false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            map.put("serialNumber", num + 1);
            num += 1;
            listMap.add(map);
        }
        retMap.put("currpage", page);
        retMap.put("totalpages", totalPages);
        retMap.put("total", total);
        retMap.put("rows", listMap);
        return retMap;
    }

    @Override
    public Map<String, Object> getListByProcessInstanceId(String processInstanceId, String userName, int rows,
        int page) {
        Map<String, Object> retMap = new HashMap<String, Object>(16);
        String senderId = Y9LoginUserHolder.getPositionId();
        List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
        if (page < 1) {
            page = 1;
        }
        int totalPages = 1;
        long total = 0;
        Pageable pageable = PageRequest.of((page < 1) ? 0 : page - 1, rows, Sort.by(Sort.Direction.DESC, "createTime"));
        Criteria criteria = new Criteria();
        criteria.and("tenantId").is(Y9LoginUserHolder.getTenantId());
        criteria.and("senderId").notIn(senderId);
        criteria.and("processInstanceId").is(processInstanceId);
        if (StringUtils.isNotBlank(userName)) {
            criteria.and("userName").contains(userName);
        }
        Query query = new CriteriaQuery(criteria).setPageable(pageable);
        SearchHits<ChaoSongInfo> searchHits = elasticsearchTemplate.search(query, ChaoSongInfo.class, INDEX);
        List<ChaoSongInfo> csList = searchHits.stream()
            .map(org.springframework.data.elasticsearch.core.SearchHit::getContent).collect(Collectors.toList());
        totalPages = (int)searchHits.getTotalHits() / rows;
        totalPages = searchHits.getTotalHits() % rows == 0 ? totalPages : totalPages + 1;
        total = searchHits.getTotalHits();
        SimpleDateFormat sdf0 = new SimpleDateFormat("yy/MM/dd HH:mm");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int startRow = (page - 1) * rows;
        for (ChaoSongInfo info : csList) {
            Map<String, Object> map = new HashMap<String, Object>(16);
            map.put("id", info.getId());
            try {
                map.put("createTime", sdf0.format(sdf1.parse(info.getCreateTime())));
                map.put("processInstanceId", processInstanceId);
                map.put("senderName", info.getSenderName());
                map.put("sendDeptId", info.getSendDeptId());
                map.put("sendDeptName", info.getSendDeptName());
                if (StringUtils.isBlank(info.getReadTime())) {
                    map.put("readTime", "");
                } else {
                    map.put("readTime", sdf0.format(sdf1.parse(info.getReadTime())));
                }
                map.put("userName", info.getUserName());
                map.put("userDeptName", info.getUserDeptName());
                map.put("title", info.getTitle());
            } catch (Exception e) {
                e.printStackTrace();
            }
            map.put("serialNumber", startRow + 1);
            startRow += 1;
            listMap.add(map);
        }
        // 获取总页数
        retMap.put("currpage", page);
        retMap.put("totalpages", totalPages);
        retMap.put("total", total);
        retMap.put("rows", listMap);
        return retMap;
    }

    @Override
    public Map<String, Object> getListBySenderIdAndProcessInstanceId(String senderId, String processInstanceId,
        String userName, int rows, int page) {
        Map<String, Object> retMap = new HashMap<String, Object>(16);
        List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
        if (page < 1) {
            page = 1;
        }
        int totalPages = 1;
        long total = 0;
        Pageable pageable = PageRequest.of((page < 1) ? 0 : page - 1, rows, Sort.by(Sort.Direction.DESC, "createTime"));
        Criteria criteria = new Criteria();
        criteria.and("tenantId").is(Y9LoginUserHolder.getTenantId());
        criteria.and("senderId").is(senderId);
        criteria.and("processInstanceId").is(processInstanceId);
        if (StringUtils.isNotBlank(userName)) {
            criteria.and("userName").contains(userName);
        }
        Query query = new CriteriaQuery(criteria).setPageable(pageable);
        SearchHits<ChaoSongInfo> searchHits = elasticsearchTemplate.search(query, ChaoSongInfo.class, INDEX);
        List<ChaoSongInfo> csList = searchHits.stream()
            .map(org.springframework.data.elasticsearch.core.SearchHit::getContent).collect(Collectors.toList());
        totalPages = (int)searchHits.getTotalHits() / rows;
        totalPages = searchHits.getTotalHits() % rows == 0 ? totalPages : totalPages + 1;
        total = searchHits.getTotalHits();
        SimpleDateFormat sdf0 = new SimpleDateFormat("yy/MM/dd HH:mm");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int startRow = (page - 1) * rows;
        for (ChaoSongInfo cs : csList) {
            Map<String, Object> map = new HashMap<String, Object>(16);
            map.put("id", cs.getId());
            try {
                map.put("createTime", sdf0.format(sdf1.parse(cs.getCreateTime())));
                map.put("processInstanceId", processInstanceId);
                map.put("senderName", cs.getSenderName());
                map.put("sendDeptId", cs.getSendDeptId());
                map.put("sendDeptName", cs.getSendDeptName());
                if (StringUtils.isBlank(cs.getReadTime())) {
                    map.put("readTime", "");
                } else {
                    map.put("readTime", sdf0.format(sdf1.parse(cs.getReadTime())));
                }
                map.put("userName", cs.getUserName());
                map.put("userDeptName", cs.getUserDeptName());
                map.put("title", cs.getTitle());
            } catch (Exception e) {
                e.printStackTrace();
            }
            map.put("serialNumber", startRow + 1);
            startRow += 1;
            listMap.add(map);
        }
        // 获取总页数
        retMap.put("currpage", page);
        retMap.put("totalpages", totalPages);
        retMap.put("total", total);
        retMap.put("rows", listMap);
        return retMap;
    }

    @Override
    public Map<String, Object> getOpinionChaosongByUserId(String userId, String documentTitle, int rows, int page) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        Map<String, Object> retMap = new HashMap<String, Object>(16);
        if (page < 1) {
            page = 1;
        }
        List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
        int totalPages = 1;
        long total = 0;
        Pageable pageable = PageRequest.of((page < 1) ? 0 : page - 1, rows, Sort.by(Sort.Direction.DESC, "createTime"));
        Criteria criteria = new Criteria();
        criteria.and("tenantId").is(Y9LoginUserHolder.getTenantId());
        criteria.and("userId").is(userId);
        criteria.and("opinionState").is("1");
        if (StringUtils.isNotBlank(documentTitle)) {
            criteria.and("title").contains(documentTitle);
        }
        Query query = new CriteriaQuery(criteria).setPageable(pageable);
        SearchHits<ChaoSongInfo> searchHits = elasticsearchTemplate.search(query, ChaoSongInfo.class, INDEX);
        List<ChaoSongInfo> csList = searchHits.stream()
            .map(org.springframework.data.elasticsearch.core.SearchHit::getContent).collect(Collectors.toList());
        totalPages = (int)searchHits.getTotalHits() / rows;
        totalPages = searchHits.getTotalHits() % rows == 0 ? totalPages : totalPages + 1;
        total = searchHits.getTotalHits();
        SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        HistoricProcessInstanceModel hpi = null;
        ProcessParam processParam = null;
        int num = (page - 1) * rows;
        for (ChaoSongInfo cs : csList) {
            Map<String, Object> map = new HashMap<String, Object>(16);
            map.put("id", cs.getId());
            try {
                String processInstanceId = cs.getProcessInstanceId();
                map.put("createTime", sdf.format(sdf1.parse(cs.getCreateTime())));
                processParam = processParamService.findByProcessInstanceId(processInstanceId);
                map.put("processInstanceId", processInstanceId);
                map.put("senderName", cs.getSenderName());
                map.put("sendDeptId", cs.getSendDeptId());
                map.put("sendDeptName", cs.getSendDeptName());
                map.put("readTime", sdf.format(sdf1.parse(cs.getReadTime())));
                map.put("title", processParam.getTitle());
                map.put("status", cs.getStatus());
                map.put("banjie", false);
                map.put("itemId", cs.getItemId());
                map.put("itemName", cs.getItemName());
                map.put("processSerialNumber", processParam.getProcessSerialNumber());
                map.put("number", processParam.getCustomNumber());
                map.put("level", processParam.getCustomLevel());
                int chaosongNum = chaoSongInfoRepository.countBySenderIdAndProcessInstanceId(userId, processInstanceId);
                map.put("chaosongNum", chaosongNum);
                hpi = historicProcessManager.getById(tenantId, processInstanceId);
                boolean banjie = hpi == null || (hpi != null && hpi.getEndTime() != null);
                if (banjie) {
                    map.put("banjie", true);
                }
                int countFollow = officeFollowService.countByProcessInstanceId(processInstanceId);
                map.put("follow", countFollow > 0 ? true : false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            map.put("serialNumber", num + 1);
            num += 1;
            listMap.add(map);
        }
        // 获取总页数
        retMap.put("currpage", page);
        retMap.put("totalpages", totalPages);
        retMap.put("total", total);
        retMap.put("rows", listMap);
        return retMap;
    }

    @Override
    public int getTodoCountByUserId(String userId) {
        return chaoSongInfoRepository.countByUserIdAndStatus(userId, 2);
    }

    @Override
    public Map<String, Object> getTodoList(String positionId, String documentTitle, int rows, int page) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        Map<String, Object> retMap = new HashMap<String, Object>(16);
        List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
        if (page < 1) {
            page = 1;
        }
        int totalPages = 1;
        long total = 0;
        Pageable pageable = PageRequest.of((page < 1) ? 0 : page - 1, rows, Sort.by(Sort.Direction.DESC, "createTime"));
        Criteria criteria = new Criteria();
        criteria.and("tenantId").is(Y9LoginUserHolder.getTenantId());
        criteria.and("userId").is(positionId);
        criteria.and("status").is(2);
        if (StringUtils.isNotBlank(documentTitle)) {
            criteria.and("title").contains(documentTitle);
        }
        Query query = new CriteriaQuery(criteria).setPageable(pageable);
        SearchHits<ChaoSongInfo> searchHits = elasticsearchTemplate.search(query, ChaoSongInfo.class, INDEX);
        List<ChaoSongInfo> csList = searchHits.stream()
            .map(org.springframework.data.elasticsearch.core.SearchHit::getContent).collect(Collectors.toList());
        totalPages = (int)searchHits.getTotalHits() / rows;
        totalPages = searchHits.getTotalHits() % rows == 0 ? totalPages : totalPages + 1;
        total = searchHits.getTotalHits();
        SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int num = (page - 1) * rows;
        HistoricProcessInstanceModel hpi = null;
        ProcessParam processParam = null;
        for (ChaoSongInfo cs : csList) {
            Map<String, Object> map = new HashMap<String, Object>(16);
            map.put("id", cs.getId());
            try {
                String processInstanceId = cs.getProcessInstanceId();
                map.put("createTime", sdf.format(sdf1.parse(cs.getCreateTime())));
                processParam = processParamService.findByProcessInstanceId(processInstanceId);
                map.put("processInstanceId", processInstanceId);
                map.put("senderName", cs.getSenderName());
                map.put("sendDeptId", cs.getSendDeptId());
                map.put("sendDeptName", cs.getSendDeptName());
                map.put("readTime", cs.getReadTime());
                map.put("title", processParam.getTitle());
                map.put("status", cs.getStatus());
                map.put("banjie", false);
                map.put("itemId", cs.getItemId());
                map.put("itemName", cs.getItemName());
                map.put("processSerialNumber", processParam.getProcessSerialNumber());
                map.put("number", processParam.getCustomNumber());
                map.put("level", processParam.getCustomLevel());
                int chaosongNum =
                    chaoSongInfoRepository.countBySenderIdAndProcessInstanceId(positionId, processInstanceId);
                map.put("chaosongNum", chaosongNum);
                hpi = historicProcessManager.getById(tenantId, processInstanceId);
                boolean banjie = hpi == null || (hpi != null && hpi.getEndTime() != null);
                if (banjie) {
                    map.put("banjie", true);
                }
                int countFollow = officeFollowService.countByProcessInstanceId(processInstanceId);
                map.put("follow", countFollow > 0 ? true : false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            map.put("serialNumber", num + 1);
            num += 1;
            listMap.add(map);
        }
        retMap.put("currpage", page);
        retMap.put("totalpages", totalPages);
        retMap.put("total", total);
        retMap.put("rows", listMap);
        return retMap;
    }

    @Override
    public ChaoSongInfo save(ChaoSongInfo chaoSong) {
        return chaoSongInfoRepository.save(chaoSong);
    }

    @Override
    public void save(List<ChaoSongInfo> chaoSongList) {
        chaoSongInfoRepository.saveAll(chaoSongList);
    }

    @Override
    @Transactional(readOnly = false)
    public Map<String, Object> save(String processInstanceId, String users, String isSendSms, String isShuMing,
        String smsContent, String smsPersonId) {
        Map<String, Object> map = new HashMap<String, Object>(16);
        map.put(UtilConsts.SUCCESS, false);
        map.put("msg", "抄送失败");
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String tenantId = Y9LoginUserHolder.getTenantId(), positionId = Y9LoginUserHolder.getPositionId();
            ProcessParam processParam = processParamService.findByProcessInstanceId(processInstanceId);
            String title = processParam.getTitle(), itemId = processParam.getItemId(),
                itemName = processParam.getItemName(), systemName = processParam.getSystemName();
            List<String> orgUnitList = Arrays.asList(users.split(";"));
            List<ChaoSongInfo> csList = new ArrayList<ChaoSongInfo>();
            List<String> userIdListAdd = new ArrayList<String>();
            // 添加的人员
            for (String orgUnitStr : orgUnitList) {
                String[] orgUnitArr = orgUnitStr.split(":");
                Integer type = Integer.valueOf(orgUnitArr[0]);
                String orgUnitId = orgUnitArr[1];
                List<Position> list = new ArrayList<Position>();
                if (2 == type) {
                    list = departmentManager.listPositions(tenantId, orgUnitId).getData();
                    for (Position position : list) {
                        userIdListAdd.add(position.getId());
                    }
                } else if (6 == type) {
                    userIdListAdd.add(orgUnitId);
                } else if (7 == type) {

                }
            }
            // 保存抄送
            OrgUnit dept =
                departmentManager.getDepartment(tenantId, Y9LoginUserHolder.getPosition().getParentId()).getData();
            if (null == dept || null == dept.getId()) {
                dept = organizationManager.getOrganization(tenantId, Y9LoginUserHolder.getPosition().getParentId())
                    .getData();
            }
            List<String> mobile = new ArrayList<String>();
            for (String userId : userIdListAdd) {
                Position position = positionManager.getPosition(tenantId, userId).getData();
                ChaoSongInfo cs = new ChaoSongInfo();
                cs.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
                cs.setCreateTime(sdf.format(new Date()));
                cs.setProcessInstanceId(processInstanceId);
                cs.setSenderId(positionId);
                cs.setSenderName(Y9LoginUserHolder.getPosition().getName());
                cs.setSendDeptId(dept.getId());
                cs.setSendDeptName(dept.getName());
                cs.setStatus(2);
                cs.setTenantId(tenantId);
                cs.setTitle(title);
                cs.setUserId(position.getId());
                cs.setUserName(position.getName());
                Department department = departmentManager.getDepartment(tenantId, position.getParentId()).getData();
                cs.setUserDeptId(department.getId());
                cs.setUserDeptName(department.getName());
                cs.setItemId(itemId);
                cs.setItemName(itemName);
                csList.add(cs);
                // TODO 处理发短信
            }
            this.save(csList);
            asyncHandleService.saveChaoSong4Todo(tenantId, csList);
            if (StringUtils.isNotBlank(isSendSms) && UtilConsts.TRUE.equals(isSendSms)) {
                smsContent += "--" + Y9LoginUserHolder.getUserInfo().getName();
                Boolean smsSwitch = y9Conf.getApp().getItemAdmin().getSmsSwitch();
                if (smsSwitch) {
                    smsHttpManager.sendSmsHttpList(tenantId, Y9LoginUserHolder.getPersonId(), mobile, smsContent,
                        systemName + "抄送");
                } else {
                    LOGGER
                        .info("*********************y9.app.itemAdmin.smsSwitch开关未打开**********************************");
                }
            }
            asyncHandleService.weiXinRemind4ChaoSongInfo(tenantId, Y9LoginUserHolder.getPersonId(),
                processParam.getProcessSerialNumber(), csList);
            map.put(UtilConsts.SUCCESS, true);
            map.put("msg", "抄送成功");
        } catch (Exception e) {
            final Writer result = new StringWriter();
            final PrintWriter print = new PrintWriter(result);
            e.printStackTrace(print);
            try {
                String msg = result.toString();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String time = sdf.format(new Date());
                ErrorLog errorLog = new ErrorLog();
                errorLog.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
                errorLog.setCreateTime(time);
                errorLog.setErrorFlag(ErrorLogModel.ERROR_FLAG_SAVE_CHAOSONG);
                errorLog.setErrorType(ErrorLogModel.ERROR_PROCESS_INSTANCE);
                errorLog.setExtendField("抄送保存失败");
                errorLog.setProcessInstanceId(processInstanceId);
                errorLog.setTaskId("");
                errorLog.setText(msg);
                errorLog.setUpdateTime(time);
                errorLogService.saveErrorLog(errorLog);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return map;
    }

    @Override
    public Map<String, Object> searchAllByUserId(String searchName, String itemId, String userName, String state,
        String year, Integer page, Integer rows) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        String userId = Y9LoginUserHolder.getPositionId();
        Map<String, Object> retMap = new HashMap<String, Object>(16);
        List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
        if (page < 1) {
            page = 1;
        }
        int totalPages = 1;
        long total = 0;
        Pageable pageable = PageRequest.of((page < 1) ? 0 : page - 1, rows, Sort.by(Sort.Direction.DESC, "createTime"));
        Criteria criteria = new Criteria();
        criteria.and("tenantId").is(Y9LoginUserHolder.getTenantId());
        criteria.and("userId").is(userId);
        criteria.and("status").is(1);
        if (StringUtils.isNotBlank(searchName)) {
            criteria.and("title").contains(searchName);
        }
        if (StringUtils.isNotBlank(itemId)) {
            criteria.and("itemId").is(itemId);
        }
        if (StringUtils.isNotBlank(userName)) {
            criteria.and("senderName").contains(userName);
        }
        if (StringUtils.isNotBlank(state)) {
            criteria.and("status").is(Integer.parseInt(state));
        }
        if (StringUtils.isNotBlank(year)) {
            criteria.and("createTime").contains(year);
        }
        Query query = new CriteriaQuery(criteria).setPageable(pageable);
        SearchHits<ChaoSongInfo> searchHits = elasticsearchTemplate.search(query, ChaoSongInfo.class, INDEX);
        List<ChaoSongInfo> csList = searchHits.stream()
            .map(org.springframework.data.elasticsearch.core.SearchHit::getContent).collect(Collectors.toList());
        totalPages = (int)searchHits.getTotalHits() / rows;
        totalPages = searchHits.getTotalHits() % rows == 0 ? totalPages : totalPages + 1;
        total = searchHits.getTotalHits();
        SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int num = (page - 1) * rows;
        HistoricProcessInstanceModel hpi = null;
        ProcessParam processParam = null;
        for (ChaoSongInfo cs : csList) {
            Map<String, Object> map = new HashMap<String, Object>(16);
            map.put("id", cs.getId());
            try {
                String processInstanceId = cs.getProcessInstanceId();
                map.put("createTime", sdf.format(sdf1.parse(cs.getCreateTime())));
                processParam = processParamService.findByProcessInstanceId(processInstanceId);
                map.put("processInstanceId", processInstanceId);
                map.put("senderName", cs.getSenderName());
                map.put("sendDeptId", cs.getSendDeptId());
                map.put("sendDeptName", cs.getSendDeptName());
                map.put("readTime",
                    StringUtils.isNotBlank(cs.getReadTime()) ? sdf.format(sdf1.parse(cs.getReadTime())) : "--");
                map.put("title", processParam.getTitle());
                map.put("status", cs.getStatus());
                map.put("banjie", false);
                map.put("itemId", cs.getItemId());
                map.put("itemName", cs.getItemName());
                map.put("processSerialNumber", processParam.getProcessSerialNumber());
                map.put("number", processParam.getCustomNumber());
                map.put("level", processParam.getCustomLevel());
                hpi = historicProcessManager.getById(tenantId, processInstanceId);
                boolean banjie = hpi == null || (hpi != null && hpi.getEndTime() != null);
                if (banjie) {
                    map.put("banjie", true);
                }
                int countFollow = officeFollowService.countByProcessInstanceId(processInstanceId);
                map.put("follow", countFollow > 0 ? true : false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            map.put("serialNumber", num + 1);
            num += 1;
            listMap.add(map);
        }
        retMap.put("currpage", page);
        retMap.put("totalpages", totalPages);
        retMap.put("total", total);
        retMap.put("rows", listMap);
        return retMap;
    }

    @Override
    public Map<String, Object> searchAllList(String searchName, String itemId, String senderName, String userName,
        String state, String year, Integer page, Integer rows) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        Map<String, Object> retMap = new HashMap<String, Object>(16);
        List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
        if (page < 1) {
            page = 1;
        }
        int totalPages = 1;
        long total = 0;
        Pageable pageable = PageRequest.of((page < 1) ? 0 : page - 1, rows, Sort.by(Sort.Direction.DESC, "createTime"));
        Criteria criteria = new Criteria();
        criteria.and("tenantId").is(Y9LoginUserHolder.getTenantId());
        criteria.and("status").is(1);
        if (StringUtils.isNotBlank(searchName)) {
            criteria.and("title").contains(searchName);
        }
        if (StringUtils.isNotBlank(itemId)) {
            criteria.and("itemId").is(itemId);
        }
        if (StringUtils.isNotBlank(senderName)) {
            criteria.and("senderName").contains(senderName);
        }
        if (StringUtils.isNotBlank(userName)) {
            criteria.and("userName").contains(userName);
        }
        if (StringUtils.isNotBlank(state)) {
            criteria.and("status").is(Integer.parseInt(state));
        }
        if (StringUtils.isNotBlank(year)) {
            criteria.and("createTime").contains(year);
        }
        Query query = new CriteriaQuery(criteria).setPageable(pageable);
        SearchHits<ChaoSongInfo> searchHits = elasticsearchTemplate.search(query, ChaoSongInfo.class, INDEX);
        List<ChaoSongInfo> csList = searchHits.stream()
            .map(org.springframework.data.elasticsearch.core.SearchHit::getContent).collect(Collectors.toList());
        totalPages = (int)searchHits.getTotalHits() / rows;
        totalPages = searchHits.getTotalHits() % rows == 0 ? totalPages : totalPages + 1;
        total = searchHits.getTotalHits();
        SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int num = (page - 1) * rows;
        HistoricProcessInstanceModel hpi = null;
        ProcessParam processParam = null;
        for (ChaoSongInfo cs : csList) {
            Map<String, Object> map = new HashMap<String, Object>(16);
            map.put("id", cs.getId());
            try {
                String processInstanceId = cs.getProcessInstanceId();
                map.put("createTime", sdf.format(sdf1.parse(cs.getCreateTime())));
                processParam = processParamService.findByProcessInstanceId(processInstanceId);
                map.put("processInstanceId", processInstanceId);
                map.put("senderName", cs.getSenderName());
                map.put("sendDeptId", cs.getSendDeptId());
                map.put("sendDeptName", cs.getSendDeptName());
                map.put("readTime",
                    StringUtils.isNotBlank(cs.getReadTime()) ? sdf.format(sdf1.parse(cs.getReadTime())) : "--");
                map.put("title", processParam.getTitle());
                map.put("status", cs.getStatus());
                map.put("banjie", false);
                map.put("itemId", cs.getItemId());
                map.put("itemName", cs.getItemName());
                map.put("userName", cs.getUserName());
                map.put("deptName", cs.getUserDeptName());
                map.put("processSerialNumber", processParam.getProcessSerialNumber());
                map.put("number", processParam.getCustomNumber());
                map.put("level", processParam.getCustomLevel());
                hpi = historicProcessManager.getById(tenantId, processInstanceId);
                boolean banjie = hpi == null || (hpi != null && hpi.getEndTime() != null);
                if (banjie) {
                    map.put("banjie", true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            map.put("serialNumber", num + 1);
            num += 1;
            listMap.add(map);
        }
        retMap.put("currpage", page);
        retMap.put("totalpages", totalPages);
        retMap.put("total", total);
        retMap.put("rows", listMap);
        return retMap;
    }

    @Override
    @Transactional(readOnly = false)
    public void updateTitle(String processInstanceId, String documentTitle) {
        try {
            List<ChaoSongInfo> list = chaoSongInfoRepository.findByProcessInstanceId(processInstanceId);
            List<ChaoSongInfo> newList = new ArrayList<ChaoSongInfo>();
            for (ChaoSongInfo info : list) {
                info.setTitle(documentTitle);
                newList.add(info);
            }
            if (newList.size() > 0) {
                chaoSongInfoRepository.saveAll(newList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
