package cn.iocoder.yudao.module.opshub.dal.dataobject.signing;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 签约合同 DO
 */
@TableName("ops_signing_contract")
@KeySequence("ops_signing_contract_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class SigningContractDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 经销商 ID
     */
    private Long dealerId;
    /**
     * 经销商编码（数据权限用）
     */
    private String dealerCode;
    /**
     * 产品线编码（数据权限用，可空）
     */
    private String productLineCode;
    /**
     * 合同类型
     *
     * 枚举 {@link cn.iocoder.yudao.module.opshub.enums.ContractTypeEnum}
     */
    private String contractType;
    /**
     * 合同类型中文名
     */
    private String contractTypeName;
    /**
     * 合同编码（唯一，系统自动生成）
     */
    private String contractCode;
    /**
     * 合同名称
     */
    private String contractName;
    /**
     * 签署状态
     *
     * 枚举 {@link cn.iocoder.yudao.module.opshub.enums.ContractStatusEnum}
     */
    private String status;
    /**
     * 子状态（仅 unsigned 时有效）
     *
     * 枚举 {@link cn.iocoder.yudao.module.opshub.enums.ContractSubStatusEnum}
     */
    private String subStatus;
    /**
     * 下发日期
     */
    private LocalDate issuedDate;
    /**
     * 签署日期
     */
    private LocalDate signDate;
    /**
     * 合同摘要
     */
    private String summary;
    /**
     * 政策解析（仅 policy 类型）
     */
    private String policyAnalysis;
    /**
     * 政策指标 JSON 数组（仅 policy 类型）
     */
    private String indicators;
    /**
     * 签署凭证 URL（执行员上传盖章文件）
     */
    private String signProofUrl;
    /**
     * 备注
     */
    private String remark;

}
