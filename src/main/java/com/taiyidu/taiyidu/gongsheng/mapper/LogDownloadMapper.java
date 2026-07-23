package com.taiyidu.taiyidu.gongsheng.mapper;

import com.taiyidu.taiyidu.gongsheng.pojo.entity.LogDownload;
import com.taiyidu.taiyidu.gongsheng.pojo.vo.HistoryRecordVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LogDownloadMapper {
    void insert(LogDownload logDownload);

    List<HistoryRecordVo> showHistory(Long userId);
}
