package cn.iocoder.yudao.module.opshub.dal.mysql.policy;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.opshub.dal.dataobject.policy.DealerPolicyIndicatorDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DealerPolicyIndicatorMapper extends BaseMapperX<DealerPolicyIndicatorDO> {

    default List<DealerPolicyIndicatorDO> selectListByPolicyId(Long policyId) {
        return selectList(DealerPolicyIndicatorDO::getPolicyId, policyId);
    }

    /**
     * 查询所有去重指标名称
     */
    default List<String> selectDistinctIndicatorNames(Integer targetYear) {
        return selectObjs(new LambdaQueryWrapper<DealerPolicyIndicatorDO>()
                .eq(DealerPolicyIndicatorDO::getTargetYear, targetYear)
                .select(DealerPolicyIndicatorDO::getIndicatorName)
                .groupBy(DealerPolicyIndicatorDO::getIndicatorName));
    }

    /**
     * 按 policyCode + indicatorName + targetYear + targetMonth 查询（幂等）
     */
    default DealerPolicyIndicatorDO selectByPolicyCodeAndNameAndMonth(String policyCode, String indicatorName,
                                                                      Integer targetYear, Integer targetMonth) {
        return selectOne(new LambdaQueryWrapper<DealerPolicyIndicatorDO>()
                .eq(DealerPolicyIndicatorDO::getPolicyCode, policyCode)
                .eq(DealerPolicyIndicatorDO::getIndicatorName, indicatorName)
                .eq(DealerPolicyIndicatorDO::getTargetYear, targetYear)
                .eq(DealerPolicyIndicatorDO::getTargetMonth, targetMonth));
    }

    /**
     * 按 policyId 物理删除（级联删除）
     */
    default void deleteByPolicyId(Long policyId) {
        delete(new LambdaQueryWrapper<DealerPolicyIndicatorDO>()
                .eq(DealerPolicyIndicatorDO::getPolicyId, policyId));
    }

}
