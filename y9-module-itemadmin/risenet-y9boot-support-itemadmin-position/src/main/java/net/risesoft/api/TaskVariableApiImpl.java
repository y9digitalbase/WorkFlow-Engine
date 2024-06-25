package net.risesoft.api;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import net.risesoft.api.itemadmin.TaskVariableApi;
import net.risesoft.entity.TaskVariable;
import net.risesoft.model.itemadmin.TaskVariableModel;
import net.risesoft.pojo.Y9Result;
import net.risesoft.repository.jpa.TaskVariableRepository;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.util.Y9BeanUtil;

/**
 * 任务变量接口
 *
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/20
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/services/rest/taskVariable", produces = MediaType.APPLICATION_JSON_VALUE)
public class TaskVariableApiImpl implements TaskVariableApi {

    private final TaskVariableRepository taskVariableRepository;

    /**
     * 根据任务id和keyName获取任务变量
     *
     * @param tenantId 租户id
     * @param taskId 任务id
     * @param keyName keyName
     * @return TaskVariableModel
     */
    @Override
    public Y9Result<TaskVariableModel> findByTaskIdAndKeyName(String tenantId, String taskId, String keyName) {
        Y9LoginUserHolder.setTenantId(tenantId);
        TaskVariable taskVariable = taskVariableRepository.findByTaskIdAndKeyName(taskId, keyName);
        TaskVariableModel taskVariableModel = null;
        if (taskVariable != null) {
            taskVariableModel = new TaskVariableModel();
            Y9BeanUtil.copyProperties(taskVariable, taskVariableModel);
        }
        return Y9Result.success(taskVariableModel);
    }

}
