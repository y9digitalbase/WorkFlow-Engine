package net.risesoft.service.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.HistoryService;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.risesoft.service.CustomHistoricVariableService;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/30
 */
@Transactional(readOnly = true)
@Service(value = "customHistoricVariableService")
public class CustomHistoricVariableServiceImpl implements CustomHistoricVariableService {

    @Autowired
    private HistoryService historyService;

    @Override
    public List<HistoricVariableInstance> getByProcessInstanceId(String processInstanceId) {
        return historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstanceId).list();
    }

    @Override
    public HistoricVariableInstance getByProcessInstanceIdAndVariableName(String processInstanceId, String variableName, String year) {
        if (StringUtils.isBlank(year)) {
            List<HistoricVariableInstance> list = historyService.createHistoricVariableInstanceQuery().executionId(processInstanceId).variableName(variableName).list();
            return (list != null && list.size() > 0) ? list.get(0) : null;
        } else {
            String sql = "select RES.* from ACT_HI_VARINST_" + year + " RES WHERE RES.EXECUTION_ID_ = '" + processInstanceId + "' and RES.NAME_ = '" + variableName + "'";
            return historyService.createNativeHistoricVariableInstanceQuery().sql(sql).singleResult();
        }
    }

    @Override
    public List<HistoricVariableInstance> getByTaskId(String taskId) {
        return historyService.createHistoricVariableInstanceQuery().taskId(taskId).list();
    }

    @Override
    public HistoricVariableInstance getByTaskIdAndVariableName(String taskId, String variableName, String year) {
        if (StringUtils.isBlank(year)) {
            return historyService.createHistoricVariableInstanceQuery().taskId(taskId).variableName(variableName).singleResult();
        } else {
            String sql = "select RES.* from ACT_HI_VARINST_" + year + " RES WHERE RES.TASK_ID_ = '" + taskId + "' and RES.NAME_ = '" + variableName + "'";
            return historyService.createNativeHistoricVariableInstanceQuery().sql(sql).singleResult();
        }
    }

    @Override
    public Map<String, Object> getVariables(String tenantId, String processInstanceId, Collection<String> keys) {
        List<HistoricVariableInstance> hviList = historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstanceId).excludeTaskVariables().list();
        Map<String, Object> map = new HashMap<String, Object>(16);
        for (HistoricVariableInstance hvi : hviList) {
            for (String key : keys) {
                if (hvi.getVariableName().equals(key)) {
                    map.put(hvi.getVariableName(), hvi.getValue());
                    continue;
                }
            }
        }
        return map;
    }
}
