package net.risesoft.api.itemadmin.position;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import net.risesoft.model.itemadmin.AttachmentModel;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/19
 */
public interface Attachment4PositionApi {

    /**
     * 根据流程编号删除附件
     *
     * @param tenantId 租户id
     * @param processSerialNumbers 流程编号
     * @return Y9Result<Object>
     */
    @PostMapping(value = "/delByProcessSerialNumbers", consumes = MediaType.APPLICATION_JSON_VALUE)
    Y9Result<Object> delBatchByProcessSerialNumbers(@RequestParam("tenantId") String tenantId, @RequestBody List<String> processSerialNumbers);

    /**
     * 删除附件
     *
     * @param tenantId 租户id
     * @param ids 附件ids
     * @return Y9Result<Object>
     */
    @PostMapping("/delFile")
    Y9Result<Object> delFile(@RequestParam("tenantId") String tenantId, @RequestParam("ids") String ids);

    /**
     * 附件数
     *
     * @param tenantId 租户id
     * @param processSerialNumber 流程编号
     * @return Y9Result<Integer>
     */
    @GetMapping("/fileCounts")
    Y9Result<Integer> fileCounts(@RequestParam("tenantId") String tenantId, @RequestParam("processSerialNumber") String processSerialNumber);

    /**
     * 附件下载
     *
     * @param tenantId 租户id
     * @param id 附件id
     * @return Y9Result<AttachmentModel>
     */
    @GetMapping("/findById")
    Y9Result<AttachmentModel> findById(@RequestParam("tenantId") String tenantId, @RequestParam("id") String id);

    /**
     * 获取附件数
     *
     * @param tenantId 租户id
     * @param processSerialNumber 流程编号
     * @param fileSource 附件来源
     * @param fileType 文件类型
     * @return Y9Result<Integer>
     */
    @GetMapping("/getAttachmentCount")
    Y9Result<Integer> getAttachmentCount(@RequestParam("tenantId") String tenantId, @RequestParam("processSerialNumber") String processSerialNumber, @RequestParam("fileSource") String fileSource, @RequestParam("fileType") String fileType);

    /**
     * 获取附件列表
     *
     * @param tenantId 租户id
     * @param processSerialNumber 流程编号
     * @param fileSource 附件来源
     * @param page 页码
     * @param rows 行数
     * @return Y9Page<AttachmentModel>
     */
    @GetMapping("/getAttachmentList")
    Y9Page<AttachmentModel> getAttachmentList(@RequestParam("tenantId") String tenantId, @RequestParam("processSerialNumber") String processSerialNumber, @RequestParam("fileSource") String fileSource, @RequestParam("page") int page, @RequestParam("rows") int rows);

    /**
     * 获取附件列表(model)
     *
     * @param tenantId 租户id
     * @param processSerialNumber 流程编号
     * @param fileSource 附件来源
     * @return Y9Result<List < AttachmentModel>>
     */
    @GetMapping("/getAttachmentModelList")
    Y9Result<List<AttachmentModel>> getAttachmentModelList(@RequestParam("tenantId") String tenantId, @RequestParam("processSerialNumber") String processSerialNumber, @RequestParam("fileSource") String fileSource);

    /**
     * 获取附件
     *
     * @param tenantId 租户id
     * @param fileId 附件id
     * @return Y9Result<AttachmentModel>
     */
    @GetMapping("/getFile")
    Y9Result<AttachmentModel> getFile(@RequestParam("tenantId") String tenantId, @RequestParam("fileId") String fileId);

    /**
     * 保存附件信息
     *
     * @param tenantId 租户id
     * @param positionId 岗位id
     * @param attachjson 附件信息
     * @param processSerialNumber 流程编号
     * @return Y9Result<Object>
     */
    @PostMapping("/saveAttachment")
    Y9Result<Object> saveAttachment(@RequestParam("tenantId") String tenantId, @RequestParam("positionId") String positionId, @RequestParam("attachjson") String attachjson, @RequestParam("processSerialNumber") String processSerialNumber);

    /**
     * 保存附件信息
     *
     * @param tenantId 租户id
     * @param userId 用户id
     * @param fileName 文件名称
     * @param fileType 文件类型
     * @param fileSizeString 文件大小
     * @param fileSource 附件来源
     * @param processInstanceId 流程实例id
     * @param processSerialNumber 流程编号
     * @param taskId 任务id
     * @param y9FileStoreId 附件上传id
     * @return Y9Result<String>
     */
    @PostMapping("/saveOrUpdateUploadInfo")
    Y9Result<String> saveOrUpdateUploadInfo(@RequestParam("tenantId") String tenantId, @RequestParam("userId") String userId, @RequestParam("fileName") String fileName, @RequestParam("fileType") String fileType, @RequestParam("fileSizeString") String fileSizeString,
        @RequestParam("fileSource") String fileSource, @RequestParam("processInstanceId") String processInstanceId, @RequestParam("processSerialNumber") String processSerialNumber, @RequestParam("taskId") String taskId, @RequestParam("y9FileStoreId") String y9FileStoreId);

    /**
     * 更新附件
     *
     * @param tenantId 租户id
     * @param userId 人员id
     * @param positionId 岗位id
     * @param fileId 文件id
     * @param fileSize 文件大小
     * @param taskId 任务id
     * @param y9FileStoreId 附件上传id
     * @return Y9Result<String>
     */
    @PostMapping("/updateFile")
    Y9Result<String> updateFile(@RequestParam("tenantId") String tenantId, @RequestParam("userId") String userId, @RequestParam("positionId") String positionId, @RequestParam("fileId") String fileId, @RequestParam("fileSize") String fileSize, @RequestParam("taskId") String taskId,
        @RequestParam("y9FileStoreId") String y9FileStoreId);

    /**
     * 上传附件
     *
     * @param tenantId 租户id
     * @param userId 人员id
     * @param positionId 岗位id
     * @param fileName 文件名
     * @param fileSize 文件大小
     * @param processInstanceId 流程实例id
     * @param taskId 任务id
     * @param describes 描述
     * @param processSerialNumber 流程编号
     * @param fileSource 附件来源
     * @param y9FileStoreId 附件上传id
     * @return Y9Result<String>
     */
    @PostMapping("/upload")
    Y9Result<String> upload(@RequestParam("tenantId") String tenantId, @RequestParam("userId") String userId, @RequestParam("positionId") String positionId, @RequestParam("fileName") String fileName, @RequestParam("fileSize") String fileSize,
        @RequestParam("processInstanceId") String processInstanceId, @RequestParam("taskId") String taskId, @RequestParam("describes") String describes, @RequestParam("processSerialNumber") String processSerialNumber, @RequestParam("fileSource") String fileSource,
        @RequestParam("y9FileStoreId") String y9FileStoreId);

    /**
     * 上传附件(model)
     *
     * @param tenantId 租户id
     * @param positionId 岗位id
     * @param attachmentModel 附件实体信息
     * @return Y9Result<Object>
     * @throws Exception Exception
     */
    @PostMapping("/uploadModel")
    Y9Result<Object> uploadModel(@RequestParam("tenantId") String tenantId, @RequestParam("positionId") String positionId, @RequestBody AttachmentModel attachmentModel) throws Exception;
}
