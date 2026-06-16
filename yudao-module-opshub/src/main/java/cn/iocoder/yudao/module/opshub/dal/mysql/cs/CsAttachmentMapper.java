package cn.iocoder.yudao.module.opshub.dal.mysql.cs;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.opshub.dal.dataobject.cs.CsAttachmentDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CsAttachmentMapper extends BaseMapperX<CsAttachmentDO> {

    /**
     * 根据模块和业务 ID 查询附件列表
     */
    default List<CsAttachmentDO> selectListByModuleAndBusinessId(String module, Long businessId) {
        return selectList(new LambdaQueryWrapperX<CsAttachmentDO>()
                .eq(CsAttachmentDO::getModule, module)
                .eq(CsAttachmentDO::getBusinessId, businessId)
                .orderByDesc(CsAttachmentDO::getId));
    }

}
