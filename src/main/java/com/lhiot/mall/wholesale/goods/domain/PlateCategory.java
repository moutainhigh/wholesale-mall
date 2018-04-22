package com.lhiot.mall.wholesale.goods.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@ApiModel
@NoArgsConstructor
public class PlateCategory {

	@ApiModelProperty(notes="id",dataType="Long")
	private Long id;
	
	@ApiModelProperty(notes="商品规格id",dataType="Long")
	private Long parentId;
	
	@ApiModelProperty(notes="版块名称",dataType="String")
	private String plateName;
	
	@ApiModelProperty(notes="版块图片",dataType="String")
	private String plateImage;
	
	@ApiModelProperty(notes="排序",dataType="Integer")
	private Integer rank;
	
	@ApiModelProperty(notes="父分类名称",dataType="String")
	private String parentPlateNameName;
	
	@ApiModelProperty(notes="等级",dataType="Integer")
	private Integer levels;
}

