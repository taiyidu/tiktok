package com.taiyidu.taiyidu.gongsheng.service;

import com.taiyidu.taiyidu.gongsheng.pojo.vo.HistoryRecordVo;
import com.taiyidu.taiyidu.gongsheng.result.HeadRequest;
import com.taiyidu.taiyidu.gongsheng.pojo.vo.GeneralResultVo;

import java.util.List;

public interface videoScraperService {

    GeneralResultVo videoScraper(HeadRequest headRequest);

    List<HistoryRecordVo> showHistory();
}
