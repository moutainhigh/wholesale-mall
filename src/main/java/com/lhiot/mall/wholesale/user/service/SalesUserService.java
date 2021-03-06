package com.lhiot.mall.wholesale.user.service;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.leon.microx.util.ImmutableMap;
import com.lhiot.mall.wholesale.base.PageQueryObject;
import com.lhiot.mall.wholesale.user.domain.SalesUser;
import com.lhiot.mall.wholesale.user.domain.SalesUserRelation;
import com.lhiot.mall.wholesale.user.domain.ShopResult;
import com.lhiot.mall.wholesale.user.domain.User;
import com.lhiot.mall.wholesale.user.domain.UserAddress;
import com.lhiot.mall.wholesale.user.mapper.SalesUserMapper;
import com.lhiot.mall.wholesale.user.mapper.UserMapper;
import com.lhiot.mall.wholesale.user.wechat.PaymentProperties;
import com.sgsl.util.StringUtils;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@Transactional
public class SalesUserService {

    private final SalesUserMapper salesUserMapper;

    private final RabbitTemplate rabbit;

    private final UserMapper userMapper;

    private final PaymentProperties properties;

    private final RestTemplate restTemplate;


    @Autowired
    public SalesUserService(SalesUserMapper salesUserMapper, RabbitTemplate rabbit, UserMapper userMapper, PaymentProperties properties, RestTemplate restTemplate) {
        this.salesUserMapper = salesUserMapper;
        this.rabbit = rabbit;
        this.userMapper = userMapper;
        this.properties = properties;
        this.restTemplate = restTemplate;
    }


    public List<SalesUserRelation> selectRelation(Map<String,Object> param) {
        return salesUserMapper.selectRelation(param);
    }

    public int updateUserSaleRelationship(SalesUserRelation salesUserRelation){
        return salesUserMapper.updateUserSaleRelationship(salesUserRelation);
    }

    public SalesUser searchSalesUser(long id){
         return salesUserMapper.findById(id);
    }

    public SalesUser searchSalesUserByOpenid(String openid){
        return salesUserMapper.searchSalesUserByOpenid(openid);
    }

    public int insertRelation(SalesUserRelation salesUserRelation){
        return salesUserMapper.insertRelation(salesUserRelation);
    }

    public SalesUserRelation isSeller(long userId){
        return salesUserMapper.isSeller(userId);
    }

    public SalesUserRelation searchSaleRelationship(SalesUserRelation salesUserRelation){
        return salesUserMapper.searchSaleRelationship(salesUserRelation);
    }

    public List<ShopResult> searchShopInfo(long salesId){
        return salesUserMapper.searchShopInfo(salesId);
    }

    public List<SalesUserRelation> salesUser(long salesmanId){
        return salesUserMapper.salesUser(salesmanId);
    }

    /**
     * 门店审核
     * @param salesUserRelation
     * @return
     */
    public boolean userCheck(SalesUserRelation salesUserRelation){
        User user = userMapper.user(salesUserRelation.getUserId());
        Map<String,Object> param = new HashMap<String,Object>();
        param.put("userId",salesUserRelation.getUserId());
        if (salesUserMapper.updateUserSaleRelationship(salesUserRelation)>0){
            if (Objects.equals(salesUserRelation.getAuditStatus(),"agree")){
                param.put("status","certified");
                if (userMapper.updateUserStatus(param)>0){//用户表改已认证
                    //审核通过的时候发送发券广播消息
                    rabbit.convertAndSend("store-check-event","", salesUserRelation.getUserId());

                    List<UserAddress> userAddresses = userMapper.searchAddressList(salesUserRelation.getUserId());
                    if (!userAddresses.isEmpty()){
                        for (UserAddress userAddress : userAddresses){
                            if (userMapper.deleteAddress(userAddress.getId())<=0){
                                log.info("用户残余地址未删除成功");
                                return false;
                            }
                        }
                    }
                    //冗余地址
                    UserAddress userAddress = new UserAddress();
                    userAddress.setPhone(user.getPhone());
                    userAddress.setIsDefault("yes");
                    userAddress.setContactsName(user.getUserName());
                    userAddress.setAddressArea(user.getCity());
                    userAddress.setAddressDetail(user.getAddressDetail()+"-"+user.getShopName());
                    userAddress.setUserId(user.getId());
                    userAddress.setSex(user.getSex());
                    userMapper.insertAddress(userAddress);


                    //发送短信
                    Map<String, Object> body = ImmutableMap.of("phone",user.getPhone());
                    HttpEntity<Map<String, Object>> request = properties.getSendSms().createRequest(body);
                    String messageUrl= MessageFormat.format(properties.getSendSms().getUrl(),"regist-pass", user.getPhone());
                    try{
                        String result = restTemplate.postForObject(messageUrl, request, String.class);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }else {
               /* List<SalesUserRelation> relationList = salesUserMapper.selectUserRelation(salesUserRelation.getUserId());
                if (!relationList.isEmpty()){
                    for (SalesUserRelation relation:relationList) {
                        if (salesUserMapper.deleteRelation(relation.getId())<=0){
                            log.info("用户和业务员关联残余数据未删除成功");
                            return false;
                        }
                    }
                }*/
                param.put("status","uncertified");
                if (userMapper.updateUserStatus(param)>0){//用户未认证
                    //发送短信
                    Map<String, Object> body = ImmutableMap.of("phone",user.getPhone());
                    HttpEntity<Map<String, Object>> request = properties.getSendSms().createRequest(body);
                    String messageUrl= MessageFormat.format(properties.getSendSms().getUrl(),"regist-unpass",user.getPhone());
                    try{
                        String result=restTemplate.postForObject(messageUrl, request, String.class);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
        return true;
    }


    /***************后台管理系统*******************/
    public int create(SalesUser salesUser){
        return this.salesUserMapper.create(salesUser);
    }

    public int updateById(SalesUser salesUser){
        return this.salesUserMapper.updateById(salesUser);
    }

    public int deleteByIds(String ids){
        List<String> idList = Arrays.asList(ids.split(","));
        return this.salesUserMapper.deleteByIds(idList);
    }

    public List<SalesUser> list(SalesUser salesUser){
        return this.salesUserMapper.list(salesUser);
    }

    public PageQueryObject page(Map<String,Object> param){
        PageQueryObject result = new PageQueryObject();
        int count = salesUserMapper.pageCount(param);
        int page = (Integer)param.get("page");
        int rows = (Integer)param.get("rows");
        //起始行
        param.put("start",(page-1)*rows);
        //param.setStart((page-1)*rows);
        //总记录数
        int totalPages = (count%rows==0?count/rows:count/rows+1);
        if(totalPages < page){
            page = 1;
            param.put("page",page);
            param.put("start",0);
        }
        List<SalesUser> salesUserPerformances = salesUserMapper.page(param);
        result.setRows(salesUserPerformances);
        result.setPage(page);
        result.setRecords(rows);
        result.setTotal(totalPages);
        return result;
    }


    public SalesUser findById(Long id){
        SalesUser salesUser =  this.salesUserMapper.findById(id);
        return salesUser;
    }

    public SalesUser findCode(String code){
        return this.salesUserMapper.findCode(code);
    }

    public SalesUser login(String acount){
        return this.salesUserMapper.login(acount);
    }

    public List<SalesUser> salesUsers(){
    	return this.salesUserMapper.salesUsers();
    }

    public int assginShop(String assginUserId, String shopId, String oldUserId) {
        int result = 0;
        Map<String,Object> param = new HashMap<String,Object>();
        param.put("userId",shopId);
        param.put("salesmanId",assginUserId);
        param.put("oldSalesmanId",oldUserId);
        if(StringUtils.isNotBlank(assginUserId)){
            if(StringUtils.isNotBlank(shopId)){
                //salesUserMapper.updateUserSaleRelationship()
                //updateSalesmanIdByUserId\
                //updateSalesmanIdBySalesmanId
                result = salesUserMapper.updateSalesmanIdByUserId(param);
            }else {
                if (StringUtils.isNotBlank(oldUserId)) {
                    result = salesUserMapper.updateSalesmanIdBySalesmanId(param);
                }
            }
        }
        return result;
    }

    public int updateSalesmanIdByUserId(Map<String, Object> param){
        return salesUserMapper.updateSalesmanIdByUserId(param);
    }

    public int updateRe(Map<String, Object> param){
        return salesUserMapper.updateRe(param);
    }
}
