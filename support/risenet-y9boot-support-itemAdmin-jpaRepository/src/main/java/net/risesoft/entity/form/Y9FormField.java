package net.risesoft.entity.form;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.Comment;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/20
 */
@Entity
@Accessors(chain = true)
@Table(name = "Y9FORM_FIELD")
@Comment("表单字段绑定")
@NoArgsConstructor
@Data
public class Y9FormField implements Serializable {
    private static final long serialVersionUID = -1137482366856338734L;

    @Id
    @Column(name = "ID", length = 38)
    @Comment("主键")
    private String id;

    @Column(name = "FORMID", length = 38)
    @Comment("表单Id")
    private String formId;

    @Column(name = "TABLEID", length = 50)
    @Comment("对应的表id")
    private String tableId;

    @Column(name = "TABLENAME", length = 50)
    @Comment("对应的表名称")
    private String tableName;

    @Column(name = "FIELDNAME", length = 50)
    @Comment("字段名称")
    private String fieldName;

    @Column(name = "FIELDCNNAME", length = 50)
    @Comment("字段中文名称")
    private String fieldCnName;

    @Column(name = "FIELDTYPE", length = 100)
    @Comment("字段类型")
    private String fieldType;

}
