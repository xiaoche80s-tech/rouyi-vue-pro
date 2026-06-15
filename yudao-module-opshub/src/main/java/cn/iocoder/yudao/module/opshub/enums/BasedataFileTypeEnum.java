package cn.iocoder.yudao.module.opshub.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 基础数据文件子类型枚举
 */
@Getter
@AllArgsConstructor
public enum BasedataFileTypeEnum {

    // ========== 资质文件 ==========
    BL("BL", "营业执照", BasedataCategoryEnum.QUALIFICATION),
    JYXK("JYXK", "经营许可证", BasedataCategoryEnum.QUALIFICATION),
    BA("BA", "备案凭证", BasedataCategoryEnum.QUALIFICATION),
    QMS("QMS", "质量体系认证", BasedataCategoryEnum.QUALIFICATION),

    // ========== 授权文件 ==========
    SQ("SQ", "品牌授权书", BasedataCategoryEnum.AUTHORIZATION),

    // ========== 合同文件 ==========
    MC("MC", "主合同", BasedataCategoryEnum.CONTRACT),
    POL("POL", "政策合同", BasedataCategoryEnum.CONTRACT),
    SA("SA", "补充协议", BasedataCategoryEnum.CONTRACT),
    TA("TA", "终止协议", BasedataCategoryEnum.CONTRACT),

    // ========== 产品文件 ==========
    ZCZ("ZCZ", "注册证", BasedataCategoryEnum.PRODUCT),
    HGZ("HGZ", "合格证", BasedataCategoryEnum.PRODUCT),
    SMS("SMS", "说明书", BasedataCategoryEnum.PRODUCT),
    JS("JS", "技术文件", BasedataCategoryEnum.PRODUCT),
    JCBG("JCBG", "检测报告", BasedataCategoryEnum.PRODUCT);

    /**
     * 子类型编码
     */
    private final String code;
    /**
     * 子类型名称
     */
    private final String name;
    /**
     * 所属分类
     */
    private final BasedataCategoryEnum category;

}
