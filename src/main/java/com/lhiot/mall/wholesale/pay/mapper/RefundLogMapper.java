package com.lhiot.mall.wholesale.pay.mapper;

import com.lhiot.mall.wholesale.pay.domain.PaymentLog;
import com.lhiot.mall.wholesale.pay.domain.RefundLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RefundLogMapper {


    int insertRefundLog(RefundLog refundLog);

}
