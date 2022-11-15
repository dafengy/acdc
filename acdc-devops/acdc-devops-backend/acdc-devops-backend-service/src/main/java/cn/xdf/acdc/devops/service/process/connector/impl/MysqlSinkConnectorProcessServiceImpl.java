package cn.xdf.acdc.devops.service.process.connector.impl;

import cn.xdf.acdc.devops.core.domain.entity.enumeration.DataSystemType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MysqlSinkConnectorProcessServiceImpl extends AbstractJdbcSinkConnectorProcessServiceImpl {

    @Override
    public DataSystemType dataSystemType() {
        return DataSystemType.MYSQL;
    }
}
