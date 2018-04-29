package com.lhiot.mall.wholesale.order.domain.gridparam;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.leon.microx.common.wrapper.PageObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

@Data
@ToString
@ApiModel
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class OrderGridParam extends PageObject{
    @JsonProperty("orderId")
    private String orderCode;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("orderStatus")
    private String orderStatus;

    @JsonProperty("orderType")
    private String orderType;

    @JsonProperty("payStatus")
    private String payStatus;

    @JsonProperty("createTimeBegin")
    private String createTimeBegin;

    @JsonProperty("createTimeEnd")
    private String createTimeEnd;

    @ApiModelProperty(notes="分页查询开始页面",dataType="Integer")
    private Integer start;

    @ApiModelProperty(notes="页数",dataType="Integer")
    private Integer page;

    @ApiModelProperty(notes="每页行数",dataType="Integer")
    private Integer rows;

    @ApiModelProperty(notes="索引",dataType="String")
    private String sidx;

    @ApiModelProperty(notes="排序",dataType="String")
    private String sord;

    @ApiModelProperty(notes="用户ids",dataType="Long")
    private Long[] userIds;

    public void setUserIds(String userIds){
        String[] array = StringUtils.tokenizeToStringArray(userIds, ",");
        if (!ObjectUtils.isEmpty(array)){
            this.userIds = new Long[array.length];
            for (int i = 0; i < array.length; i++) {
                this.userIds[i] = Long.valueOf(array[i]);
            }
        }
    }
}
