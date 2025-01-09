package com.prodigal.system.manager.websocket;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.prodigal.system.manager.websocket.model.PictureEditActionEnum;
import com.prodigal.system.manager.websocket.model.PictureEditMessageTypeEnum;
import com.prodigal.system.manager.websocket.model.PictureEditRequestMessage;
import com.prodigal.system.manager.websocket.model.PictureEditResponseMessage;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: websocket 处理器
 **/
@Component
public class PictureEditHandler extends TextWebSocketHandler {
    @Resource
    private UserService userService;
    //使用 ConcurrentHashMap 保证线程安全
    //每张图片的编辑状态 key: pictureId  value: 当前正在编辑的userId
    private static final Map<Long, Long> pictureEditingUsers = new ConcurrentHashMap<>();
    //保存所有的会话连接， key: pictureId  value: 所有会话集合
    private static final Map<Long, Set<WebSocketSession>> pictureSessions = new ConcurrentHashMap<>();

    /**
     * 连接建立后，将当前用户加入到图片的编辑会话中
     *
     * @param session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //保存会话至集合
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");

        pictureSessions.putIfAbsent(pictureId, ConcurrentHashMap.newKeySet());
        pictureSessions.get(pictureId).add(session);
        //构造响应
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        String message = String.format("用户%s进入编辑状态", user.getUserName());
        pictureEditResponseMessage.setMessage(message);
        pictureEditResponseMessage.setUser(userService.getUserVO(user));
        //向编辑同一张图片的其它用户发送消息
        broadcastToPicture(pictureId, pictureEditResponseMessage);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Map<String, Object> attributes = session.getAttributes();
        Long pictureId = (Long) attributes.get("pictureId");
        User user = (User) attributes.get("user");
        //移除当前用户的编辑状态
        this.handleExitEditMessage(session, null, user, pictureId);

        //删除会话
        Set<WebSocketSession> sessionSet = pictureSessions.get(pictureId);
        if (sessionSet!=null){
            sessionSet.remove(session);
            if (sessionSet.isEmpty()){
                pictureSessions.remove(pictureId);
            }
        }
        //构造响应
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        String message = String.format("用户%s 离开编辑", user.getUserName());
        pictureEditResponseMessage.setMessage(message);
        pictureEditResponseMessage.setUser(userService.getUserVO(user));
        this.broadcastToPicture(pictureId, pictureEditResponseMessage);
    }

    /**
     * 接收消息;并执行相应的操作
     *
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        //将消息解析为 对象  PictureEditMessage
        PictureEditRequestMessage pictureEditRequestMessage = JSONUtil.toBean(message.getPayload(), PictureEditRequestMessage.class);
        String type = pictureEditRequestMessage.getType();
        PictureEditMessageTypeEnum pictureEditMessageTypeEnum = PictureEditMessageTypeEnum.valueOf(type);

        //从 session 中 获取公共参数
        Map<String, Object> attributes = session.getAttributes();
        Long pictureId = (Long) attributes.get("pictureId");
        User user = (User) attributes.get("user");
        //执行相应的操作
        switch (pictureEditMessageTypeEnum) {
            case ENTER_EDIT:
                handleEnterEditMessage(session, pictureEditRequestMessage, user, pictureId);
                break;
            case EDIT_ACTION:
                handleEditActionMessage(session, pictureEditRequestMessage, user, pictureId);
                break;
            case EXIT_EDIT:
                handleExitEditMessage(session, pictureEditRequestMessage, user, pictureId);
                break;
            default:
                PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
                pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.ERROR.getValue());
                pictureEditResponseMessage.setMessage("未知的消息类型");
                pictureEditResponseMessage.setUser(userService.getUserVO(user));
                session.sendMessage(new TextMessage(JSONUtil.toJsonStr(pictureEditResponseMessage)));
        }
    }

    public void handleEnterEditMessage(WebSocketSession session, PictureEditRequestMessage pictureEditRequestMessage, User user, Long pictureId) throws Exception {
        //判断是否存在用户正在编辑该图片
        if (!pictureEditingUsers.containsKey(pictureId)) {
            //设置当前用户为编辑用户
            pictureEditingUsers.put(pictureId, user.getId());
            //构造响应
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.ENTER_EDIT.getValue());
            String message = String.format("用户%s 开始编辑", user.getUserName());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setUser(userService.getUserVO(user));
            this.broadcastToPicture(pictureId, pictureEditResponseMessage);
        }
    }

    public void handleEditActionMessage(WebSocketSession session, PictureEditRequestMessage pictureEditRequestMessage, User user, Long pictureId) throws IOException {
        Long editingUserId = pictureEditingUsers.get(pictureId);
        String editAction = pictureEditRequestMessage.getEditAction();
        PictureEditActionEnum pictureEditActionEnum = PictureEditActionEnum.getEnumByValue(editAction);
        if (pictureEditActionEnum == null){
            return;
        }
        //确认是当前编辑用户
        if (editingUserId.equals(user.getId())) {
            //构造响应
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EDIT_ACTION.getValue());
            pictureEditResponseMessage.setEditAction(editAction);
            pictureEditResponseMessage.setMessage(String.format("用户%s 执行编辑操作：%s", user.getUserName(), pictureEditActionEnum.getText()));
            pictureEditResponseMessage.setUser(userService.getUserVO(user));
            //广播给除了当前客户端外的其它客户、否则会造成重复编辑
            this.broadcastToPicture(pictureId, pictureEditResponseMessage, session);
        }
    }

    public void handleExitEditMessage(WebSocketSession session, PictureEditRequestMessage pictureEditRequestMessage, User user, Long pictureId) throws Exception {
        Long editingUserId = pictureEditingUsers.get(pictureId);
        if (editingUserId!=null && editingUserId.equals(user.getId())){
            //移除当前用户的编辑状态
            pictureEditingUsers.remove(pictureId);
            //构造响应
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EXIT_EDIT.getValue());
            pictureEditResponseMessage.setMessage(String.format("用户%s 退出编辑", user.getUserName()));
            pictureEditResponseMessage.setUser(userService.getUserVO(user));
            this.broadcastToPicture(pictureId, pictureEditResponseMessage);
        }
    }

    // 全部广播
    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage) throws Exception {
        broadcastToPicture(pictureId, pictureEditResponseMessage, null);
    }

    /**
     * 将正在编辑用户的编辑信息，广播给所有会话；排除当前操作人
     *
     * @param pictureId       图片id
     * @param responseMessage 响应信息
     * @param excludeSession  排除的会话
     */
    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage responseMessage, WebSocketSession excludeSession) throws IOException {
        //获取所有会话
        Set<WebSocketSession> sessionSet = pictureSessions.get(pictureId);
        if (CollUtil.isNotEmpty(sessionSet)) {
            //创建 ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();
            //配置序列化、将Long类型 转为 String 类型;解决精度丢失的问题
            SimpleModule simpleModule = new SimpleModule();
            simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
            simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
            objectMapper.registerModule(simpleModule);
            String message = objectMapper.writeValueAsString(responseMessage);
            TextMessage textMessage = new TextMessage(message);
            for (WebSocketSession session : sessionSet) {
                //排除当前操作人
                if (session.equals(excludeSession)) {
                    continue;
                }
                //发送消息
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            }
        }
    }
}
