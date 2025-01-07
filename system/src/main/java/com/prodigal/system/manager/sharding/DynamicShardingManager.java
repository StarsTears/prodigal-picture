package com.prodigal.system.manager.sharding;

import com.baomidou.mybatisplus.extension.toolkit.SqlRunner;
import com.prodigal.system.model.entity.Space;
import com.prodigal.system.model.enums.SpaceLevelEnum;
import com.prodigal.system.model.enums.SpaceTypeEnum;
import com.prodigal.system.service.SpaceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 分表管理器
 **/

@Slf4j
@Component
public class DynamicShardingManager {
    @Resource
    private DataSource dataSource;

    @Resource
    private SpaceService spaceService;
    private static final String LOGIC_TABLE_NAME = "picture";
    private static final String DATABASE_NAME = "logic_db";//配置文件中d数据库名称

    /**
     * Bean加载后，获取所有配置并更新配置
     */
    @PostConstruct
    public void initialize() {
        log.info("初始化动态分表配置···");
        this.updateShardingTableNodes();
    }

    /**
     * 动态建表；仅为 空间类型为旗舰版的 团队空间创建分表
     * @param space
     */
    public void createSpacePictureTable(Space space){
        if (space.getSpaceType() == SpaceTypeEnum.TEAM.getValue() && space.getSpaceLevel() == SpaceLevelEnum.FLAGSHIP.getValue()){
            Long spaceId = space.getId();
            String tableName = "picture_"+spaceId;
            //创建新表
            String createSql = "CREATE TABLE IF NOT EXISTS `"+tableName+"` LIKE `picture`";
            try{
                boolean update = SqlRunner.db().update(createSql);
                //更新分表
                updateShardingTableNodes();
            }catch (Exception e){
                log.error("创建图片空间分表失败，空间 id = {}", space.getId());
            }
        }

    }

    /**
     * 更新 ShardingSphere  的 actual-data-nodes 动态表名配置
     */
    private void updateShardingTableNodes() {
        Set<String> tableNames = fetchAllPictureTableNames();
        String newActualDataNodes = tableNames.stream()
                .map(tableName -> "prodigal-picture." + tableName)//确保前缀合法
                .collect(Collectors.joining(","));
        log.info("动态分表 actual-data-nodes 配置:{}", newActualDataNodes);

        ContextManager contextManager = this.getContextManager();
        ShardingSphereRuleMetaData ruleMetaData = contextManager.getMetaDataContexts()
                                                                .getMetaData()
                                                                .getDatabases()
                                                                .get(DATABASE_NAME)
                                                                .getRuleMetaData();
        Optional<ShardingRule> shardingRule  = ruleMetaData.findSingleRule(ShardingRule.class);
        if (shardingRule .isPresent()){
            ShardingRuleConfiguration ruleConfig = (ShardingRuleConfiguration)shardingRule.get().getConfiguration();
            List<ShardingTableRuleConfiguration> updateRules = ruleConfig.getTables()
                                                                    .stream()
                                                                    .map(oldTableRule -> {
                                                                        if (oldTableRule.getLogicTable().equals(LOGIC_TABLE_NAME)) {
                                                                            ShardingTableRuleConfiguration newTableRuleConfig = new ShardingTableRuleConfiguration(LOGIC_TABLE_NAME, newActualDataNodes);
                                                                            newTableRuleConfig.setDatabaseShardingStrategy(oldTableRule.getDatabaseShardingStrategy());
                                                                            newTableRuleConfig.setTableShardingStrategy(oldTableRule.getTableShardingStrategy());
                                                                            newTableRuleConfig.setKeyGenerateStrategy(oldTableRule.getKeyGenerateStrategy());
                                                                            newTableRuleConfig.setAuditStrategy(oldTableRule.getAuditStrategy());
                                                                            return newTableRuleConfig;
                                                                        }
                                                                        return oldTableRule;
                                                                    }).collect(Collectors.toList());

            ruleConfig.setTables(updateRules);
            contextManager.alterRuleConfiguration(DATABASE_NAME, Collections.singleton(ruleConfig));
            contextManager.reloadDatabase(DATABASE_NAME);
            log.info("动态分表规则 actual-data-nodes 更新成功.");
        }else{
            log.error("未找到 ShardingSphere 的分片规则配置,动态分表更新失败.");
        }

    }


    /**
     * 获取 shardingSphere ContextManager
     * @return
     */
    private ContextManager getContextManager() {
        try(ShardingSphereConnection connection = dataSource.getConnection().unwrap(ShardingSphereConnection.class)){
            return connection.getContextManager();
        } catch (SQLException e) {
            throw new RuntimeException("获取 shardingSphere ContextManager 失败 ",e);
        }
    }

    /**
     * 获取所有动态表名，包括初始表 picture 和 分表 picture_{spaceId}
     * @return
     */
    private Set<String> fetchAllPictureTableNames() {
        //查询所有 旗舰版的团队空间
        Set<Long> spaceIds = spaceService.lambdaQuery()
                .eq(Space::getSpaceType, SpaceTypeEnum.TEAM.getValue())
                .eq(Space::getSpaceLevel, SpaceLevelEnum.FLAGSHIP.getValue())
                .list()
                .stream()
                .map(Space::getId)
                .collect(Collectors.toSet());
        Set<String> tableNames = spaceIds.stream().map(spaceId -> LOGIC_TABLE_NAME + "_" + spaceId).collect(Collectors.toSet());
        //添加初始逻辑表
        tableNames.add(LOGIC_TABLE_NAME);
        return tableNames;
    }


}
