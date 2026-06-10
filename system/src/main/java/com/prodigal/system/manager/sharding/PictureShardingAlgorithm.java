package com.prodigal.system.manager.sharding;

import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 自定义分表算法
 * 我们只对 旗舰版 空间下的图片 进行分表
 **/
public class PictureShardingAlgorithm implements StandardShardingAlgorithm<Comparable<?>> {
    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Comparable<?>> preciseShardingValue) {
        String spaceId = String.valueOf(preciseShardingValue.getValue());
        String logicTableName = preciseShardingValue.getLogicTableName();
        if (spaceId == null || "null".equals(spaceId)) {
            return logicTableName;
        }
        String realTableName = "picture_" + spaceId;
        if (availableTargetNames.contains(realTableName)){
            return realTableName;
        }else{
            return logicTableName;
        }
    }

    @Override
    public Collection<String> doSharding(Collection<String> collection, RangeShardingValue<Comparable<?>> rangeShardingValue) {
        return null;
    }
}
