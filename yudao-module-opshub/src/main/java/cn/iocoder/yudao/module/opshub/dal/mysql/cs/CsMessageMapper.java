package cn.iocoder.yudao.module.opshub.dal.mysql.cs;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.opshub.controller.admin.cs.vo.CsMessagePageReqVO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.cs.CsMessageDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CsMessageMapper extends BaseMapperX<CsMessageDO> {

    default PageResult<CsMessageDO> selectPageBySession(CsMessagePageReqVO reqVO) {
        LambdaQueryWrapperX<CsMessageDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(CsMessageDO::getSessionId, reqVO.getSessionId());
        wrapper.orderByAsc(CsMessageDO::getCreateTime);
        return selectPage(reqVO, wrapper);
    }

    /**
     * 查询某会话全部消息（正序）
     */
    default List<CsMessageDO> selectListBySessionId(Long sessionId) {
        return selectList(new LambdaQueryWrapper<CsMessageDO>()
                .eq(CsMessageDO::getSessionId, sessionId)
                .orderByAsc(CsMessageDO::getCreateTime));
    }

    /**
     * 获取指定时间后的最新消息（长轮询备选）
     */
    default List<CsMessageDO> selectLatestBySession(Long sessionId, LocalDateTime since) {
        return selectList(new LambdaQueryWrapper<CsMessageDO>()
                .eq(CsMessageDO::getSessionId, sessionId)
                .gt(CsMessageDO::getCreateTime, since)
                .orderByAsc(CsMessageDO::getCreateTime));
    }

    /**
     * 批量标记已读：将指定会话中非当前用户发送的未读消息标记为已读
     */
    default void updateMarkRead(Long sessionId, Long excludeSenderId) {
        update(new LambdaUpdateWrapper<CsMessageDO>()
                .set(CsMessageDO::getIsRead, 1)
                .eq(CsMessageDO::getSessionId, sessionId)
                .eq(CsMessageDO::getIsRead, 0)
                .ne(CsMessageDO::getSenderId, excludeSenderId));
    }

}
