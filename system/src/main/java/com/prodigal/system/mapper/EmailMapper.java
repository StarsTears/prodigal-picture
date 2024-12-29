package com.prodigal.system.mapper;

import com.prodigal.system.model.entity.Email;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: EmailMapper
 **/
@Repository
public interface EmailMapper extends MongoRepository<Email, String> {
    // 自定义查询方法
    List<Email> findByTo(String to);
    List<Email> findByToAndSubjectContainsIgnoreCaseAndTxtContainsIgnoreCase(String to, String subject, String txt);
    List<Email> findByToAndSubjectContainsIgnoreCase(String to, String subject);
    List<Email> findByToAndTxtContainingIgnoreCase(String to, String txt);
    List<Email> findBySubjectContainsIgnoreCase(String subject);
    List<Email> findByTxtContainsIgnoreCase(String txt);
    List<Email> findBySubjectContainsIgnoreCaseAndTxtContainsIgnoreCase(String subject, String txt);
    List<Email> findByReceiveUserId(Long receiveUserId);
    List<Email> findByStatusAndToOrAndReceiveUserId(Integer status, String to,Long receiveUserId);

    //    List<Email> findByTimestampBetween(long startDate, long endDate);
    List<Email> findByFromAndToAndReceiveUserId(String from, String to, String receiveUserId);

    // 使用 @Query 注解进行复杂的查询
    @Query("{ 'from': ?0, 'to': ?1 }")
    List<Email> findEmailsByFromAndTo(String from, String to);
}
