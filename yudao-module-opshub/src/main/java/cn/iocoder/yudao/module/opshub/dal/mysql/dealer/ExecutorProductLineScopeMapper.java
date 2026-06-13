package cn.iocoder.yudao.module.opshub.dal.mysql.dealer;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.opshub.dal.dataobject.dealer.ExecutorProductLineScopeDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Set;

@Mapper
public interface ExecutorProductLineScopeMapper extends BaseMapperX<ExecutorProductLineScopeDO> {

    default List<ExecutorProductLineScopeDO> selectListByUserId(Long userId) {
        return selectList(ExecutorProductLineScopeDO::getUserId, userId);
    }

    default int deleteByUserId(Long userId) {
        return delete(ExecutorProductLineScopeDO::getUserId, userId);
    }

    default Set<Long> selectProductLineIdsByUserId(Long userId) {
        List<ExecutorProductLineScopeDO> list = selectList(ExecutorProductLineScopeDO::getUserId, userId);
        return cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet(list, ExecutorProductLineScopeDO::getProductLineId);
    }

    default List<ExecutorProductLineScopeDO> selectListByProductLineId(Long productLineId) {
        return selectList(ExecutorProductLineScopeDO::getProductLineId, productLineId);
    }

    default Set<Long> selectUserIdsByProductLineId(Long productLineId) {
        List<ExecutorProductLineScopeDO> list = selectList(ExecutorProductLineScopeDO::getProductLineId, productLineId);
        return cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet(list, ExecutorProductLineScopeDO::getUserId);
    }

}
