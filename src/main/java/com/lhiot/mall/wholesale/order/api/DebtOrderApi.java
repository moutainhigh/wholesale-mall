package com.lhiot.mall.wholesale.order.api;

import com.leon.microx.util.StringUtils;
import com.lhiot.mall.wholesale.base.PageQueryObject;
import com.lhiot.mall.wholesale.order.domain.DebtOrder;
import com.lhiot.mall.wholesale.order.domain.DebtOrderResult;
import com.lhiot.mall.wholesale.order.domain.OrderDetail;
import com.lhiot.mall.wholesale.order.domain.gridparam.DebtOrderGridParam;
import com.lhiot.mall.wholesale.order.service.DebtOrderService;
import com.lhiot.mall.wholesale.order.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Api(description ="账款订单接口")
@Slf4j
@RestController
@RequestMapping("/debtorder")
public class DebtOrderApi {

    private final DebtOrderService debtOrderService;
    private final OrderService orderService;

    @Autowired
    public DebtOrderApi(DebtOrderService debtOrderService,OrderService orderService) {
        this.debtOrderService = debtOrderService;
        this.orderService=orderService;
    }

    @PostMapping("/create")
    @ApiOperation(value = "创建账款订单")
    public ResponseEntity create(@RequestBody DebtOrder debtOrder)  {

        String orderIds=debtOrder.getOrderIds();
        if(StringUtils.isEmpty(orderIds)){
            return ResponseEntity.badRequest().body("未选择订单");
        }
        int debtFee=0;
        List<OrderDetail> orderDetailList=orderService.searchOrdersByOrderCodes(orderIds.split(","));
        if(orderDetailList==null||orderDetailList.isEmpty()){
            return ResponseEntity.badRequest().body("未查找到相关订单");
        }
        for (OrderDetail item:orderDetailList){
            //计算账款订单金额
            debtFee+=item.getPayableFee()+item.getDeliveryFee();
            //检查非结账付款订单
            if(!Objects.equals("offline",item.getSettlementType())){
                return ResponseEntity.badRequest().body("订单编码"+item.getOrderCode()+"非货到付款订单");
            }
            DebtOrder searchDebtOrder= debtOrderService.findByOrderIdLike(item.getOrderCode());
            //非空说明依据选择了
            if(Objects.nonNull(searchDebtOrder)){
                return ResponseEntity.badRequest().body("订单编码"+item.getOrderCode()+"已经绑定账款订单");
            }
        }
        debtOrder.setDebtFee(debtFee);
        debtOrderService.create(debtOrder);
        return ResponseEntity.ok(debtOrder);
    }

    @PutMapping("/modify")
    @ApiOperation(value = "审核账款订单")
    public ResponseEntity modify(@RequestBody DebtOrder debtOrder)  {
        DebtOrder searchDebtOrder=debtOrderService.findByCode(debtOrder.getOrderDebtCode());

        if(Objects.isNull(searchDebtOrder)){
            return ResponseEntity.badRequest().body("未查找到相关账款订单");
        }
        debtOrder.setOrderIds(searchDebtOrder.getOrderIds());
       if (Objects.equals(debtOrder.getCheckStatus(),"agree")){
           debtOrderService.passAuditing(debtOrder);
       }else if(Objects.equals(debtOrder.getCheckStatus(),"reject")){
           debtOrderService.unpassAuditing(debtOrder);
       }else{
           return ResponseEntity.badRequest().body("账款订单传递审核状态不正确"+debtOrder.getCheckStatus());
       }
        return ResponseEntity.ok(debtOrder);
    }

    @GetMapping("/{debtorderCode}")
    @ApiOperation(value = "根据订单编号查询订单详情")
    public ResponseEntity queryOrderByCode(@PathVariable("debtorderCode") String debtorderCode){
        DebtOrder debtOrder = debtOrderService.findByCode(debtorderCode);
        if (Objects.isNull(debtOrder)){
            return ResponseEntity.badRequest().body("未找到欠款订单信息");
        }
        return ResponseEntity.ok(debtOrder);
    }

    @PostMapping("/grid")
    @ApiOperation(value = "后台管理-分页查询账款订单信息", response = PageQueryObject.class)
    public ResponseEntity<PageQueryObject> grid(@RequestBody(required = true) DebtOrderGridParam param) throws IntrospectionException, InstantiationException, IllegalAccessException, InvocationTargetException {
        return ResponseEntity.ok(debtOrderService.pageQuery(param));
    }

    @GetMapping("/detail/{id}")
    @ApiOperation(value = "后台管理-账款订单详情页面",response = DebtOrderResult.class)
    public  ResponseEntity<DebtOrderResult> detail(@PathVariable("id") Long id){
        return ResponseEntity.ok(debtOrderService.detail(id));
    }

    @PostMapping("/export")
    @ApiOperation(value = "后台管理系统新建一个查询，数据导出", response = DebtOrderResult.class,responseContainer="list")
    public ResponseEntity<List<Map<String, Object>>> exportData(@RequestBody(required = true) DebtOrderGridParam param) {
        //FIXME 根据orderIds查询查询订单和支付记录，后期统一修改
        return ResponseEntity.ok(debtOrderService.exportData(param));
    }
}
