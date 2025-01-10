package com.prodigal.system.manager.websocket.disruptor;

import cn.hutool.json.JSONUtil;
import com.lmax.disruptor.WorkHandler;
import com.prodigal.system.manager.websocket.PictureEditHandler;
import com.prodigal.system.manager.websocket.model.PictureEditMessageTypeEnum;
import com.prodigal.system.manager.websocket.model.PictureEditRequestMessage;
import com.prodigal.system.manager.websocket.model.PictureEditResponseMessage;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: disruptor 事件处理器(消费者)
 **/
@Slf4j
@Component
public class PictureEditEventWorkHandler implements WorkHandler<PictureEditEvent> {
    @Resource
    private PictureEditHandler pictureEditHandler;
    @Resource
    private UserService userService;
    @Override
    public void onEvent(PictureEditEvent event) throws Exception {
        User user = event.getUser();
        Long pictureId = event.getPictureId();
        WebSocketSession session = event.getSession();
        PictureEditRequestMessage pictureEditRequestMessage = event.getPictureEditRequestMessage();
        String type = pictureEditRequestMessage.getType();
        PictureEditMessageTypeEnum pictureEditMessageTypeEnum = PictureEditMessageTypeEnum.valueOf(type);
        //执行相应的操作
        switch (pictureEditMessageTypeEnum) {
            case ENTER_EDIT:
                pictureEditHandler.handleEnterEditMessage(session, pictureEditRequestMessage, user, pictureId);
                break;
            case EDIT_ACTION:
                pictureEditHandler.handleEditActionMessage(session, pictureEditRequestMessage, user, pictureId);
                break;
            case EXIT_EDIT:
                pictureEditHandler.handleExitEditMessage(session, pictureEditRequestMessage, user, pictureId);
                break;
            default:
                PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
                pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.ERROR.getValue());
                pictureEditResponseMessage.setMessage("未知的消息类型");
                pictureEditResponseMessage.setUser(userService.getUserVO(user));
                session.sendMessage(new TextMessage(JSONUtil.toJsonStr(pictureEditResponseMessage)));
        }
    }
}
