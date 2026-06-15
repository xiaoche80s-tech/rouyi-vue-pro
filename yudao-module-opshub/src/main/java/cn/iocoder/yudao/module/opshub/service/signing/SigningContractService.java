package cn.iocoder.yudao.module.opshub.service.signing;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.opshub.controller.admin.signing.vo.*;
import cn.iocoder.yudao.module.opshub.dal.dataobject.signing.SigningContractDO;

import java.util.List;

/**
 * 签约合同 Service 接口
 */
public interface SigningContractService {

    /**
     * 创建合同（自动生成编码）
     */
    Long createSigningContract(SigningContractCreateReqVO reqVO);

    /**
     * 更新合同
     */
    void updateSigningContract(SigningContractUpdateReqVO reqVO);

    /**
     * 获得合同详情
     */
    SigningContractDO getSigningContract(Long id);

    /**
     * 获得合同分页
     */
    PageResult<SigningContractDO> getSigningContractPage(SigningContractPageReqVO reqVO);

    /**
     * 获得统计数据
     */
    SigningContractStatisticsRespVO getStatistics();

    /**
     * 获得趋势数据
     */
    List<SigningContractTrendRespVO> getTrend(String timeDimension);

    /**
     * 经销商发起签署（sub_status → signing）
     */
    void signContract(Long id);

    /**
     * 执行员上传盖章文件（回填 sign_proof_url，status → signed）
     */
    void uploadSignProof(Long id, String signProofUrl);

}
