package com.prodigal.system.manager.websocket.model;

import com.prodigal.system.model.vo.UserVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 图片编辑响应信息
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PictureEditResponseMessage {
    /**
     * 消息类型，例如 "ENTER_EDIT", "EXIT_EDIT", "EDIT_ACTION"
     */
    private String type;

    /**
     * 信息
     */
    private String message;

    /**
     * 执行的编辑动作
     */
    private String editAction;

    /**
     * 用户信息
     */
    private UserVO user;
}