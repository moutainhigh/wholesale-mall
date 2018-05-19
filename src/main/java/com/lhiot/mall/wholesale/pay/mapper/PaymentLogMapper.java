package com.lhiot.mall.wholesale.pay.mapper;

import com.lhiot.mall.wholesale.pay.domain.Balance;
import com.lhiot.mall.wholesale.pay.domain.PaymentLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PaymentLogMapper {


    int insertPaymentLog(PaymentLog paymentLog);

    int updatePaymentLog(PaymentLog paymentLog);

    List <Balance> getBalanceRecord(Integer userId);

    PaymentLog getPaymentLog(String orderCode);

    List<PaymentLog> getPaymentLogList(List<Long> userIds);

    PaymentLog countFee(List<String> orderCode);
}
