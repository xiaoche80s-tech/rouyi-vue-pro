package cn.iocoder.yudao.module.opshub.dal.mysql.dealer;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.opshub.dal.dataobject.dealer.DealerUserScopeDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Set;

@Mapper
public interface DealerUserScopeMapper extends BaseMapperX<DealerUserScopeDO> {

    default List<DealerUserScopeDO> selectListByUserId(Long userId) {
        return selectList(DealerUserScopeDO::getUserId, userId);
    }

    default List<DealerUserScopeDO> selectListByDealerCode(String dealerCode) {
        return selectList(DealerUserScopeDO::getDealerCode, dealerCode);
    }

    default DealerUserScopeDO selectByUserIdAndDealerCode(Long userId, String dealerCode) {
        return selectOne(DealerUserScopeDO::getUserId, userId,
                DealerUserScopeDO::getDealerCode, dealerCode);
    }

    default int deleteByUserId(Long userId) {
        return delete(DealerUserScopeDO::getUserId, userId);
    }

    default Set<String> selectDealerCodesByUserId(Long userId) {
        List<DealerUserScopeDO> list = selectList(DealerUserScopeDO::getUserId, userId);
        return cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet(list, DealerUserScopeDO::getDealerCode);
    }

    default Set<Long> selectUserIdsByDealerCode(String dealerCode) {
        List<DealerUserScopeDO> list = selectList(DealerUserScopeDO::getDealerCode, dealerCode);
        return cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet(list, DealerUserScopeDO::getUserId);
    }

}
