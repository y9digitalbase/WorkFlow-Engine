package net.risesoft.repository.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import net.risesoft.entity.ItemInterfaceTaskBind;

/**
 *
 * @author zhangchongjie
 * @date 2024/05/24
 */
@Transactional(value = "rsTenantTransactionManager", readOnly = true)
public interface ItemInterfaceTaskBindRepository
    extends JpaRepository<ItemInterfaceTaskBind, String>, JpaSpecificationExecutor<ItemInterfaceTaskBind> {

    @Modifying
    @Transactional(readOnly = false)
    void deleteByItemIdAndInterfaceId(String itemId, String interfaceId);

    List<ItemInterfaceTaskBind> findByItemIdAndTaskDefKeyAndProcessDefinitionIdAndExecuteConditionContaining(
        String itemId, String taskKey, String processDefinitionId, String condition);

    ItemInterfaceTaskBind findByTaskDefKeyAndItemIdAndProcessDefinitionIdAndInterfaceId(String elementKey,
        String itemId, String processDefinitionId, String interfaceId);

}
