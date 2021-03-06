package com.lhiot.mall.wholesale.activity.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.lhiot.mall.wholesale.activity.domain.Activity;
import com.lhiot.mall.wholesale.activity.domain.FlashActivityGoods;
import com.lhiot.mall.wholesale.activity.domain.gridparam.ActivityGirdParam;

@Mapper
public interface ActivityMapper {

    int insert(Activity activity);

    int update(Activity activity);

    void removeInbatch(List<Long> ids);

    Activity select(long id);
    
    List<Activity> search(List<Long> ids);
    //查询给定时间范围内开启的活动
    List<Activity> avtivityIsOpen(Activity param);
    //分页查询分类
    List<Activity> pageQuery(ActivityGirdParam activityGirdParam);
    //查询分类的总记录数
    int pageQueryCount(ActivityGirdParam activityGirdParam);
    
    //查询当前开启的活动
    FlashActivityGoods currentActivity(String type);
    
    //查询下期开启的活动
    FlashActivityGoods nextActivity(Map<String,Object> map);

    Activity flashActivity(Long activityId);
}
