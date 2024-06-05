package net.risesoft.api;

import java.util.HashMap;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import net.risesoft.api.itemadmin.position.AssociatedFile4PositionApi;
import net.risesoft.api.platform.org.PositionApi;
import net.risesoft.model.platform.Position;
import net.risesoft.service.AssociatedFileService;
import net.risesoft.y9.Y9LoginUserHolder;

/**
 * 关联文件接口
 *
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/20
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/services/rest/associatedFile4Position")
public class AssociatedFileApiImpl implements AssociatedFile4PositionApi {

    private final AssociatedFileService associatedFileService;

    private final PositionApi positionManager;

    /**
     * 关联文件计数
     *
     * @param tenantId            租户id
     * @param processSerialNumber 流程编号
     * @return int
     */
    @Override
    @GetMapping(value = "/countAssociatedFile", produces = MediaType.APPLICATION_JSON_VALUE)
    public int countAssociatedFile(@RequestParam @NotBlank String tenantId, @RequestParam @NotBlank String processSerialNumber) {
        Y9LoginUserHolder.setTenantId(tenantId);
        return associatedFileService.countAssociatedFile(processSerialNumber);
    }

    /**
     * 批量删除关联文件
     *
     * @param tenantId            租户id
     * @param processSerialNumber 流程编号
     * @param delIds              关联流程实例id(,隔开)
     * @return boolean 是否删除成功
     */
    @Override
    @PostMapping(value = "/deleteAllAssociatedFile", produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean deleteAllAssociatedFile(@RequestParam @NotBlank String tenantId, @RequestParam @NotBlank String processSerialNumber, @RequestParam @NotBlank String delIds) {
        Y9LoginUserHolder.setTenantId(tenantId);
        boolean b = associatedFileService.deleteAllAssociatedFile(processSerialNumber, delIds);
        return b;
    }

    /**
     * 删除关联文件
     *
     * @param tenantId            租户id
     * @param processSerialNumber 流程编号
     * @param delId               关联流程实例id
     * @return boolean 是否删除成功
     */
    @Override
    @PostMapping(value = "/deleteAssociatedFile", produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean deleteAssociatedFile(@RequestParam @NotBlank String tenantId, @RequestParam @NotBlank String processSerialNumber, @RequestParam @NotBlank String delId) {
        Y9LoginUserHolder.setTenantId(tenantId);
        boolean b = associatedFileService.deleteAssociatedFile(processSerialNumber, delId);
        return b;
    }

    /**
     * 获取关联文件列表(包括未办结件)
     *
     * @param tenantId            租户id
     * @param positionId          岗位id
     * @param processSerialNumber 流程编号
     * @return Map<String, Object>
     */
    @Override
    @GetMapping(value = "/getAssociatedFileAllList", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getAssociatedFileAllList(@RequestParam @NotBlank String tenantId, @RequestParam @NotBlank String positionId,
                                                        @RequestParam @NotBlank String processSerialNumber) {
        Y9LoginUserHolder.setTenantId(tenantId);
        Position position = positionManager.get(tenantId, positionId).getData();
        Y9LoginUserHolder.setPosition(position);
        Map<String, Object> map = new HashMap<String, Object>(16);
        map = associatedFileService.getAssociatedFileAllList(processSerialNumber);
        return map;
    }

    /**
     * 获取关联文件列表
     *
     * @param tenantId            租户id
     * @param processSerialNumber 流程编号
     * @return Map<String, Object>
     */
    @Override
    @GetMapping(value = "/getAssociatedFileList", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getAssociatedFileList(@RequestParam @NotBlank String tenantId, @RequestParam @NotBlank String processSerialNumber) {
        Y9LoginUserHolder.setTenantId(tenantId);
        Map<String, Object> map = new HashMap<String, Object>(16);
        map = associatedFileService.getAssociatedFileList(processSerialNumber);
        return map;
    }

    /**
     * 保存关联文件
     *
     * @param tenantId            租户id
     * @param positionId          岗位id
     * @param processSerialNumber 流程编号
     * @param processInstanceIds  关联的流程实例ids
     * @return boolean 是否保存成功
     */
    @Override
    @PostMapping(value = "/saveAssociatedFile", produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean saveAssociatedFile(@RequestParam @NotBlank String tenantId, @RequestParam @NotBlank String positionId, @RequestParam @NotBlank String processSerialNumber,
                                      @RequestParam @NotBlank String processInstanceIds) {
        Y9LoginUserHolder.setTenantId(tenantId);
        Position position = positionManager.get(tenantId, positionId).getData();
        Y9LoginUserHolder.setPosition(position);
        boolean b = associatedFileService.saveAssociatedFile(processSerialNumber, processInstanceIds);
        return b;
    }
}
