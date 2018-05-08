package com.lhiot.mall.wholesale;

import com.lhiot.mall.wholesale.base.JacksonUtils;
import com.lhiot.mall.wholesale.order.domain.OrderDetail;
import com.lhiot.mall.wholesale.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;

@Service
@Slf4j
public class MqConsumer{

    private final OrderService orderService;

    private final RabbitTemplate rabbit;

    @Autowired
    public MqConsumer(OrderService orderService,RabbitTemplate rabbit){
        this.orderService=orderService;
        this.rabbit=rabbit;
    }

    /**
     * 处理订单超过三十分钟业务
     * @param getMessage 接收到的消息
     */
    @RabbitHandler
    @RabbitListener(queues = MQDefaults.REPEAT_QUEUE_NAME)
    public void orderOuttime(String getMessage) {
        log.info("orderOuttime =========== " + getMessage);
        try {
            OrderDetail orderDetail=JacksonUtils.fromJson(getMessage, OrderDetail.class);
            OrderDetail searchOrderDetail= orderService.searchOrderById(orderDetail.getId());

            if(Objects.nonNull(searchOrderDetail)) {
                //还是未支付状态 直接改成已失效
                if (Objects.equals("unpaid", searchOrderDetail.getOrderStatus())){
                    OrderDetail updateOrderDetail=new OrderDetail();
                    updateOrderDetail.setOrderCode(searchOrderDetail.getOrderCode());
                    updateOrderDetail.setOrderStatus("failed");
                    updateOrderDetail.setCurrentOrderStatus("unpaid");
                    orderService.updateOrderStatus(updateOrderDetail);
                }else if (Objects.equals("paying", searchOrderDetail.getOrderStatus())){
                    //继续往延迟队列中发送
                    rabbit.convertAndSend(MQDefaults.DIRECT_EXCHANGE_NAME, MQDefaults.DLX_QUEUE_NAME, JacksonUtils.toJson(orderDetail), message -> {
                        message.getMessageProperties().setExpiration(String.valueOf(1 * 60 * 1000));
                        return message;
                    });
                }
            }
        }  catch (IOException e) {
            log.error("消息处理错误" + e.getLocalizedMessage());
        }
    }
}