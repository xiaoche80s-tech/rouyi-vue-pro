package cn.iocoder.yudao.module.opshub.service.basedata.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import cn.iocoder.yudao.module.opshub.controller.admin.basedata.vo.BasedataFilePageReqVO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.basedata.BasedataFileDO;
import cn.iocoder.yudao.module.opshub.dal.mysql.basedata.BasedataFileMapper;
import cn.iocoder.yudao.module.opshub.enums.BasedataFileTypeEnum;
import cn.iocoder.yudao.module.opshub.service.basedata.BasedataFileService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.opshub.enums.ErrorCodeConstants.*;

/**
 * 基础数据文件 Service 实现类
 */
@Service
@Validated
public class BasedataFileServiceImpl implements BasedataFileService {

    /**
     * 预签名 URL 有效期（秒）
     */
    private static final Integer PRESIGN_EXPIRE_SECONDS = 300;

    @Resource
    private BasedataFileMapper basedataFileMapper;

    @Resource
    private FileApi fileApi;

    @Override
    public PageResult<BasedataFileDO> getBasedataFilePage(BasedataFilePageReqVO pageReqVO) {
        return basedataFileMapper.selectPage(pageReqVO);
    }

    @Override
    public BasedataFileDO getBasedataFile(Long id) {
        return basedataFileMapper.selectById(id);
    }

    @Override
    public String getDownloadUrl(Long id) {
        // 1. 校验文件存在
        BasedataFileDO file = basedataFileMapper.selectById(id);
        if (file == null) {
            throw exception(BASEDATA_FILE_NOT_EXISTS);
        }
        // 2. 校验文件 URL 不为空
        if (StrUtil.isEmpty(file.getFileUrl())) {
            return null;
        }
        // 3. 生成预签名 URL
        return fileApi.presignGetUrl(file.getFileUrl(), PRESIGN_EXPIRE_SECONDS);
    }

    @Override
    public Map<String, Long> getCategoryCount() {
        // 查询所有文件
        List<BasedataFileDO> allFiles = basedataFileMapper.selectList();
        // 按分类统计
        Map<String, Long> countMap = new HashMap<>();
        for (BasedataFileDO file : allFiles) {
            countMap.merge(file.getCategory(), 1L, Long::sum);
        }
        return countMap;
    }

    /**
     * 计算有效期状态
     *
     * @param expireDate 有效期
     * @return valid / expiring_soon / expired
     */
    public static String calculateExpireStatus(LocalDate expireDate) {
        if (expireDate == null) {
            return null;
        }
        LocalDate today = LocalDate.now();
        LocalDate soonThreshold = today.plusDays(30);

        if (expireDate.isBefore(today)) {
            return "expired";
        } else if (expireDate.isBefore(today) || expireDate.isEqual(today)) {
            // 已过期
            return "expired";
        } else if (!expireDate.isAfter(soonThreshold)) {
            // 即将到期（<= 30天内）
            return "expiring_soon";
        } else {
            // 有效
            return "valid";
        }
    }

}
