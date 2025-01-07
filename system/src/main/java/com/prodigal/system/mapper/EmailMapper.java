package com.prodigal.system.mapper;

import com.prodigal.system.model.entity.Email;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: EmailMapper
 **/
@Repository
public interface EmailMapper extends MongoRepository<Email, String> {
    // 自定义查询方法
}
