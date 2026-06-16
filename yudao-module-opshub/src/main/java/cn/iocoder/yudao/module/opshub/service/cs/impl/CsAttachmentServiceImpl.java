package cn.iocoder.yudao.module.opshub.service.cs.impl;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.opshub.controller.admin.cs.vo.CsAttachmentUploadReqVO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.cs.CsAttachmentDO;
import cn.iocoder.yudao.module.opshub.dal.mysql.cs.CsAttachmentMapper;
import cn.iocoder.yudao.module.opshub.enums.CsAttachmentModuleEnum;
import cn.iocoder.yudao.module.opshub.service.cs.CsAttachmentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.opshub.enums.ErrorCodeConstants.*;

/**
 * 通用附件 Service 实现类
 */
@Service
@Validated
@Slf4j
public class CsAttachmentServiceImpl implements CsAttachmentService {

    @Resource
    private CsAttachmentMapper csAttachmentMapper;

    @Override
    public Long uploadAttachment(CsAttachmentUploadReqVO reqVO) {
        // 校验模块
        if (CsAttachmentModuleEnum.getByCode(reqVO.getModule()) == null) {
            throw exception(CS_ATTACHMENT_UPLOAD_FAIL);
        }

        CsAttachmentDO attachmentDO = BeanUtils.toBean(reqVO, CsAttachmentDO.class);
        csAttachmentMapper.insert(attachmentDO);
        return attachmentDO.getId();
    }

    @Override
    public List<CsAttachmentDO> getAttachmentList(String module, Long businessId) {
        return csAttachmentMapper.selectListByModuleAndBusinessId(module, businessId);
    }

    @Override
    public void deleteAttachment(Long id) {
        // 校验存在
        CsAttachmentDO attachment = csAttachmentMapper.selectById(id);
        if (attachment == null) {
            throw exception(CS_ATTACHMENT_NOT_EXISTS);
        }
        csAttachmentMapper.deleteById(id);
    }

}
