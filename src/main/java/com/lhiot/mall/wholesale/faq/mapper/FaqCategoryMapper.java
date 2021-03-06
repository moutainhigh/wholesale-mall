package com.lhiot.mall.wholesale.faq.mapper;

import com.lhiot.mall.wholesale.faq.domain.FaqCategory;
import com.lhiot.mall.wholesale.faq.domain.FaqCategoryTree;
import com.lhiot.mall.wholesale.faq.domain.gridparam.FaqGridParam;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FaqCategoryMapper {
    //查询FAQ分类的总数
    int pageQueryCount();
    
    List<FaqCategory> pageQuery(FaqGridParam param);

    int insertFaqCategory(FaqCategory faqCategory);
    
    int delete(List<Long> ids);
    
    int update(FaqCategory faqCategory);

    //后台管理系统----FAQ分类下拉框
    List<FaqCategory> searchFaqCategory();
    
    List<FaqCategoryTree> ztree();
}
