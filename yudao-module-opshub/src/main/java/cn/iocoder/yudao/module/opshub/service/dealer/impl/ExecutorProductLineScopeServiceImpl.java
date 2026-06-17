package cn.iocoder.yudao.module.opshub.service.dealer.impl;

import cn.iocoder.yudao.module.opshub.dal.dataobject.dealer.ExecutorProductLineScopeDO;
import cn.iocoder.yudao.module.opshub.dal.mysql.dealer.ExecutorProductLineScopeMapper;
import cn.iocoder.yudao.module.opshub.service.dealer.ExecutorProductLineScopeService;
import cn.iocoder.yudao.module.opshub.service.dealer.UserScopeDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

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

    @Override
    public List<UserScopeDTO> getAllUserProductLineScopes() {
        // 查全表，按 userId 分组
        List<ExecutorProductLineScopeDO> allScopes = executorPlScopeMapper.selectList();
        Map<Long, Set<String>> userCodeMap = allScopes.stream()
                .collect(Collectors.groupingBy(
                        ExecutorProductLineScopeDO::getUserId,
                        Collectors.mapping(ExecutorProductLineScopeDO::getProductLineCode, Collectors.toSet())
                ));
        List<UserScopeDTO> result = new ArrayList<>();
        userCodeMap.forEach((userId, codes) -> result.add(new UserScopeDTO(userId, codes)));
        return result;
    }

}
