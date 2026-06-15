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
    public Set<String> getProductLineCodesByUserId(Long userId) {
        return executorPlScopeMapper.selectProductLineCodesByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assign(Long userId, Set<String> productLineCodes) {
        // 1. 先删除旧的授权
        executorPlScopeMapper.deleteByUserId(userId);
        // 2. 批量插入新的授权
        if (productLineCodes != null && !productLineCodes.isEmpty()) {
            for (String productLineCode : productLineCodes) {
                ExecutorProductLineScopeDO scope = new ExecutorProductLineScopeDO();
                scope.setUserId(userId);
                scope.setProductLineCode(productLineCode);
                executorPlScopeMapper.insert(scope);
            }
        }
    }

    @Override
    public Set<Long> getUserIdsByProductLineCode(String productLineCode) {
        return executorPlScopeMapper.selectUserIdsByProductLineCode(productLineCode);
    }

}
