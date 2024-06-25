package net.risesoft.controller;

import lombok.RequiredArgsConstructor;
import net.risesoft.api.processadmin.ProcessDefinitionApi;
import net.risesoft.entity.CommonButton;
import net.risesoft.entity.ItemButtonBind;
import net.risesoft.entity.SendButton;
import net.risesoft.entity.SpmApproveItem;
import net.risesoft.enums.ItemButtonTypeEnum;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.CommonButtonService;
import net.risesoft.service.ItemButtonBindService;
import net.risesoft.service.SendButtonService;
import net.risesoft.service.SpmApproveItemService;
import net.risesoft.y9.Y9LoginUserHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/20
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/vue/itemButtonBind")
public class ItemButtonBindRestController {

    private final ItemButtonBindService itemButtonBindService;

    private final CommonButtonService commonButtonService;

    private final SendButtonService sendButtonService;

    private final ProcessDefinitionApi processDefinitionManager;

    private final SpmApproveItemService spmApproveItemService;

    /**
     * 复制按钮配置
     *
     * @param itemId 事项id
     * @return
     */
    @RequestMapping(value = "/copyBind", method = RequestMethod.POST, produces = "application/json")
    public Y9Result<String> copyBind(@RequestParam String itemId, @RequestParam String processDefinitionId) {
        itemButtonBindService.copyBind(itemId, processDefinitionId);
        return Y9Result.successMsg("复制成功");
    }

    /**
     * 获取按钮绑定列表
     *
     * @param itemId 事项id
     * @param buttonType 按钮类型
     * @param processDefinitionId 流程定义id
     * @param taskDefKey 任务key
     * @return
     */
    @RequestMapping(value = "/getBindList", method = RequestMethod.GET, produces = "application/json")
    public Y9Result<List<ItemButtonBind>> getBindList(@RequestParam String itemId, @RequestParam Integer buttonType,
        @RequestParam String processDefinitionId, @RequestParam(required = false) String taskDefKey) {
        List<ItemButtonBind> list =
            itemButtonBindService.findListContainRole(itemId, buttonType, processDefinitionId, taskDefKey);
        return Y9Result.success(list, "获取成功");
    }

    @RequestMapping(value = "/getBindListByButtonId", method = RequestMethod.GET, produces = "application/json")
    public Y9Result<List<Map<String, Object>>> getBindListByButtonId(@RequestParam String buttonId) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        List<ItemButtonBind> ibbList = itemButtonBindService.findListByButtonId(buttonId);
        List<Map<String, Object>> bindList = new ArrayList<>();
        Map<String, Object> map;
        SpmApproveItem item;
        for (ItemButtonBind bind : ibbList) {
            map = new HashMap<>(16);
            map.put("id", bind.getId());
            map.put("processDefinitionId", bind.getProcessDefinitionId());
            map.put("roleNames", bind.getRoleNames());

            item = spmApproveItemService.findById(bind.getItemId());
            map.put("itemName", null == item ? "事项不存在" : item.getName());

            String taskDefName = "整个流程";
            if (StringUtils.isNotEmpty(bind.getTaskDefKey())) {
                List<Map<String, Object>> list =
                    processDefinitionManager.getNodes(tenantId, bind.getProcessDefinitionId(), false);
                for (Map<String, Object> mapTemp : list) {
                    if (mapTemp.get("taskDefKey").equals(bind.getTaskDefKey())) {
                        taskDefName = (String)mapTemp.get("taskDefName");
                    }
                }
            }
            map.put("taskDefKey",
                taskDefName + (StringUtils.isEmpty(bind.getTaskDefKey()) ? "" : "(" + bind.getTaskDefKey() + ")"));
            bindList.add(map);
        }
        return Y9Result.success(bindList, "获取成功");
    }

    /**
     * 获取任务节点信息和流程定义信息
     *
     * @param itemId 事项id
     * @param processDefinitionId 流程定义id
     * @return
     */
    @RequestMapping(value = "/getBpmList", method = RequestMethod.GET, produces = "application/json")
    public Y9Result<Map<String, Object>> getBpmList(@RequestParam String itemId,
        @RequestParam String processDefinitionId) {
        List<Map<String, Object>> list;
        Map<String, Object> resMap = new HashMap<>(16);
        String tenantId = Y9LoginUserHolder.getTenantId();
        list = processDefinitionManager.getNodes(tenantId, processDefinitionId, false);
        List<ItemButtonBind> cbList, sbList;
        for (Map<String, Object> map : list) {
            String commonButtonNames = "";
            String sendButtonNames = "";
            cbList = itemButtonBindService.findListContainRole(itemId, ItemButtonTypeEnum.COMMON.getValue(),
                processDefinitionId, (String)map.get("taskDefKey"));
            sbList = itemButtonBindService.findListContainRole(itemId, ItemButtonTypeEnum.SEND.getValue(),
                processDefinitionId, (String)map.get("taskDefKey"));
            for (ItemButtonBind cb : cbList) {
                if (StringUtils.isEmpty(commonButtonNames)) {
                    commonButtonNames = cb.getButtonName();
                } else {
                    commonButtonNames += "、" + cb.getButtonName();
                }
            }
            for (ItemButtonBind sb : sbList) {
                if (StringUtils.isEmpty(sendButtonNames)) {
                    sendButtonNames = sb.getButtonName();
                } else {
                    sendButtonNames += "、" + sb.getButtonName();
                }
            }
            map.put("commonButtonNames", commonButtonNames);
            map.put("sendButtonNames", sendButtonNames);
        }
        resMap.put("rows", list);
        return Y9Result.success(resMap, "获取成功");
    }

    /**
     * 获取按钮列表
     *
     * @param itemId 事项id
     * @param buttonType 按钮类型
     * @param processDefinitionId 流程定义id
     * @param taskDefKey 任务key
     * @return
     */
    @RequestMapping(value = "/getButtonList", method = RequestMethod.GET, produces = "application/json")
    public Y9Result<Map<String, Object>> getButtonList(@RequestParam String itemId, @RequestParam Integer buttonType,
        @RequestParam String processDefinitionId, @RequestParam(required = false) String taskDefKey) {
        Map<String, Object> map = new HashMap<>(16);
        List<ItemButtonBind> buttonItemBindList =
            itemButtonBindService.findList(itemId, buttonType, processDefinitionId, taskDefKey);
        if (1 == buttonType) {
            List<CommonButton> cbList = commonButtonService.findAll();
            List<CommonButton> cbListTemp = new ArrayList<>();
            if (buttonItemBindList.isEmpty()) {
                cbListTemp = cbList;
            } else {
                for (CommonButton cb : cbList) {
                    boolean isBind = false;
                    for (ItemButtonBind bib : buttonItemBindList) {
                        if (bib.getButtonId().equals(cb.getId())) {
                            isBind = true;
                            break;
                        }
                    }
                    if (!isBind) {
                        cbListTemp.add(cb);
                    }
                }
            }
            map.put("rows", cbListTemp);
        } else {
            List<SendButton> sbList = sendButtonService.findAll();
            List<SendButton> sbListTemp = new ArrayList<>();
            if (buttonItemBindList.isEmpty()) {
                sbListTemp = sbList;
            } else {
                for (SendButton sb : sbList) {
                    boolean isBind = false;
                    for (ItemButtonBind bib : buttonItemBindList) {
                        if (bib.getButtonId().equals(sb.getId())) {
                            isBind = true;
                            break;
                        }
                    }
                    if (!isBind) {
                        sbListTemp.add(sb);
                    }
                }
            }
            map.put("rows", sbListTemp);
        }
        return Y9Result.success(map, "获取成功");
    }

    /**
     * 获取按钮排序列表
     *
     * @param itemId 事项id
     * @param buttonType 按钮类型
     * @param processDefinitionId 流程定义id
     * @param taskDefKey 任务key
     * @return
     */
    @RequestMapping(value = "/getButtonOrderList", method = RequestMethod.GET, produces = "application/json")
    public Y9Result<List<ItemButtonBind>> getButtonOrderList(@RequestParam String itemId,
        @RequestParam Integer buttonType, @RequestParam String processDefinitionId,
        @RequestParam(required = false) String taskDefKey) {
        List<ItemButtonBind> list = itemButtonBindService.findList(itemId, buttonType, processDefinitionId, taskDefKey);
        return Y9Result.success(list, "获取成功");
    }

    /**
     * 删除按钮绑定
     *
     * @param ids 绑定ids
     * @return
     */
    @RequestMapping(value = "/removeBind", method = RequestMethod.POST, produces = "application/json")
    public Y9Result<String> removeBind(@RequestParam String[] ids) {
        itemButtonBindService.removeButtonItemBinds(ids);
        return Y9Result.successMsg("删除成功");
    }

    /**
     * 保存绑定按钮
     *
     * @param buttonId 按钮id
     * @param itemId 事项id
     * @param buttonType 按钮类型
     * @param processDefinitionId 流程定义id
     * @param taskDefKey 任务key
     * @return
     */
    @RequestMapping(value = "/saveBindButton", method = RequestMethod.POST, produces = "application/json")
    public Y9Result<String> saveBindButton(@RequestParam String buttonId, @RequestParam String itemId,
        @RequestParam String processDefinitionId, @RequestParam Integer buttonType,
        @RequestParam(required = false) String taskDefKey) {
        itemButtonBindService.bindButton(itemId, buttonId, processDefinitionId, taskDefKey, buttonType);
        return Y9Result.successMsg("绑定成功");
    }

    /**
     * 保存按钮排序
     *
     * @param idAndTabIndexs 排序id
     * @return
     */
    @RequestMapping(value = "/saveOrder", method = RequestMethod.POST, produces = "application/json")
    public Y9Result<String> saveOrder(@RequestParam String[] idAndTabIndexs) {
        itemButtonBindService.saveOrder(idAndTabIndexs);
        return Y9Result.successMsg("保存成功");
    }
}
