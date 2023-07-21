package net.risesoft.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.risesoft.entity.ErrorLog;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.repository.jpa.ErrorLogRepository;
import net.risesoft.service.ErrorLogService;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/20
 */
@Service(value = "errorLogService")
@Transactional(value = "rsTenantTransactionManager", readOnly = true)
public class ErrorLogServiceImpl implements ErrorLogService {

    @Autowired
    private ErrorLogRepository errorLogRepository;

    @Override
    @Transactional(readOnly = false)
    public void saveErrorLog(ErrorLog errorLog) {
        try {
            if (StringUtils.isBlank(errorLog.getId())) {
                errorLog.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
            }
            errorLogRepository.save(errorLog);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
