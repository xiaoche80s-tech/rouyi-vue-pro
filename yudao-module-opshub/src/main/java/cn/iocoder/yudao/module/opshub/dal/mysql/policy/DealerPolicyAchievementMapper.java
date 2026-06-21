package cn.iocoder.yudao.module.opshub.dal.mysql.policy;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.opshub.controller.admin.policy.vo.DealerPolicyAchievementPageReqVO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.policy.DealerPolicyAchievementDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DealerPolicyAchievementMapper extends BaseMapperX<DealerPolicyAchievementDO> {

    default PageResult<DealerPolicyAchievementDO> selectPage(DealerPolicyAchievementPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DealerPolicyAchievementDO>()
                .eqIfPresent(DealerPolicyAchievementDO::getIndicatorId, reqVO.getIndicatorId())
                .eqIfPresent(DealerPolicyAchievementDO::getAchieveLevel, reqVO.getAchieveLevel())
                .orderByDesc(DealerPolicyAchievementDO::getId));
    }

    /**
     * 下钻查询：按 targetYear + indicatorId + level + 可选 province/hospital
     */
    default List<DealerPolicyAchievementDO> selectDrilldown(Integer targetYear, Long indicatorId, String level,
                                                             String province, String hospital) {
        LambdaQueryWrapperX<DealerPolicyAchievementDO> wrapper = new LambdaQueryWrapperX<DealerPolicyAchievementDO>()
                .eq(DealerPolicyAchievementDO::getTargetYear, targetYear)
                .eq(DealerPolicyAchievementDO::getIndicatorId, indicatorId)
                .eq(DealerPolicyAchievementDO::getAchieveLevel, level)
                .eqIfPresent(DealerPolicyAchievementDO::getProvince, province)
                .eqIfPresent(DealerPolicyAchievementDO::getHospital, hospital)
                .orderByDesc(DealerPolicyAchievementDO::getAchievedValue);
        return selectList(wrapper);
    }

    /**
     * 按 indicatorId 物理删除
     */
    default void deleteByIndicatorId(Long indicatorId) {
        delete(new LambdaQueryWrapper<DealerPolicyAchievementDO>()
                .eq(DealerPolicyAchievementDO::getIndicatorId, indicatorId));
    }

}
