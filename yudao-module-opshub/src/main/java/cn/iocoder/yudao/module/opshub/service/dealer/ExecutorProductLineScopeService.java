package cn.iocoder.yudao.module.opshub.service.dealer;

import java.util.Set;

/**
 * 执行员-产品线授权 Service 接口
 */
public interface ExecutorProductLineScopeService {

    /**
     * 获取用户授权的产品线 Code 集合
     */
    Set<String> getProductLineCodesByUserId(Long userId);

    /**
     * 分配用户产品线授权（全量替换）
     */
    void assign(Long userId, Set<String> productLineCodes);

    /**
     * 获取产品线关联的所有执行员 userId 集合
     */
    Set<Long> getUserIdsByProductLineCode(String productLineCode);

}
