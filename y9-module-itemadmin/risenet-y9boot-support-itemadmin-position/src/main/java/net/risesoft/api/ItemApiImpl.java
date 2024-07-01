package net.risesoft.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import net.risesoft.api.itemadmin.position.Item4PositionApi;
import net.risesoft.api.platform.org.PositionApi;
import net.risesoft.entity.ItemMappingConf;
import net.risesoft.entity.SpmApproveItem;
import net.risesoft.model.itemadmin.ItemListModel;
import net.risesoft.model.itemadmin.ItemMappingConfModel;
import net.risesoft.model.itemadmin.ItemModel;
import net.risesoft.model.itemadmin.ItemSystemListModel;
import net.risesoft.model.platform.Position;
import net.risesoft.pojo.Y9Result;
import net.risesoft.repository.jpa.ItemMappingConfRepository;
import net.risesoft.repository.jpa.SpmApproveItemRepository;
import net.risesoft.service.DocumentService;
import net.risesoft.service.SpmApproveItemService;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.util.Y9BeanUtil;

/**
 * 事项接口
 *
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/20
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/services/rest/item4Position")
public class ItemApiImpl implements Item4PositionApi {

    private final DocumentService documentService;

    private final SpmApproveItemService spmApproveItemService;

    private final SpmApproveItemRepository spmApproveItemRepository;

    private final PositionApi positionManager;

    private final ItemMappingConfRepository itemMappingConfRepository;

    /**
     * 根据系统名称获取事项
     *
     * @param tenantId 租户id
     * @param systemName 系统名称
     * @return Y9Result<List<ItemModel>>
     */
    @Override
    public Y9Result<List<ItemModel>> findAll(String tenantId, String systemName) {
        Y9LoginUserHolder.setTenantId(tenantId);
        List<SpmApproveItem> list = spmApproveItemRepository.findAll(systemName);
        List<ItemModel> itemModelList = new ArrayList<>();
        for (SpmApproveItem item : list) {
            ItemModel itemModel = new ItemModel();
            Y9BeanUtil.copyProperties(item, itemModel);
            itemModelList.add(itemModel);
        }
        return Y9Result.success(itemModelList);
    }

    /**
     * 根据流程的定义Key查找对应的事项
     *
     * @param tenantId 租户Id
     * @param processDefinitionKey 流程定义Key
     * @return Y9Result<ItemModel>
     */
    @Override
    public Y9Result<ItemModel> findByProcessDefinitionKey(String tenantId, String processDefinitionKey) {
        Y9LoginUserHolder.setTenantId(tenantId);
        ItemModel itemModel = spmApproveItemService.findByProcessDefinitionKey(tenantId, processDefinitionKey);
        return Y9Result.success(itemModel);
    }

    /**
     * 获取所有事项列表
     *
     * @param tenantId 租户id
     * @return Y9Result<List<ItemModel>>
     */
    @Override
    public Y9Result<List<ItemModel>> getAllItem(String tenantId) {
        Y9LoginUserHolder.setTenantId(tenantId);
        List<SpmApproveItem> list = spmApproveItemRepository.findAll();
        List<ItemModel> itemModelList = new ArrayList<>();
        for (SpmApproveItem item : list) {
            ItemModel itemModel = new ItemModel();
            Y9BeanUtil.copyProperties(item, itemModel);
            itemModelList.add(itemModel);
        }
        return Y9Result.success(itemModelList);
    }

    /**
     * 获取所有事项
     *
     * @param tenantId 租户id
     * @return Y9Result<List<ItemModel>>
     */
    @SuppressWarnings("unchecked")
    @Override
    public Y9Result<List<ItemModel>> getAllItemList(String tenantId) {
        Y9LoginUserHolder.setTenantId(tenantId);
        Map<String, Object> map = spmApproveItemService.list();
        List<SpmApproveItem> list = (List<SpmApproveItem>)map.get("rows");
        List<ItemModel> itemList = new ArrayList<>();
        for (SpmApproveItem item : list) {
            ItemModel itemModel = new ItemModel();
            Y9BeanUtil.copyProperties(item, itemModel);
            itemList.add(itemModel);
        }
        return Y9Result.success(itemList);
    }

    /**
     * 根据事项id获取事项
     *
     * @param tenantId 租户id
     * @param itemId 事项id
     * @return Y9Result<ItemModel>
     */
    @Override
    public Y9Result<ItemModel> getByItemId(String tenantId, String itemId) {
        Y9LoginUserHolder.setTenantId(tenantId);
        SpmApproveItem item = spmApproveItemService.findById(itemId);
        ItemModel itemModel = new ItemModel();
        if (item != null) {
            Y9BeanUtil.copyProperties(item, itemModel);
        }
        return Y9Result.success(itemModel);
    }

    /**
     * 获取有权限的首个事项id
     *
     * @param tenantId 租户id
     * @param positionId 岗位id
     * @return Y9Result<String>
     */
    @Override
    public Y9Result<String> getFirstItem(String tenantId, String positionId) {
        Y9LoginUserHolder.setTenantId(tenantId);
        Position position = positionManager.get(tenantId, positionId).getData();
        Y9LoginUserHolder.setPosition(position);
        String itemId = documentService.getFirstItem();
        return Y9Result.success(itemId);
    }

    /**
     * 根据事项id获取绑定的表单id
     *
     * @param tenantId 租户Id
     * @param itemId 事项id
     * @param processDefinitionKey 流程定义Key
     * @return Y9Result<String>
     */
    @Override
    public Y9Result<String> getFormIdByItemId(String tenantId, String itemId, String processDefinitionKey) {
        Y9LoginUserHolder.setTenantId(tenantId);
        String formId = documentService.getFormIdByItemId(itemId, processDefinitionKey);
        return Y9Result.success(formId);
    }

    /**
     * 获取新建事项列表
     *
     * @param tenantId 租户id
     * @param positionId 岗位id
     * @return Y9Result<List<ItemListModel>>
     */
    @Override
    public Y9Result<List<ItemListModel>> getItemList(String tenantId, String positionId) {
        Y9LoginUserHolder.setTenantId(tenantId);
        Position position = positionManager.get(tenantId, positionId).getData();
        Y9LoginUserHolder.setPosition(position);
        List<ItemListModel> list = documentService.getItemList();
        return Y9Result.success(list);
    }

    /**
     * 获取事项系统字段映射配置
     *
     * @param tenantId 租户Id
     * @param itemId 事项id
     * @param mappingId 系统标识
     * @return Y9Result<List<ItemMappingConfModel>>
     */
    @Override
    public Y9Result<List<ItemMappingConfModel>> getItemMappingConf(String tenantId, String itemId, String mappingId) {
        Y9LoginUserHolder.setTenantId(tenantId);
        List<ItemMappingConf> list =
            itemMappingConfRepository.findByItemIdAndMappingIdOrderByCreateTimeDesc(itemId, mappingId);
        List<ItemMappingConfModel> itemList = new ArrayList<>();
        for (ItemMappingConf item : list) {
            ItemMappingConfModel itemModel = new ItemMappingConfModel();
            Y9BeanUtil.copyProperties(item, itemModel);
            itemList.add(itemModel);
        }
        return Y9Result.success(itemList);
    }

    /**
     * 获取事项系统
     *
     * @param tenantId 租户id
     * @return Y9Result<List<ItemSystemListModel>>
     */
    @Override
    public Y9Result<List<ItemSystemListModel>> getItemSystem(String tenantId) {
        Y9LoginUserHolder.setTenantId(tenantId);
        List<Map<String, Object>> list = spmApproveItemRepository.getItemSystem();
        List<ItemSystemListModel> res_list = new ArrayList<>();
        for (Map<String, Object> map : list) {
            ItemSystemListModel itemSystemListModel = new ItemSystemListModel();
            itemSystemListModel.setSystemName(map.get("systemName").toString());
            itemSystemListModel.setSysLevel(map.get("sysLevel").toString());
            res_list.add(itemSystemListModel);
        }
        return Y9Result.success(res_list);
    }

    /**
     * 获取个人有权限事项列表
     *
     * @param tenantId 租户id
     * @param positionId 岗位id
     * @return Y9Result<List<ItemListModel>>
     */
    @Override
    public Y9Result<List<ItemListModel>> getMyItemList(String tenantId, String positionId) {
        Y9LoginUserHolder.setTenantId(tenantId);
        Position position = positionManager.get(tenantId, positionId).getData();
        Y9LoginUserHolder.setPosition(position);
        List<ItemListModel> list = documentService.getMyItemList();
        return Y9Result.success(list);
    }

    /**
     * 判断该租户是否有流程定义
     *
     * @param tenantId 租户Id
     * @param processDefinitionKey 流程定义Key
     * @return Y9Result<Boolean>
     */
    @Override
    public Y9Result<Boolean> hasProcessDefinitionByKey(String tenantId, String processDefinitionKey) {
        Y9LoginUserHolder.setTenantId(tenantId);
        Boolean hasProcessDefinition = spmApproveItemService.hasProcessDefinitionByKey(processDefinitionKey);
        return Y9Result.success(hasProcessDefinition);
    }
}
