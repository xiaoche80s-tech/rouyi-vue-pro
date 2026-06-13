package cn.iocoder.yudao.module.opshub.service.dealer.impl;

import cn.iocoder.yudao.module.opshub.dal.dataobject.dealer.ExecutorProductLineScopeDO;
import cn.iocoder.yudao.module.opshub.dal.mysql.dealer.ExecutorProductLineScopeMapper;
import cn.iocoder.yudao.module.opshub.service.dealer.ExecutorProductLineScopeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

/**
 * 执行员-产品线授权 Service 实现类
 */
@Service
@Validated
public class ExecutorProductLineScopeServiceImpl implements ExecutorProductLineScopeService {

    @Resource
    private ExecutorProductLineScopeMapper executorPlScopeMapper;

    @Override
    public Set<Long> getProductLineIdsByUserId(Long userId) {
        return executorPlScopeMapper.selectProductLineIdsByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assign(Long userId, Set<Long> productLineIds) {
        // 1. 先删除旧的授权
        executorPlScopeMapper.deleteByUserId(userId);
        // 2. 批量插入新的授权
        if (productLineIds != null && !productLineIds.isEmpty()) {
            for (Long productLineId : productLineIds) {
                ExecutorProductLineScopeDO scope = new ExecutorProductLineScopeDO();
                scope.setUserId(userId);
                scope.setProductLineId(productLineId);
                executorPlScopeMapper.insert(scope);
            }
        }
    }

    @Override
    public Set<Long> getUserIdsByProductLineId(Long productLineId) {
        return executorPlScopeMapper.selectUserIdsByProductLineId(productLineId);
    }

}
