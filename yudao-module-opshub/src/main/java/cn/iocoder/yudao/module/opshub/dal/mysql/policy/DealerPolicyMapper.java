package cn.iocoder.yudao.module.opshub.dal.mysql.policy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.opshub.controller.admin.policy.vo.DealerPolicyPageReqVO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.policy.DealerPolicyDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DealerPolicyMapper extends BaseMapperX<DealerPolicyDO> {

    default PageResult<DealerPolicyDO> selectPage(DealerPolicyPageReqVO reqVO) {
        LambdaQueryWrapperX<DealerPolicyDO> wrapper = new LambdaQueryWrapperX<DealerPolicyDO>()
                .inIfPresent(DealerPolicyDO::getPolicyType, reqVO.getPolicyTypes())
                .inIfPresent(DealerPolicyDO::getAchievementType, reqVO.getAchievementTypes())
                .inIfPresent(DealerPolicyDO::getPolicyStatus, reqVO.getStatuses())
                .eqIfPresent(DealerPolicyDO::getDealerId, reqVO.getDealerId())
                .orderByDesc(DealerPolicyDO::getId);

        // 关键词搜索
        if (StrUtil.isNotEmpty(reqVO.getKeyword())) {
            wrapper.and(w -> w
                    .like(DealerPolicyDO::getPolicyCode, reqVO.getKeyword())
                    .or().like(DealerPolicyDO::getPolicyName, reqVO.getKeyword()));
        }

        return selectPage(reqVO, wrapper);
    }

    default DealerPolicyDO selectByPolicyCode(String policyCode) {
        return selectOne(DealerPolicyDO::getPolicyCode, policyCode);
    }

    /**
     * 查询指定年份下所有政策编码，取最大序号（用于编码自动生成）
     */
    default Integer selectMaxSeq(int year) {
        String prefix = "POL-" + year + "-";
        List<DealerPolicyDO> list = selectList(new LambdaQueryWrapper<DealerPolicyDO>()
                .likeRight(DealerPolicyDO::getPolicyCode, prefix)
                .orderByDesc(DealerPolicyDO::getPolicyCode));
        if (CollUtil.isEmpty(list)) {
            return 0;
        }
        String maxCode = list.get(0).getPolicyCode();
        String seqStr = maxCode.substring(maxCode.lastIndexOf("-") + 1);
        try {
            return Integer.parseInt(seqStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

}
