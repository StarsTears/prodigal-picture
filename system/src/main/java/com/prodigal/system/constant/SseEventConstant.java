package com.prodigal.system.constant;

/**
 * SSE 事件类型常量
 */
public interface SseEventConstant {

    /** 邮件发送通知——通知收件人刷新界面 */
    String EMAIL_SENT = "email_sent";

    /** 邮件发送成功——通知发送者刷新界面 */
    String EMAIL_SEND_SUCCESS = "email_send_success";

    /** 图片审核结果通知——通知上传者审核结果 */
    String PICTURE_REVIEWED = "picture_reviewed";
}
