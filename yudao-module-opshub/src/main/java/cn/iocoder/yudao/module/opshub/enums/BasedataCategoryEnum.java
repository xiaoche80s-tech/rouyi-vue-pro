package cn.iocoder.yudao.module.opshub.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 基础数据文件分类枚举
 */
@Getter
@AllArgsConstructor
public enum BasedataCategoryEnum {

    QUALIFICATION("qualification", "资质文件"),
    AUTHORIZATION("authorization", "授权文件"),
    CONTRACT("contract", "合同文件"),
    PRODUCT("product", "产品文件");

    /**
     * 分类编码
     */
    private final String category;
    /**
     * 分类名称
     */
    private final String name;

}
