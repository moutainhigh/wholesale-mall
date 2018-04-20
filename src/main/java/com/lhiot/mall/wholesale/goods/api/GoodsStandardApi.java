package com.lhiot.mall.wholesale.goods.api;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.leon.microx.common.wrapper.ArrayObject;
import com.leon.microx.common.wrapper.PageObject;
import com.leon.microx.common.wrapper.ResultObject;
import com.lhiot.mall.wholesale.goods.domain.GoodsStandard;
import com.lhiot.mall.wholesale.goods.domain.girdparam.GoodsStandardGirdParam;
import com.lhiot.mall.wholesale.goods.service.GoodsStandardService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Api(description = "商品规格")
@RestController
@RequestMapping
public class GoodsStandardApi {
	
	private final GoodsStandardService GoodsStandardService;
	
	@Autowired
	public GoodsStandardApi(GoodsStandardService GoodsStandardService){
		this.GoodsStandardService = GoodsStandardService;
	}
	
    @PostMapping("/goodsstandard")
    @ApiOperation(value = "添加商品单位", response = Boolean.class)
    public ResponseEntity<?> add(@RequestBody GoodsStandard GoodsStandard){
    	if(GoodsStandardService.create(GoodsStandard)){
    		return ResponseEntity.created(URI.create("/goodsstandard/"+GoodsStandard.getId()))
    				.body(GoodsStandard);
    	}
    	return ResponseEntity.badRequest().body(ResultObject.of("添加失败"));
    }
    
    @PutMapping("/goodsstandard/{id}")
    @ApiOperation(value = "根据ID修改商品规格", response = GoodsStandard.class)
    public ResponseEntity<?> modify(@PathVariable("id") Long id, GoodsStandard GoodsStandard) {
        if (GoodsStandardService.update(GoodsStandard)) {
            return ResponseEntity.ok(GoodsStandard);
        }
        return ResponseEntity.badRequest().body(ResultObject.of("修改失败"));
    }

    @DeleteMapping("/goodsstandard/{id}")
    @ApiOperation(value = "根据id批量删除商品规格")
    public ResponseEntity<?> delete(@PathVariable("id") String id) {
    	GoodsStandardService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/goodsstandard/{id}")
    @ApiOperation(value = "根据ID查询商品规格", response = GoodsStandard.class)
    public ResponseEntity<GoodsStandard> goodsStandard(@PathVariable("id") Long id) {
        return ResponseEntity.ok(GoodsStandardService.goodsStandard(id));
    }

    @GetMapping("/goodsstandard/search")
    @ApiOperation(value = "根据关键词查询商品规格", response = GoodsStandard.class, responseContainer = "List")
    public ResponseEntity<List<GoodsStandard>> search(@RequestParam String keywords) {
        return ResponseEntity.ok(GoodsStandardService.findByKeywords(keywords));
    }
    
    @PostMapping("/goodsstandard/gird")
    @ApiOperation(value = "新建一个查询，分页查询商品规格", response = ArrayObject.class)
    public ResponseEntity<ArrayObject<PageObject>> grid(@RequestBody(required = true) GoodsStandardGirdParam param) {
        return ResponseEntity.ok(GoodsStandardService.pageQuery(param));
    }
}
