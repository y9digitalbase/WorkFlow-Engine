package net.risesoft.service.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.HistoryService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.risesoft.service.CustomHistoricActivityService;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/30
 */
@Transactional(readOnly = true)
@Service(value = "customHistoricActivityService")
public class CustomHistoricActivityServiceImpl implements CustomHistoricActivityService {

    @Autowired
    private HistoryService historyService;

    @Override
    public List<HistoricActivityInstance> getByProcessInstanceId(String processInstanceId) {
        return historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId)
            .orderByHistoricActivityInstanceStartTime().asc().list();
    }

    @Override
    public List<HistoricActivityInstance> getByProcessInstanceIdAndActivityType(String processInstanceId,
        String activityType) {
        return historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId)
            .activityType(activityType).list();
    }

    @Override
    public List<HistoricActivityInstance> getByProcessInstanceIdAndYear(String processInstanceId, String year) {
        if (StringUtils.isBlank(year)) {
            return historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime().asc().list();
        } else {
            String sql = "select RES.* from ACT_HI_ACTINST_" + year + " RES WHERE RES.PROC_INST_ID_ = '"
                + processInstanceId + "' order by START_TIME_ asc";
            return historyService.createNativeHistoricActivityInstanceQuery().sql(sql).list();
        }
    }
}
