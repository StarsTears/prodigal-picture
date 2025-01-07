package com.prodigal.system.manager.sharding;

import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;
import java.util.Properties;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 自定义分表算法
 * 我们只对 旗舰版 空间下的图片 进行分表
 **/
public class PictureShardingAlgorithm implements StandardShardingAlgorithm<Long> {
    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Long> preciseShardingValue) {
        Long spaceId = preciseShardingValue.getValue();
        String logicTableName = preciseShardingValue.getLogicTableName();
        //查所有图片
        if (spaceId == null) {
            return logicTableName;
        }
        //根据 spaceId 生成动态表名
        String realTableName = "picture_" + spaceId;
        if (availableTargetNames.contains(realTableName)){
            return realTableName;
        }else{
            return logicTableName;
        }
    }

    @Override
    public Collection<String> doSharding(Collection<String> collection, RangeShardingValue<Long> rangeShardingValue) {
        return null;
    }

    @Override
    public Properties getProps() {
        return null;
    }

    @Override
    public void init(Properties properties) {

    }
}
