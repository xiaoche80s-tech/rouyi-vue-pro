package cn.iocoder.yudao.module.opshub.dal.mysql.dealer;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.opshub.dal.dataobject.dealer.DealerProductLineRelationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DealerProductLineRelationMapper extends BaseMapperX<DealerProductLineRelationDO> {

    default List<DealerProductLineRelationDO> selectListByDealerId(Long dealerId) {
        return selectList(DealerProductLineRelationDO::getDealerId, dealerId);
    }

    default List<DealerProductLineRelationDO> selectListByProductLineId(Long productLineId) {
        return selectList(DealerProductLineRelationDO::getProductLineId, productLineId);
    }

    default DealerProductLineRelationDO selectByDealerIdAndProductLineId(Long dealerId, Long productLineId) {
        return selectOne(DealerProductLineRelationDO::getDealerId, dealerId,
                DealerProductLineRelationDO::getProductLineId, productLineId);
    }

    default int deleteByDealerIdAndProductLineId(Long dealerId, Long productLineId) {
        return delete(new LambdaQueryWrapperX<DealerProductLineRelationDO>()
                .eq(DealerProductLineRelationDO::getDealerId, dealerId)
                .eq(DealerProductLineRelationDO::getProductLineId, productLineId));
    }

}
