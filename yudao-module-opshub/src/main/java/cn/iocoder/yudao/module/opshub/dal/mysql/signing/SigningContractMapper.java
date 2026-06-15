package cn.iocoder.yudao.module.opshub.dal.mysql.signing;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.opshub.controller.admin.signing.vo.SigningContractPageReqVO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.signing.SigningContractDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface SigningContractMapper extends BaseMapperX<SigningContractDO> {

    default PageResult<SigningContractDO> selectPage(SigningContractPageReqVO reqVO) {
        LambdaQueryWrapperX<SigningContractDO> wrapper = new LambdaQueryWrapperX<SigningContractDO>()
                .inIfPresent(SigningContractDO::getProductLineCode, reqVO.getProductLineCodes())
                .inIfPresent(SigningContractDO::getContractType, reqVO.getContractTypes())
                .inIfPresent(SigningContractDO::getStatus, reqVO.getStatuses())
                .inIfPresent(SigningContractDO::getDealerCode, reqVO.getDealerCodes())
                .orderByDesc(SigningContractDO::getId);

        // 时间维度过滤
        applyTimeFilter(wrapper, reqVO);

        // 关键词搜索
        applyKeywordFilter(wrapper, reqVO.getKeyword());

        return selectPage(reqVO, wrapper);
    }

    /**
     * 统计：按合同类型 + 状态 GROUP BY
     */
    default List<Map<String, Object>> selectStatistics() {
        return selectMaps(new LambdaQueryWrapper<SigningContractDO>()
                .select(SigningContractDO::getContractType, SigningContractDO::getStatus, SigningContractDO::getSubStatus)
                .groupBy(SigningContractDO::getContractType, SigningContractDO::getStatus, SigningContractDO::getSubStatus));
    }

    /**
     * 趋势查询：按月聚合（已签署合同）
     */
    default List<Map<String, Object>> selectTrendByMonth() {
        return selectMaps(new LambdaQueryWrapper<SigningContractDO>()
                .select(SigningContractDO::getContractType)
                .eq(SigningContractDO::getStatus, "signed")
                .groupBy(SigningContractDO::getContractType));
    }

    /**
     * 查询指定类型+年份的最大序号（用于编码自动生成）
     */
    default Integer selectMaxSeq(String contractType, int year) {
        // 查询该类型+年份下所有合同编码，取最大序号
        String prefix = "";
        switch (contractType) {
            case "main":
                prefix = "MC";
                break;
            case "policy":
                prefix = "POL";
                break;
            case "supplement":
                prefix = "SA";
                break;
            case "termination":
                prefix = "TA";
                break;
        }
        String pattern = prefix + "-" + year + "-%";
        List<SigningContractDO> list = selectList(new LambdaQueryWrapper<SigningContractDO>()
                .likeRight(SigningContractDO::getContractCode, prefix + "-" + year + "-")
                .orderByDesc(SigningContractDO::getContractCode));
        if (CollUtil.isEmpty(list)) {
            return 0;
        }
        // 从最大编码中提取序号
        String maxCode = list.get(0).getContractCode();
        String seqStr = maxCode.substring(maxCode.lastIndexOf("-") + 1);
        try {
            return Integer.parseInt(seqStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ========== 辅助方法 ==========

    private void applyTimeFilter(LambdaQueryWrapperX<SigningContractDO> wrapper, SigningContractPageReqVO reqVO) {
        String timeDimension = reqVO.getTimeDimension();
        if (StrUtil.isEmpty(timeDimension)) {
            return;
        }

        switch (timeDimension) {
            case "month":
                if (CollUtil.isNotEmpty(reqVO.getMonths())) {
                    // 按月份过滤 issued_date 的月份部分
                    wrapper.and(w -> {
                        for (int i = 0; i < reqVO.getMonths().size(); i++) {
                            Integer month = reqVO.getMonths().get(i);
                            if (i == 0) {
                                w.apply("EXTRACT(MONTH FROM issued_date) = {0}", month);
                            } else {
                                w.or().apply("EXTRACT(MONTH FROM issued_date) = {0}", month);
                            }
                        }
                    });
                }
                break;
            case "quarter":
                if (CollUtil.isNotEmpty(reqVO.getQuarters())) {
                    wrapper.and(w -> {
                        for (int i = 0; i < reqVO.getQuarters().size(); i++) {
                            Integer quarter = reqVO.getQuarters().get(i);
                            if (i == 0) {
                                w.apply("EXTRACT(QUARTER FROM issued_date) = {0}", quarter);
                            } else {
                                w.or().apply("EXTRACT(QUARTER FROM issued_date) = {0}", quarter);
                            }
                        }
                    });
                }
                break;
            case "year":
                if (CollUtil.isNotEmpty(reqVO.getYears())) {
                    wrapper.and(w -> {
                        for (int i = 0; i < reqVO.getYears().size(); i++) {
                            Integer year = reqVO.getYears().get(i);
                            if (i == 0) {
                                w.apply("EXTRACT(YEAR FROM issued_date) = {0}", year);
                            } else {
                                w.or().apply("EXTRACT(YEAR FROM issued_date) = {0}", year);
                            }
                        }
                    });
                }
                break;
            default:
                break;
        }
    }

    private void applyKeywordFilter(LambdaQueryWrapperX<SigningContractDO> wrapper, String keyword) {
        if (StrUtil.isEmpty(keyword)) {
            return;
        }
        wrapper.and(w -> w
                .like(SigningContractDO::getContractCode, keyword)
                .or().like(SigningContractDO::getContractName, keyword)
                .or().like(SigningContractDO::getDealerCode, keyword)
                .or().like(SigningContractDO::getSummary, keyword));
    }

}
