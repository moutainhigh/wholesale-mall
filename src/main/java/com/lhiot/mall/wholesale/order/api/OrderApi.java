package com.lhiot.mall.wholesale.order.api;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.leon.microx.common.wrapper.ArrayObject;
import com.lhiot.mall.wholesale.activity.domain.FlashsaleGoods;
import com.lhiot.mall.wholesale.activity.service.FlashsaleService;
import com.lhiot.mall.wholesale.aftersale.domain.ApplicationType;
import com.lhiot.mall.wholesale.aftersale.service.OrderRefundApplicationService;
import com.lhiot.mall.wholesale.base.CalculateUtil;
import com.lhiot.mall.wholesale.base.DateFormatUtil;
import com.lhiot.mall.wholesale.base.JacksonUtils;
import com.lhiot.mall.wholesale.base.PageQueryObject;
import com.lhiot.mall.wholesale.coupon.domain.CouponEntityResult;
import com.lhiot.mall.wholesale.coupon.service.CouponEntityService;
import com.lhiot.mall.wholesale.goods.domain.Goods;
import com.lhiot.mall.wholesale.goods.domain.GoodsPriceRegion;
import com.lhiot.mall.wholesale.goods.service.GoodsPriceRegionService;
import com.lhiot.mall.wholesale.goods.service.GoodsService;
import com.lhiot.mall.wholesale.invoice.domain.Invoice;
import com.lhiot.mall.wholesale.order.domain.Distribution;
import com.lhiot.mall.wholesale.order.domain.OrderDetail;
import com.lhiot.mall.wholesale.order.domain.OrderGoods;
import com.lhiot.mall.wholesale.order.domain.OrderGridResult;
import com.lhiot.mall.wholesale.order.domain.gridparam.OrderGridParam;
import com.lhiot.mall.wholesale.order.service.OrderService;
import com.lhiot.mall.wholesale.setting.domain.ParamConfig;
import com.lhiot.mall.wholesale.setting.service.SettingService;
import com.sgsl.util.StringUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Api(description ="订单接口")
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderApi {

    private final OrderService orderService;

    private final SettingService settingService;

    private final GoodsService goodsService;

    private final CouponEntityService couponEntityService;

    private final FlashsaleService flashsaleService;

    private final GoodsPriceRegionService priceRegionService;

    private final OrderRefundApplicationService orderRefundApplicationService;
    @Autowired
    public OrderApi(OrderService orderService, SettingService settingService,  
    		GoodsService goodsService, CouponEntityService couponEntityService, 
    		FlashsaleService flashsaleService, GoodsPriceRegionService priceRegionService,
    		OrderRefundApplicationService orderRefundApplicationService) {

        this.orderService = orderService;
        this.settingService = settingService;
        this.goodsService=goodsService;
        this.couponEntityService = couponEntityService;
        this.flashsaleService = flashsaleService;
        this.priceRegionService = priceRegionService;
        this.orderRefundApplicationService = orderRefundApplicationService;
    }

    @GetMapping("/my-orders/{userId}")
    @ApiOperation(value = "我的订单列表")
    public ResponseEntity<ArrayObject> queryMyOrders(@PathVariable("userId") long userId, @RequestParam(required = false) String orderStatus,
                                                     @RequestParam(required = false) String orderStatusIn,
                                                     @RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="10") Integer rows){
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setUserId(userId);
        orderDetail.setOrderStatus(orderStatus);
        if(StringUtils.isNotEmpty(orderStatusIn)){
            orderDetail.setOrderStatusIn(orderStatusIn.split(","));
        }
        orderDetail.setPage(page);
        orderDetail.setStart((page-1)*rows);
        orderDetail.setRows(rows);
        List<OrderDetail> orderDetailList = orderService.searchOrders(orderDetail);
        if (orderDetailList.isEmpty()){
            return ResponseEntity.ok(ArrayObject.of(new ArrayList<OrderDetail>()));
        }else {
            for (OrderDetail order:orderDetailList){
                String checkStatus = orderService.searchOutstandingAccountsOrder(order.getOrderCode());
                order.setCheckStatus(checkStatus);
                List<OrderGoods> goods = orderService.searchOrderGoods(order.getId());
                order.setOrderGoodsList(goods);
            }
        }
        return ResponseEntity.ok(ArrayObject.of(orderDetailList));
    }

    @GetMapping("/debtorders/{userId}")
    @ApiOperation(value = "账款订单列表")
    public ResponseEntity<ArrayObject> debtOrders(@PathVariable("userId") long userId,@RequestParam(defaultValue="1") Integer page,@RequestParam(defaultValue="10") Integer rows){
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setUserId(userId);
        orderDetail.setSettlementType("offline");
        orderDetail.setPayStatus("unpaid");
        orderDetail.setOrderStatusIn(new String[]{"undelivery","delivery","received","unrefunded","refundfailed"});
        orderDetail.setPage(page);
        orderDetail.setStart((page-1)*rows);
        orderDetail.setRows(rows);
        orderDetail.setCheckStatus("agree");
        List<OrderDetail> orderDetailList = orderService.searchOrders(orderDetail);
        if (orderDetailList.isEmpty()){
            return ResponseEntity.ok(ArrayObject.of(new ArrayList<OrderDetail>()));
        }else {
            for (OrderDetail order:orderDetailList){
/*                String checkStatus = orderService.searchOutstandingAccountsOrder(order.getOrderCode());
                order.setCheckStatus(checkStatus);*/
                List<OrderGoods> goods = orderService.searchOrderGoods(order.getId());
                order.setOrderGoodsList(goods);
            }
        }
        return ResponseEntity.ok(ArrayObject.of(orderDetailList));
    }


    @GetMapping("/my-order/{orderCode}")
    @ApiOperation(value = "根据订单编号查询订单详情")
    public ResponseEntity queryOrder(@PathVariable("orderCode") String orderCode){
        OrderDetail orderDetail = orderService.searchOrder(orderCode);
        /*PaymentLog paymentLog = paymentLogService.getPaymentLog(orderCode);
        orderDetail.setPayType(paymentLog.getPaymentType());*/
        if (Objects.isNull(orderDetail)){
           return ResponseEntity.badRequest().body("没有该订单信息");
        }
    	orderDetail.setSupplements(orderRefundApplicationService.supplements(orderCode,ApplicationType.supplement));
        List<OrderGoods> goods = orderService.searchOrderGoods(orderDetail.getId());
        if (goods.isEmpty()){
            orderDetail.setOrderGoodsList(new ArrayList<OrderGoods>());
        }else {
            orderDetail.setOrderGoodsList(goods);
        }
        return ResponseEntity.ok(orderDetail);
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据订单编号查询订单详情")
    public ResponseEntity queryOrderById(@PathVariable("id") long id){
        OrderDetail orderDetail = orderService.searchOrderById(id);
        if (Objects.isNull(orderDetail)){
            return ResponseEntity.badRequest().body("没有该订单信息");
        }
        List<OrderGoods> goods = orderService.searchOrderGoods(id);
        if (goods.isEmpty()){
            orderDetail.setOrderGoodsList(new ArrayList<OrderGoods>());
        }else {
            orderDetail.setOrderGoodsList(goods);
        }
        return ResponseEntity.ok(orderDetail);
    }

    @GetMapping("/invoice/orders/{userId}")
    @ApiOperation(value = "查询可开发票的订单列表")
    public ResponseEntity<ArrayObject> invoiceOrders(@PathVariable("userId") @NotNull long userId,@RequestParam(defaultValue="1") Integer page,
                                                     @RequestParam(defaultValue="10") Integer rows) {
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setUserId(userId);
        orderDetail.setPayStatus("paid");
        orderDetail.setOrderStatus("received");
        orderDetail.setInvoiceStatus("no");
        orderDetail.setPage(page);
        orderDetail.setRows(rows);
        orderDetail.setStart((page-1)*rows);
        List <OrderDetail> orders = orderService.searchAfterSaleOrder(orderDetail);
        List<OrderDetail> orderResults=new ArrayList<OrderDetail>();
        if (orders.isEmpty()){
            return ResponseEntity.ok(ArrayObject.of(new ArrayList<>()));
        }
        String time = DateFormatUtil.format1(new java.util.Date());
        Timestamp currentTime = Timestamp.valueOf(time);
        for (OrderDetail order:orders) {
            //发票订单是收货已付款且过售后时间的订单
            if ("received".equals(order.getOrderStatus())&&"paid".equals(order.getPayStatus())&&order.getAfterSaleTime().before(currentTime)){
                List<OrderGoods> goods = orderService.searchOrderGoods(order.getId());
                if (goods.isEmpty()){
                    order.setOrderGoodsList(new ArrayList<OrderGoods>());
                }else {
                    order.setOrderGoodsList(goods);
                }
                orderResults.add(order);
            }
        }
        return ResponseEntity.ok(ArrayObject.of(orderResults));
    }

    @GetMapping("/distribution/{fee}")
    @ApiOperation(value = "查询配送费")
    public  ResponseEntity distribution(@PathVariable("fee") @NotNull Integer fee) throws Exception{
        //最低订单金额限制
        ParamConfig orderMinFeeConfig = settingService.searchConfigParam("orderMinFee");
        if(Objects.nonNull(orderMinFeeConfig)&&Integer.valueOf(orderMinFeeConfig.getConfigParamValue())>fee){
            return ResponseEntity.ok().body("{\"code\":-1,\"msg\":\"未达到最低配送金额："+Integer.valueOf(orderMinFeeConfig.getConfigParamValue())/100.0+"\"}");
        }
        ParamConfig paramConfig = settingService.searchConfigParam("distributionFeeSet");
        String distribution = paramConfig.getConfigParamValue();
        Distribution[] distributionsJson = JacksonUtils.fromJson(distribution,  Distribution[].class);//字符串转json
        List<Distribution> distributionsList=Arrays.asList(distributionsJson);
        //[{"minPrice": 200,"maxPrice":300,"distributionFee": 25},
        // {"minPrice": 300,"maxPrice": 500,"distributionFee": 15},
        // {"minPrice":500,"maxPrice": 1000,"distributionFee": 0}]
        //先排序
        distributionsList.sort((o1,o2) -> o1.getMaxPrice() - o2.getMaxPrice());
/*        Collections.sort(distributionsList, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                Distribution distribution1 = (Distribution) o1;
                Distribution distribution2 = (Distribution) o2;
                if (distribution1.getMaxPrice() > distribution2.getMaxPrice()) {
                    return 1;
                } else if (distribution1.getMaxPrice() == distribution2.getMaxPrice()) {
                    return 0;
                } else {
                    return -1;
                }
            }
        });*/
        for (Distribution item:distributionsList){
            //只限定查询最大值
            if (fee<item.getMaxPrice()){
                Integer distributionFee = item.getDistributionFee();
                return ResponseEntity.ok(distributionFee);
            }
        }
        //没有就设置成默认的运费0
        return ResponseEntity.ok(0);
    }

    @GetMapping("/after-sale/{userId}")
    @ApiOperation(value = "查询售后订单")
    public ResponseEntity<ArrayObject> queryAfterSale(@PathVariable("userId") @NotNull long userId,@RequestParam(defaultValue="1") Integer page,
                                                      @RequestParam(defaultValue="10") Integer rows) {
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setUserId(userId);
        orderDetail.setOrderStatus("received");
        orderDetail.setPayStatus("paid");
        orderDetail.setAfterStatus("no");
        orderDetail.setPage(page);
        orderDetail.setRows(rows);
        orderDetail.setStart((page-1)*rows);
        List <OrderDetail> orders = orderService.searchAfterSaleOrder(orderDetail);
        if (orders.isEmpty()){
            return ResponseEntity.ok(ArrayObject.of(new ArrayList<>()));
        }
        String time = DateFormatUtil.format1(new java.util.Date());
        Timestamp currentTime = Timestamp.valueOf(time);
        for (OrderDetail order:orders) {
            if (order.getAfterSaleTime().after(currentTime)){
                order.setExpire("no");
            }else{
                order.setExpire("yes");
            }
            List<OrderGoods> goods = orderService.searchOrderGoods(order.getId());
            if (goods.isEmpty()){
                order.setOrderGoodsList(new ArrayList<OrderGoods>());
            }else {
                order.setOrderGoodsList(goods);
            }
        }
        return ResponseEntity.ok(ArrayObject.of(orders));
    }

    @PostMapping("/create")
    @ApiOperation(value = "创建订单")
    public ResponseEntity<OrderDetail> create(@RequestBody OrderDetail orderDetail) throws IOException {
        //查询商品库存 不足返回
        //检测限时抢购商品
        //验证前端应付金额
        int needPay=0;//需要支付金额
        int gooddNeedPay=0;//商品金额
        Integer totalFee=orderDetail.getPayableFee()+orderDetail.getDiscountFee();//总金额
        Integer payableFee = orderDetail.getPayableFee();
        boolean haveFlashGoods=false;
        if(Objects.isNull(orderDetail.getTotalFee())
                ||Objects.isNull(orderDetail.getDeliveryFee())
                ||Objects.isNull(payableFee)){
            orderDetail.setCode(-1002);
            orderDetail.setMsg("订单金额传递为空");
            return ResponseEntity.ok(orderDetail);
        }
        if(!settingService.isBuyTime()){
            orderDetail.setCode(-1002);
            orderDetail.setMsg("请在营业时间内下单,营业时间为:"+settingService.searchConfigParam("buyTime").getConfigParamValue());
            return ResponseEntity.ok(orderDetail);
        }
        for(OrderGoods item: orderDetail.getOrderGoodsList()){
            Goods goods=goodsService.goods(item.getGoodsId());
            Integer stockLimit=goods.getStockLimit();
            Integer quanity = item.getQuanity();
            if(Objects.nonNull(stockLimit)&&stockLimit-quanity<0){
                orderDetail.setCode(-1002);
                orderDetail.setMsg(item.getGoodsName()+"库存不足");
                return ResponseEntity.ok(orderDetail);
            }
            if(item.getFlash()==1){
                //查询限时抢购相关信息
                log.info("查询限时抢购相关信息"+item.getGoodsId()+":"+item.getGoodsName());
                //用户已经抢购数量
                int userFlashCount=flashsaleService.userRecords(orderDetail.getUserId(),item.getGoodsStandardId());
                FlashsaleGoods flashsaleGoods= flashsaleService.flashsaleGoods(item.getGoodsStandardId());
                if(Objects.isNull(flashsaleGoods)){
                    orderDetail.setCode(-1002);
                    orderDetail.setMsg(item.getGoodsName()+"当前没有开启限时抢购活动");
                    return ResponseEntity.ok(orderDetail);
                }
                //查询当前活动每人能够抢购数量与实际抢购库存数 如果超过就提示错误
                if(flashsaleGoods.getRemain()<item.getQuanity()){
                    orderDetail.setCode(-1002);
                    orderDetail.setMsg(item.getGoodsName()+"抢购活动数量不足");
                    return ResponseEntity.ok(orderDetail);
                }
                if(flashsaleGoods.getLimitQuantity()<userFlashCount+item.getQuanity()){
                    orderDetail.setCode(-1002);
                    orderDetail.setMsg(item.getGoodsName()+"超过限时抢购每人限定数量");
                    return ResponseEntity.ok(orderDetail);
                }
                needPay+=flashsaleGoods.getSpecialPrice()*item.getQuanity();
                item.setDiscountGoodsPrice(orderService.discountPrice(totalFee, 
                		payableFee, flashsaleGoods.getSpecialPrice()));
                gooddNeedPay+=flashsaleGoods.getSpecialPrice()*item.getQuanity();
                haveFlashGoods=true;//拥有限时抢购商品
            }else{
                //要依据订单的商品规格查询关联的商品数量与对应购买价格计算实际商品价格
                List<GoodsPriceRegion> goodsPriceRegions=priceRegionService.selectPriceRegion(item.getGoodsStandardId());
                for (GoodsPriceRegion goodsPriceRegion:goodsPriceRegions){
                    //依据购买数量计算所在价格区间
                    if(item.getQuanity()>=goodsPriceRegion.getMinQuantity()&&item.getQuanity()<goodsPriceRegion.getMaxQuantity()){
                        needPay+=goodsPriceRegion.getPrice()*item.getQuanity();
                        gooddNeedPay+=goodsPriceRegion.getPrice()*item.getQuanity();
                        item.setDiscountGoodsPrice(orderService.discountPrice(totalFee, payableFee, 
                        		goodsPriceRegion.getPrice()));//优惠后价格
                        break;
                    }
                }
            }
        }
        //检查优惠券是否失效
        if(Objects.nonNull(orderDetail.getOrderCoupon())) {
            CouponEntityResult couponEntityResult = couponEntityService.coupon(orderDetail.getOrderCoupon());
            if(Objects.isNull(couponEntityResult)){
                orderDetail.setCode(-1002);
                orderDetail.setMsg("优惠券不存在");
                return ResponseEntity.ok(orderDetail);
            }
            //优惠券状态：unused-未使用  used-已使用  expired-已过期
            if (!Objects.equals(couponEntityResult.getCouponStatus(), "unused")) {
                if (Objects.equals(couponEntityResult.getCouponStatus(), "used")) {
                    orderDetail.setCode(-1002);
                    orderDetail.setMsg("优惠券已使用");
                    return ResponseEntity.ok(orderDetail);
                }
                if (Objects.equals(couponEntityResult.getCouponStatus(), "expired")) {
                    orderDetail.setCode(-1002);
                    orderDetail.setMsg("优惠券已过期");
                    return ResponseEntity.ok(orderDetail);
                }
            }else{
                if(haveFlashGoods){
                    orderDetail.setCode(-1002);
                    orderDetail.setMsg("拥有限时抢购商品不允许使用优惠券");
                    return ResponseEntity.ok(orderDetail);
                }
                int fullFee=couponEntityResult.getFullFee();//优惠满减金额
                //满减金额大于订单商品总金额 不允许使用优惠券
                if(fullFee>gooddNeedPay){
                    orderDetail.setCode(-1002);
                    orderDetail.setMsg("优惠券满减金额大于订单商品金额");
                    return ResponseEntity.ok(orderDetail);
                }
                //应付金额要减优惠金额
                needPay =needPay-couponEntityResult.getCouponFee();
            }
        }
        //查询配送费
        ParamConfig paramConfig = settingService.searchConfigParam("distributionFeeSet");
        String distribution = paramConfig.getConfigParamValue();
        Distribution[] distributionsJson = JacksonUtils.fromJson(distribution,  Distribution[].class);//字符串转json
        int distributionFee=0;//配送费
        List<Distribution> distributionsList=Arrays.asList(distributionsJson);
        //先排序
        distributionsList.sort((o1,o2) -> o1.getMaxPrice() - o2.getMaxPrice());
        for (Distribution item:distributionsList){
            if (needPay<item.getMaxPrice()){
                distributionFee = item.getDistributionFee();
                break;
            }
        }
        //最低订单金额限制
        ParamConfig orderMinFeeConfig = settingService.searchConfigParam("orderMinFee");
        if(Objects.nonNull(orderMinFeeConfig)&&
        		CalculateUtil.compare(orderMinFeeConfig.getConfigParamValue(), needPay) > 0){
            orderDetail.setCode(-1002);
            orderDetail.setMsg("未达到最低配送金额："+CalculateUtil.division(orderMinFeeConfig.getConfigParamValue(), 100, 2));
            return ResponseEntity.ok(orderDetail);
        }
        needPay =needPay+distributionFee;
        //验证实际计算订单金额+配送费-优惠金额=传递的订单应付金额+应付配送费
        if (needPay!=orderDetail.getPayableFee()+orderDetail.getDeliveryFee()){
            orderDetail.setCode(-1002);
            orderDetail.setMsg("订单计算应付金额("+needPay+")与实际传递订单金额("+(orderDetail.getPayableFee()+orderDetail.getDeliveryFee())+")不一致，请刷新重试");
            return ResponseEntity.ok(orderDetail);
        }
        //总金额不需要计算
        int result=orderService.create(orderDetail);
        if(result>0){
            orderDetail.setCode(1002);
            orderDetail.setMsg("创建成功");
        }else{
            orderDetail.setCode(-1002);
            orderDetail.setMsg("创建失败");
        }
        return ResponseEntity.ok(orderDetail);
    }

    @PostMapping("/update/{orderCode}")
    @ApiOperation(value = "依据订单编码修改订单信息")
    public ResponseEntity<OrderDetail> update(@PathVariable("orderCode") String orderCode,@RequestBody OrderDetail orderDetail){
        orderDetail.setOrderCode(orderCode);
        int result=orderService.updateOrder(orderDetail);
        if(result>0){
            orderDetail.setCode(1001);
        }else{
            orderDetail.setCode(-1001);
        }
        return ResponseEntity.ok(orderDetail);
    }


    @PutMapping("/cancel/unpay/{orderCode}")
    @ApiOperation(value = "取消未支付订单")
    public ResponseEntity<Integer> cancelUnpayOrder(@PathVariable("orderCode") String orderCode){
        Integer result=orderService.cancelUnpayOrder(orderCode);
        return ResponseEntity.ok(result);
    }


    @PutMapping("/cancel/payed/{orderCode}")
    @ApiOperation(value = "取消待收货订单")
    public ResponseEntity cancelPayedOrder(@PathVariable("orderCode") String orderCode){
        //判断订单类型来确定退款方式
        //需求 当天可以自己任意取消支付订单，订单为全部商品,最后取消海鼎订单 超过指定时间，需要后台审核订单，审核走售后流程
        OrderDetail orderDetail = orderService.searchOrder(orderCode);
        if (Objects.isNull(orderDetail)){
            return ResponseEntity.badRequest().body("没有该订单信息");
        }
        if(!Objects.equals(orderDetail.getOrderStatus(),"undelivery")){
            return ResponseEntity.badRequest().body("非待收货订单状态");
        }
        if(Objects.equals("unpaid",orderDetail.getPayStatus())
                &&!Objects.equals("offline",orderDetail.getSettlementType())){
            return ResponseEntity.badRequest().body("订单未支付");
        }
        String cancelPayedOrderResult=orderService.cancelPayedOrder(orderDetail);
        if(StringUtils.isEmpty(cancelPayedOrderResult)){
            return ResponseEntity.ok("1");
        }
        return ResponseEntity.badRequest().body(cancelPayedOrderResult);

    }

    @PutMapping("/received/{orderCode}")
    @ApiOperation(value = "确认订单收货")
    public ResponseEntity receivedOrder(@PathVariable("orderCode") String orderCode){
        //判断订单类型来确定退款方式
        //需求 当天可以自己任意取消支付订单，订单为全部商品,最后取消海鼎订单 超过指定时间，需要后台审核订单，审核走售后流程
        OrderDetail orderDetail = orderService.searchOrder(orderCode);
        if (Objects.isNull(orderDetail)){
            return ResponseEntity.badRequest().body("没有该订单信息");
        }
        if(!Objects.equals(orderDetail.getOrderStatus(),"undelivery")&&!Objects.equals(orderDetail.getOrderStatus(),"delivery")){
            return ResponseEntity.badRequest().body("非待收货或者非配送中订单状态");
        }
        return ResponseEntity.ok(orderService.receivedOrder(orderCode));
    }


    @PostMapping("/grid")
    @ApiOperation(value = "后台管理-分页查询订单信息", response = PageQueryObject.class)
    public ResponseEntity<PageQueryObject> grid(@RequestBody(required = true) OrderGridParam param) throws IntrospectionException, InstantiationException, IllegalAccessException, InvocationTargetException {
        return ResponseEntity.ok(orderService.pageQuery(param));
    }

    @GetMapping("/detail/{id}")
    @ApiOperation(value = "后台管理-根据订单id查看订单详情",response = OrderGridResult.class)
    public  ResponseEntity<OrderDetail> orderDetail(@PathVariable("id") Long id){
    	OrderDetail orderDetail = orderService.detail(id);
    	if(Objects.nonNull(orderDetail)){
        	orderDetail.setSupplements(orderRefundApplicationService.supplements(orderDetail.getOrderCode(), 
        			ApplicationType.supplement));
    	}
        return ResponseEntity.ok(orderDetail);
    }

    @PostMapping("/export")
    @ApiOperation(value = "后台管理系统新建一个查询，数据导出", response = Invoice.class,responseContainer="list")
    public ResponseEntity<List<Map<String, Object>>> exportData(@RequestBody(required = true) OrderGridParam param) {
        return ResponseEntity.ok(orderService.exportData(param));
    }

    @PostMapping("/exportgoods")
    @ApiOperation(value = "后台管理系统新建一个查询，数据导出", response = Invoice.class,responseContainer="list")
    public ResponseEntity<List<Map<String, Object>>> exportDataOrderGoods(@RequestBody(required = true) OrderGridParam param) {
        return ResponseEntity.ok(orderService.exportDataOrderGoods(param));
    }
}
