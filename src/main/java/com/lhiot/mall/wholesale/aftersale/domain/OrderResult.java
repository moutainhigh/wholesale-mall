package com.lhiot.mall.wholesale.aftersale.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lhiot.mall.wholesale.order.domain.OrderGoods;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Timestamp;
import java.util.List;

@Data
@ToString
@ApiModel
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class OrderResult {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("orderId")
    private String orderCode;

    @JsonProperty("salesmanId")
    private Long salesmanId;

    @JsonProperty("settlementType")
    private String settlementType;

    @JsonProperty("status")
    private String orderStatus;

    //订单当前状态 用于修改的时候约束条件
    @JsonProperty("currentOrderStatus")
    private String currentOrderStatus;

    @JsonProperty("auditStatus")
    private String checkStatus;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @JsonProperty("createTime")
    private Timestamp createTime;

    @JsonProperty("total")
    private Integer totalFee;

    @JsonProperty("couponVal")
    private Integer discountFee;

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("paidType")
    private String payStatus;

    @JsonProperty("comments")
    private String remarks;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @JsonProperty("afterSaleTime")
    private Timestamp afterSaleTime;

    @JsonProperty("hdStatus")
    private String hdStatus;//发送订单到海鼎是否成功0成功1失败

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @JsonProperty("deliveryTime")
    private Timestamp deliveryTime;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @JsonProperty("receiveTime")
    private Timestamp receiveTime;

    @JsonProperty("deliveryFee")
    private Integer deliveryFee;

    @JsonProperty("deliveryAddress")
    private String deliveryAddress;

    @JsonProperty("proList")
    private List<OrderGoods> orderGoodsList;

    @JsonProperty("shopName")
    private String shopName;

    @JsonProperty("userName")
    private String userName;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("needPay")
    private  Integer payableFee;

    @JsonProperty("addressDetail")
    private String addressDetail;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @JsonProperty("paymentTime")
    private Timestamp paymentTime;

    @JsonProperty("paymentType")
    private String paymentType;

    @JsonProperty("salesmanName")
    private String salesmanName;

    @JsonProperty("expire")
    private String expire;//是否已过期 yes no

    @JsonProperty("hdCode")
    private String hdCode;

    @JsonProperty("orderCoupon")
    private Long orderCoupon;//优惠券

    @JsonProperty("start")
    private Integer start;

    @JsonProperty("rows")
    private Integer rows;

    private String sidx;

    private Integer page;

    @JsonProperty("orderRefundApplication")
    private OrderRefundApplication orderRefundApplication;
}
