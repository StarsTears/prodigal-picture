package com.prodigal.system.manager.websocket.disruptor;

import com.prodigal.system.manager.websocket.model.PictureEditRequestMessage;
import com.prodigal.system.model.entity.User;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 图片编辑事件
 **/
@Data
public class PictureEditEvent {
    /**
     * 消息
     */
    private PictureEditRequestMessage pictureEditRequestMessage;

    /**
     * 当前用户的session
     */
    private WebSocketSession session;

    /**
     * 当前用户
     */
    private User user;

    /**
     * 图片id
     */
    private Long pictureId;
}
