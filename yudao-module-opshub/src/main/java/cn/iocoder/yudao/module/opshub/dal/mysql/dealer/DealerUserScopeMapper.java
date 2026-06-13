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

    default List<DealerUserScopeDO> selectListByDealerId(Long dealerId) {
        return selectList(DealerUserScopeDO::getDealerId, dealerId);
    }

    default DealerUserScopeDO selectByUserIdAndDealerId(Long userId, Long dealerId) {
        return selectOne(DealerUserScopeDO::getUserId, userId,
                DealerUserScopeDO::getDealerId, dealerId);
    }

    default int deleteByUserId(Long userId) {
        return delete(DealerUserScopeDO::getUserId, userId);
    }

    default Set<Long> selectDealerIdsByUserId(Long userId) {
        List<DealerUserScopeDO> list = selectList(DealerUserScopeDO::getUserId, userId);
        return cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet(list, DealerUserScopeDO::getDealerId);
    }

    default Set<Long> selectUserIdsByDealerId(Long dealerId) {
        List<DealerUserScopeDO> list = selectList(DealerUserScopeDO::getDealerId, dealerId);
        return cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet(list, DealerUserScopeDO::getUserId);
    }

}
