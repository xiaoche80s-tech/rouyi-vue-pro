package cn.iocoder.yudao.module.opshub.dal.mysql.dealer;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.opshub.dal.dataobject.dealer.DealerProductLineRelationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DealerProductLineRelationMapper extends BaseMapperX<DealerProductLineRelationDO> {

    default List<DealerProductLineRelationDO> selectListByDealerCode(String dealerCode) {
        return selectList(DealerProductLineRelationDO::getDealerCode, dealerCode);
    }

    default List<DealerProductLineRelationDO> selectListByProductLineCode(String productLineCode) {
        return selectList(DealerProductLineRelationDO::getProductLineCode, productLineCode);
    }

    default DealerProductLineRelationDO selectByDealerCodeAndProductLineCode(String dealerCode, String productLineCode) {
        return selectOne(DealerProductLineRelationDO::getDealerCode, dealerCode,
                DealerProductLineRelationDO::getProductLineCode, productLineCode);
    }

    default int deleteByDealerCodeAndProductLineCode(String dealerCode, String productLineCode) {
        return delete(new LambdaQueryWrapperX<DealerProductLineRelationDO>()
                .eq(DealerProductLineRelationDO::getDealerCode, dealerCode)
                .eq(DealerProductLineRelationDO::getProductLineCode, productLineCode));
    }

}
