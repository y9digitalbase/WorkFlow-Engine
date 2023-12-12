package net.risesoft.api.itemadmin.position;

import java.util.Map;

/**
 * 关联文件接口
 *
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/19
 */
public interface AssociatedFile4PositionApi {

    /**
     * 关联文件计数
     *
     * @param tenantId 租户id
     * @param processSerialNumber 流程编号
     * @return int
     */
    int countAssociatedFile(String tenantId, String processSerialNumber);

    /**
     * 删除关联文件
     *
     * @param tenantId 租户id
     * @param processSerialNumber 流程编号
     * @param delIds 关联流程实例id(,隔开)
     * @return boolean 是否删除成功
     */
    public boolean deleteAllAssociatedFile(String tenantId, String processSerialNumber, String delIds);

    /**
     * 删除关联文件
     *
     * @param tenantId 租户id
     * @param processSerialNumber 流程编号
     * @param delId 关联流程实例id
     * @return boolean 是否删除成功
     */
    public boolean deleteAssociatedFile(String tenantId, String processSerialNumber, String delId);

    /**
     * 获取关联文件列表,包括未办结件
     *
     * @param tenantId 租户id
     * @param processSerialNumber 流程编号
     * @return Map&lt;String, Object&gt;
     */
    public Map<String, Object> getAssociatedFileAllList(String tenantId, String positionId, String processSerialNumber);

    /**
     * 获取关联文件列表
     *
     * @param tenantId 租户id
     * @param processSerialNumber 流程编号
     * @return Map&lt;String, Object&gt;
     */
    public Map<String, Object> getAssociatedFileList(String tenantId, String processSerialNumber);

    /**
     * 保存关联文件
     *
     * @param tenantId 租户id
     * @param positionId 岗位id
     * @param processSerialNumber 流程编号
     * @param processInstanceIds 关联的流程实例ids
     * @return boolean 是否保存成功
     */
    public boolean saveAssociatedFile(String tenantId, String positionId, String processSerialNumber, String processInstanceIds);
}
