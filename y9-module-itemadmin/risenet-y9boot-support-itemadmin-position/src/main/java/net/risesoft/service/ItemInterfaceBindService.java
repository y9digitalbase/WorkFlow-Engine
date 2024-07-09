package net.risesoft.service;

import net.risesoft.entity.ItemInterfaceBind;

import java.util.List;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/21
 */
public interface ItemInterfaceBindService {

    /**
     * 根据接口id获取绑定关系列表
     *
     * @param interfaceId
     * @return
     */
    List<ItemInterfaceBind> findByInterfaceId(String interfaceId);

    /**
     * 根据事项id获取绑定列表
     *
     * @param itemId
     * @return
     */
    List<ItemInterfaceBind> findByItemId(String itemId);

    /**
     * 根据id删除绑定关系
     *
     * @param id
     */
    void removeBind(String id);

    /**
     * 保存事项链接绑定
     *
     * @param itemId
     * @param interfaceIds
     * @return
     */
    void saveBind(String itemId, String[] interfaceIds);

    /**
     * 复制事项接口配置绑定信息
     *
     * @param itemId
     * @param newItemId
     */
    void copyBindInfo(String itemId, String newItemId);
}
