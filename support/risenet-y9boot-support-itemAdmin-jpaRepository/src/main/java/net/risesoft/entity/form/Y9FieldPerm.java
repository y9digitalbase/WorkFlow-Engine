package net.risesoft.entity.form;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;

import org.hibernate.annotations.Comment;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/20
 */
@Entity
@Table(name = "Y9FORM_FIELD_PERM")
@Comment("字段权限配置")
@NoArgsConstructor
@Data
public class Y9FieldPerm implements Serializable {
    private static final long serialVersionUID = -1137482366856338734L;

    @Id
    @Column(name = "ID", length = 38)
    @Comment("主键")
    private String id;

    @Column(name = "FORMID", length = 38)
    @Comment("表单Id")
    private String formId;

    @Column(name = "FIELDNAME", length = 100)
    @Comment("字段名称")
    private String fieldName;

    @Column(name = "WRITEROLEID", length = 200)
    @Comment("写权限角色id")
    private String writeRoleId;

    @Column(name = "WRITEROLENAME", length = 200)
    @Comment("写权限角色名称")
    private String writeRoleName;

    @Column(name = "PROCESSDEFINITIONID", length = 200)
    @Comment("流程定义id")
    private String processDefinitionId;

    @Column(name = "TASKDEFKEY", length = 200)
    @Comment("任务key")
    private String taskDefKey;

}
