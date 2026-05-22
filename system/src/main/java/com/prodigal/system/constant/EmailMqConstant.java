package com.prodigal.system.constant;

/**
 * 邮件相关 RabbitMQ 拓扑
 */
public interface EmailMqConstant {

   String CAPTCHA_EXCHANGE = "email.captcha.exchange";

   String CAPTCHA_QUEUE = "email.captcha.queue";

   String CAPTCHA_ROUTING_KEY = "email.captcha.send";
}
