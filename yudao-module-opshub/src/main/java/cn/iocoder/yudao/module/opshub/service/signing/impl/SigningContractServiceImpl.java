package cn.iocoder.yudao.module.opshub.service.signing.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.opshub.controller.admin.signing.vo.*;
import cn.iocoder.yudao.module.opshub.dal.dataobject.dealer.DealerInfoDO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.signing.SigningContractDO;
import cn.iocoder.yudao.module.opshub.dal.mysql.signing.SigningContractMapper;
import cn.iocoder.yudao.module.opshub.enums.ContractStatusEnum;
import cn.iocoder.yudao.module.opshub.enums.ContractSubStatusEnum;
import cn.iocoder.yudao.module.opshub.enums.ContractTypeEnum;
import cn.iocoder.yudao.module.opshub.service.dealer.DealerInfoService;
import cn.iocoder.yudao.module.opshub.service.signing.SigningContractService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.opshub.enums.ErrorCodeConstants.*;

/**
 * 签约合同 Service 实现类
 */
@Service
@Validated
public class SigningContractServiceImpl implements SigningContractService {

    @Resource
    private SigningContractMapper signingContractMapper;

    @Resource
    private DealerInfoService dealerInfoService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSigningContract(SigningContractCreateReqVO reqVO) {
        // 1. 校验经销商存在
        DealerInfoDO dealer = dealerInfoService.getDealer(reqVO.getDealerId());
        if (dealer == null) {
            throw exception(SIGNING_CONTRACT_DEALER_NOT_EXISTS);
        }

        // 2. 自动生成合同编码
        String contractCode = generateContractCode(reqVO.getContractType(), reqVO.getIssuedDate());

        // 3. 构建 DO
        SigningContractDO contractDO = BeanUtils.toBean(reqVO, SigningContractDO.class);
        contractDO.setContractCode(contractCode);
        contractDO.setStatus(ContractStatusEnum.UNSIGNED.getCode());
        contractDO.setSubStatus(ContractSubStatusEnum.PENDING.getCode());

        // 4. 插入
        signingContractMapper.insert(contractDO);
        return contractDO.getId();
    }

    @Override
    public void updateSigningContract(SigningContractUpdateReqVO reqVO) {
        // 1. 校验存在
        SigningContractDO existDO = signingContractMapper.selectById(reqVO.getId());
        if (existDO == null) {
            throw exception(SIGNING_CONTRACT_NOT_EXISTS);
        }

        // 2. 更新
        SigningContractDO updateDO = BeanUtils.toBean(reqVO, SigningContractDO.class);
        signingContractMapper.updateById(updateDO);
    }

    @Override
    public SigningContractDO getSigningContract(Long id) {
        return signingContractMapper.selectById(id);
    }

    @Override
    public PageResult<SigningContractDO> getSigningContractPage(SigningContractPageReqVO reqVO) {
        return signingContractMapper.selectPage(reqVO);
    }

    @Override
    public SigningContractStatisticsRespVO getStatistics() {
        return getStatistics(null);
    }

    @Override
    public SigningContractStatisticsRespVO getStatistics(LocalDateTime startTime) {
        SigningContractStatisticsRespVO respVO = new SigningContractStatisticsRespVO();
        int totalCount = 0, signedCount = 0, unsignedCount = 0, pendingCount = 0, signingCount = 0;
        Map<String, int[]> typeStatsMap = new HashMap<>(); // type -> [total, signed, unsigned]

        // 查询数据（支持时间范围过滤）
        LambdaQueryWrapperX<SigningContractDO> wrapper = new LambdaQueryWrapperX<>();
        if (startTime != null) {
            wrapper.ge(SigningContractDO::getCreateTime, startTime);
        }
        List<SigningContractDO> allContracts = signingContractMapper.selectList(wrapper);
        totalCount = allContracts.size();

        for (SigningContractDO c : allContracts) {
            String type = c.getContractType();
            int[] typeStats = typeStatsMap.computeIfAbsent(type, k -> new int[]{0, 0, 0});
            typeStats[0]++;

            if (ContractStatusEnum.SIGNED.getCode().equals(c.getStatus())) {
                signedCount++;
                typeStats[1]++;
            } else {
                unsignedCount++;
                typeStats[2]++;
                if (ContractSubStatusEnum.PENDING.getCode().equals(c.getSubStatus())) {
                    pendingCount++;
                } else if (ContractSubStatusEnum.SIGNING.getCode().equals(c.getSubStatus())) {
                    signingCount++;
                }
            }
        }

        respVO.setTotalCount(totalCount);
        respVO.setSignedCount(signedCount);
        respVO.setUnsignedCount(unsignedCount);
        respVO.setPendingCount(pendingCount);
        respVO.setSigningCount(signingCount);
        respVO.setSignedRate(totalCount > 0
                ? BigDecimal.valueOf(signedCount).multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalCount), 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);

        // 各类型数量
        respVO.setMainCount(typeStatsMap.getOrDefault("main", new int[]{0, 0, 0})[0]);
        respVO.setPolicyCount(typeStatsMap.getOrDefault("policy", new int[]{0, 0, 0})[0]);
        respVO.setSupplementCount(typeStatsMap.getOrDefault("supplement", new int[]{0, 0, 0})[0]);
        respVO.setTerminationCount(typeStatsMap.getOrDefault("termination", new int[]{0, 0, 0})[0]);

        // 各类型统计
        respVO.setMain(buildTypeStat(typeStatsMap.get("main")));
        respVO.setPolicy(buildTypeStat(typeStatsMap.get("policy")));
        respVO.setSupplement(buildTypeStat(typeStatsMap.get("supplement")));
        respVO.setTermination(buildTypeStat(typeStatsMap.get("termination")));

        return respVO;
    }

    @Override
    public List<SigningContractTrendRespVO> getTrend(String timeDimension) {
        List<SigningContractDO> signedContracts = signingContractMapper.selectList(
                new LambdaQueryWrapperX<SigningContractDO>()
                        .eq(SigningContractDO::getStatus, ContractStatusEnum.SIGNED.getCode()));

        // 按时间维度分组
        Map<String, List<SigningContractDO>> grouped = new TreeMap<>();
        for (SigningContractDO c : signedContracts) {
            if (c.getIssuedDate() == null) continue;
            String period = formatPeriod(c.getIssuedDate(), timeDimension);
            grouped.computeIfAbsent(period, k -> new ArrayList<>()).add(c);
        }

        List<SigningContractTrendRespVO> result = new ArrayList<>();
        for (Map.Entry<String, List<SigningContractDO>> entry : grouped.entrySet()) {
            SigningContractTrendRespVO vo = new SigningContractTrendRespVO();
            vo.setPeriod(entry.getKey());
            int main = 0, policy = 0, supplement = 0, termination = 0;
            for (SigningContractDO c : entry.getValue()) {
                switch (c.getContractType()) {
                    case "main": main++; break;
                    case "policy": policy++; break;
                    case "supplement": supplement++; break;
                    case "termination": termination++; break;
                }
            }
            vo.setMainCount(main);
            vo.setPolicyCount(policy);
            vo.setSupplementCount(supplement);
            vo.setTerminationCount(termination);
            vo.setTotalCount(main + policy + supplement + termination);
            result.add(vo);
        }
        return result;
    }

    // ========== 辅助方法 ==========

    /**
     * 生成合同编码
     */
    @Transactional(rollbackFor = Exception.class)
    public String generateContractCode(String contractType, LocalDate issuedDate) {
        ContractTypeEnum typeEnum = ContractTypeEnum.getByCode(contractType);
        if (typeEnum == null) {
            throw exception(SIGNING_CONTRACT_NOT_EXISTS);
        }
        int year = issuedDate.getYear();
        Integer maxSeq = signingContractMapper.selectMaxSeq(contractType, year);
        int nextSeq = (maxSeq != null ? maxSeq : 0) + 1;
        return String.format("%s-%d-%03d", typeEnum.getPrefix(), year, nextSeq);
    }

    private SigningContractStatisticsRespVO.ContractTypeStat buildTypeStat(int[] stats) {
        SigningContractStatisticsRespVO.ContractTypeStat stat = new SigningContractStatisticsRespVO.ContractTypeStat();
        if (stats != null) {
            stat.setTotal(stats[0]);
            stat.setSigned(stats[1]);
            stat.setUnsigned(stats[2]);
        } else {
            stat.setTotal(0);
            stat.setSigned(0);
            stat.setUnsigned(0);
        }
        return stat;
    }

    private String formatPeriod(LocalDate date, String timeDimension) {
        if ("quarter".equals(timeDimension)) {
            int quarter = (date.getMonthValue() - 1) / 3 + 1;
            return date.getYear() + "-Q" + quarter;
        } else if ("year".equals(timeDimension)) {
            return String.valueOf(date.getYear());
        } else {
            // month
            return String.format("%d-%02d", date.getYear(), date.getMonthValue());
        }
    }

}
