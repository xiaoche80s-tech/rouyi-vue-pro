package cn.iocoder.yudao.module.opshub.dal.mysql.oprequest;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.opshub.dal.dataobject.oprequest.OpRequestSigningDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作请求-签约子表 Mapper
 */
@Mapper
public interface OpRequestSigningMapper extends BaseMapperX<OpRequestSigningDO> {

    default OpRequestSigningDO selectByRequestId(Long requestId) {
        return selectOne(OpRequestSigningDO::getRequestId, requestId);
    }

    default OpRequestSigningDO selectByContractId(Long contractId) {
        return selectOne(OpRequestSigningDO::getContractId, contractId);
    }

}
