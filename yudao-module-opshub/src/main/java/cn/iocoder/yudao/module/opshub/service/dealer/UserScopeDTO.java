package cn.iocoder.yudao.module.opshub.service.dealer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 用户授权范围 DTO（Service 层内部使用）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserScopeDTO {

    /** 用户ID */
    private Long userId;

    /** 授权的业务编码集合（产品线编码或经销商编码） */
    private Set<String> codes;

}
