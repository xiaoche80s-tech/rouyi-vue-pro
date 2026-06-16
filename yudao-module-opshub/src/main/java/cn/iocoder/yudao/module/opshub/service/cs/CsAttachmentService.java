package cn.iocoder.yudao.module.opshub.service.cs;

import cn.iocoder.yudao.module.opshub.controller.admin.cs.vo.CsAttachmentUploadReqVO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.cs.CsAttachmentDO;

import java.util.List;

/**
 * 通用附件 Service 接口
 */
public interface CsAttachmentService {

    /**
     * 上传附件（保存附件记录）
     */
    Long uploadAttachment(CsAttachmentUploadReqVO reqVO);

    /**
     * 根据模块和业务 ID 查询附件列表
     */
    List<CsAttachmentDO> getAttachmentList(String module, Long businessId);

    /**
     * 删除附件
     */
    void deleteAttachment(Long id);

}
