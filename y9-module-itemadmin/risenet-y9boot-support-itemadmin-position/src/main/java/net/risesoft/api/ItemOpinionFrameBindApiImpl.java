package net.risesoft.api;

import lombok.RequiredArgsConstructor;
import net.risesoft.api.itemadmin.ItemOpinionFrameBindApi;
import net.risesoft.entity.ItemOpinionFrameBind;
import net.risesoft.entity.OpinionFrame;
import net.risesoft.model.itemadmin.ItemOpinionFrameBindModel;
import net.risesoft.service.ItemOpinionFrameBindService;
import net.risesoft.service.OpinionFrameService;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.util.Y9BeanUtil;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 意见框绑定接口
 *
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/20
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/services/rest/itemOpinionFrameBind")
public class ItemOpinionFrameBindApiImpl implements ItemOpinionFrameBindApi {

    private final ItemOpinionFrameBindService itemOpinionFrameBindService;

    private final OpinionFrameService opinionFrameService;

    /**
     * 根据事项id获取所有绑定意见框
     *
     * @param tenantId 租户id
     * @param itemId 事项id
     * @return List<ItemOpinionFrameBindModel>
     */
    @Override
    @GetMapping(value = "/findByItemId", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ItemOpinionFrameBindModel> findByItemId(String tenantId, String itemId) {
        Y9LoginUserHolder.setTenantId(tenantId);
        List<ItemOpinionFrameBind> list = itemOpinionFrameBindService.findByItemId(itemId);
        List<ItemOpinionFrameBindModel> modelList = new ArrayList<>();
        for (ItemOpinionFrameBind o : list) {
            ItemOpinionFrameBindModel model = new ItemOpinionFrameBindModel();
            Y9BeanUtil.copyProperties(o, model);
            OpinionFrame opinionFrame = opinionFrameService.findByMark(o.getOpinionFrameMark());
            model.setOpinionFrameName(opinionFrame == null ? "意见框不存在" : opinionFrame.getName());
            modelList.add(model);
        }
        return modelList;
    }

    /**
     * 根据事项id和流程定义id获取所有绑定意见框
     * 
     * @param tenantId 租户id
     * @param itemId 事项id
     * @param processDefinitionId 流程定义id
     * @return List<ItemOpinionFrameBindModel>
     */
    @Override
    @GetMapping(value = "/findByItemIdAndProcessDefinitionId", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ItemOpinionFrameBindModel> findByItemIdAndProcessDefinitionId(String tenantId, String itemId,
        String processDefinitionId) {
        Y9LoginUserHolder.setTenantId(tenantId);
        List<ItemOpinionFrameBind> list =
            itemOpinionFrameBindService.findByItemIdAndProcessDefinitionId(itemId, processDefinitionId);
        List<ItemOpinionFrameBindModel> modelList = new ArrayList<>();
        for (ItemOpinionFrameBind o : list) {
            ItemOpinionFrameBindModel model = new ItemOpinionFrameBindModel();
            Y9BeanUtil.copyProperties(o, model);
            OpinionFrame opinionFrame = opinionFrameService.findByMark(o.getOpinionFrameMark());
            model.setOpinionFrameName(opinionFrame == null ? "意见框不存在" : opinionFrame.getName());
            modelList.add(model);
        }
        return modelList;
    }

    /**
     * 根据事项id和任务id获取绑定意见框
     *
     * @param tenantId 租户id
     * @param userId 人员id
     * @param itemId 事项id
     * @param processDefinitionId 流程定义id
     * @param taskDefKey 任务key
     * @return List<ItemOpinionFrameBindModel>
     */
    @Override
    @GetMapping(value = "/findByItemIdAndProcessDefinitionIdAndTaskDefKey", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ItemOpinionFrameBindModel> findByItemIdAndProcessDefinitionIdAndTaskDefKey(String tenantId,
        String userId, String itemId, String processDefinitionId, String taskDefKey) {
        Y9LoginUserHolder.setTenantId(tenantId);
        List<ItemOpinionFrameBind> list = itemOpinionFrameBindService
            .findByItemIdAndProcessDefinitionIdAndTaskDefKey(itemId, processDefinitionId, taskDefKey);
        List<ItemOpinionFrameBindModel> modelList = new ArrayList<>();
        for (ItemOpinionFrameBind o : list) {
            ItemOpinionFrameBindModel model = new ItemOpinionFrameBindModel();
            Y9BeanUtil.copyProperties(o, model);
            OpinionFrame opinionFrame = opinionFrameService.findByMark(o.getOpinionFrameMark());
            model.setOpinionFrameName(opinionFrame == null ? "意见框不存在" : opinionFrame.getName());
            modelList.add(model);
        }
        return modelList;
    }

    /**
     * 根据事项id和任务id获取绑定意见框（包含角色信息）
     *
     * @param tenantId 租户id
     * @param userId 人员id
     * @param itemId 事项id
     * @param processDefinitionId 流程定义id
     * @param taskDefKey 任务key
     * @return List<ItemOpinionFrameBindModel>
     */
    @Override
    @GetMapping(value = "/findByItemIdAndProcessDefinitionIdAndTaskDefKeyContainRole",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ItemOpinionFrameBindModel> findByItemIdAndProcessDefinitionIdAndTaskDefKeyContainRole(String tenantId,
        String userId, String itemId, String processDefinitionId, String taskDefKey) {
        Y9LoginUserHolder.setTenantId(tenantId);
        List<ItemOpinionFrameBind> list = itemOpinionFrameBindService
            .findByItemIdAndProcessDefinitionIdAndTaskDefKeyContainRole(itemId, processDefinitionId, taskDefKey);
        List<ItemOpinionFrameBindModel> modelList = new ArrayList<>();
        for (ItemOpinionFrameBind o : list) {
            ItemOpinionFrameBindModel model = new ItemOpinionFrameBindModel();
            Y9BeanUtil.copyProperties(o, model);
            OpinionFrame opinionFrame = opinionFrameService.findByMark(o.getOpinionFrameMark());
            model.setOpinionFrameName(opinionFrame == null ? "意见框不存在" : opinionFrame.getName());
            modelList.add(model);
        }
        return modelList;
    }
}
