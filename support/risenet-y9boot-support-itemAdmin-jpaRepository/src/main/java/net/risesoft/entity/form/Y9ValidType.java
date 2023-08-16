package net.risesoft.entity.form;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.hibernate.annotations.Comment;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/20
 */
@Entity
@Table(name = "Y9FORM_ValidType")
@Comment("校验规则定义")
@NoArgsConstructor
@Data
public class Y9ValidType implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1291770032218560221L;

    @Id
    @Column(name = "ID", length = 38)
    @Comment("主键")
    private String id;

    @Column(name = "VALIDNAME", length = 50)
    @Comment("校验名称")
    private String validName;

    @Column(name = "VALIDCNNAME", length = 50)
    @Comment("校验中文名称")
    private String validCnName;

    @Lob
    @Column(name = "VALIDCONTENT")
    @Comment("检验内容")
    private String validContent;

    /**
     * {@link #ItemFormTypeEnum}
     */
    @Column(name = "VALIDTYPE", length = 50)
    @Comment("校验类型")
    private String validType;

    @Column(name = "TENANTID", length = 38)
    @Comment("租户Id")
    private String tenantId;

    @Column(name = "PERSONID", length = 38)
    @Comment("修改人")
    private String personId;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "UPDATETIME")
    @Comment("修改时间")
    private Date updateTime;

}
