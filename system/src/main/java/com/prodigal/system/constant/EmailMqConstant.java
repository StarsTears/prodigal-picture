package com.prodigal.system.constant;

/**
 * 邮件相关 RabbitMQ 拓扑
 */
public interface EmailMqConstant {

   String CAPTCHA_EXCHANGE = "email.captcha.exchange";

   String CAPTCHA_QUEUE = "email.captcha.queue";

   String CAPTCHA_ROUTING_KEY = "email.captcha.send";

   String EMAIL_SEND_EXCHANGE = "email.send.exchange";

   String EMAIL_SEND_QUEUE = "email.send.queue";

   String EMAIL_SEND_ROUTING_KEY = "email.send.message";
}
