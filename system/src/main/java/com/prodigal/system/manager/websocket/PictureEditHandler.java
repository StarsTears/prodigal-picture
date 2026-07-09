package com.prodigal.system.manager.websocket;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.prodigal.system.manager.websocket.disruptor.PictureEditEventProducer;
import com.prodigal.system.manager.websocket.model.PictureEditActionEnum;
import com.prodigal.system.manager.websocket.model.PictureEditMessageTypeEnum;
import com.prodigal.system.manager.websocket.model.PictureEditRequestMessage;
import com.prodigal.system.manager.websocket.model.PictureEditResponseMessage;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: websocket 处理器
 **/
@Slf4j
@Component
public class PictureEditHandler extends TextWebSocketHandler {

    private static final long EDIT_IDLE_TIMEOUT_MS = 30_000;
    private static final long HEARTBEAT_TIMEOUT_MS = 45_000;

    @Resource
    private UserService userService;
    @Resource
    @Lazy
    private PictureEditEventProducer pictureEditEventProducer;

    //使用 ConcurrentHashMap 保证线程安全
    //每张图片的编辑状态 key: pictureId  value: 编辑锁信息(包含userId/session/最近活跃时间)
    private static final Map<String, EditLockInfo> pictureEditingUsers = new ConcurrentHashMap<>();
    //保存所有的会话连接， key: pictureId  value: 所有会话集合
    private static final Map<String, Set<WebSocketSession>> pictureSessions = new ConcurrentHashMap<>();
    //会话心跳时间， key: session  value: 最后一次收到心跳的时间戳
    private static final Map<WebSocketSession, Long> sessionHeartbeats = new ConcurrentHashMap<>();

    /**
     * 编辑锁信息: 持有人、会话、心跳时间
     */
    static class EditLockInfo {
        final String userId;
        final WebSocketSession session;
        volatile long lastActiveTime;

        EditLockInfo(String userId, WebSocketSession session) {
            this.userId = userId;
            this.session = session;
            this.lastActiveTime = System.currentTimeMillis();
        }
    }

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
        String pictureId = (String) session.getAttributes().get("pictureId");

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
        //清理心跳记录
        sessionHeartbeats.remove(session);

        Map<String, Object> attributes = session.getAttributes();
        String pictureId = (String) attributes.get("pictureId");
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

        //心跳消息直接响应，不走 Disruptor，减少开销
        if (pictureEditMessageTypeEnum == PictureEditMessageTypeEnum.HEARTBEAT) {
            sessionHeartbeats.put(session, System.currentTimeMillis());
            PictureEditResponseMessage pong = new PictureEditResponseMessage();
            pong.setType(PictureEditMessageTypeEnum.HEARTBEAT.getValue());
            pong.setMessage("pong");
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(JSONUtil.toJsonStr(pong)));
            }
            return;
        }

        //从 session 中 获取公共参数
        Map<String, Object> attributes = session.getAttributes();
        String pictureId = (String) attributes.get("pictureId");
        User user = (User) attributes.get("user");
        //以下方法可改为使用 disruptor 优化
        pictureEditEventProducer.publishEvent(session, pictureEditRequestMessage, user, pictureId);
//        //执行相应的操作
//        switch (pictureEditMessageTypeEnum) {
//            case ENTER_EDIT:
//                handleEnterEditMessage(session, pictureEditRequestMessage, user, pictureId);
//                break;
//            case EDIT_ACTION:
//                handleEditActionMessage(session, pictureEditRequestMessage, user, pictureId);
//                break;
//            case EXIT_EDIT:
//                handleExitEditMessage(session, pictureEditRequestMessage, user, pictureId);
//                break;
//            default:
//                PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
//                pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.ERROR.getValue());
//                pictureEditResponseMessage.setMessage("未知的消息类型");
//                pictureEditResponseMessage.setUser(userService.getUserVO(user));
//                session.sendMessage(new TextMessage(JSONUtil.toJsonStr(pictureEditResponseMessage)));
//        }
    }

    /**
     * 定时清理: 过期编辑锁(心跳超时或会话已关闭) + 心跳超时会话 + 已关闭的会话
     */
    @Scheduled(fixedRate = 30_000)
    public void cleanStaleConnections() {
        long now = System.currentTimeMillis();

        //清理过期编辑锁
        for (Map.Entry<String, EditLockInfo> entry : pictureEditingUsers.entrySet()) {
            EditLockInfo lock = entry.getValue();
            if (!lock.session.isOpen() || now - lock.lastActiveTime > EDIT_IDLE_TIMEOUT_MS) {
                String pictureId = entry.getKey();
                try {
                    User user = userService.getById(Long.valueOf(lock.userId));
                    handleExitEditMessage(lock.session, null, user, pictureId);
                    log.info("清理过期编辑锁, pictureId={}, userId={}, idleMs={}",
                            pictureId, lock.userId, now - lock.lastActiveTime);
                } catch (Exception e) {
                    pictureEditingUsers.remove(pictureId);
                    log.warn("清理编辑锁异常, pictureId={}, userId={}", pictureId, lock.userId, e);
                }
            }
        }

        //清理心跳超时会话 和 已关闭的会话
        for (Map.Entry<String, Set<WebSocketSession>> entry : pictureSessions.entrySet()) {
            Set<WebSocketSession> sessions = entry.getValue();
            Iterator<WebSocketSession> it = sessions.iterator();
            while (it.hasNext()) {
                WebSocketSession session = it.next();
                Long lastHeartbeat = sessionHeartbeats.get(session);
                boolean heartbeatTimeout = lastHeartbeat != null && now - lastHeartbeat > HEARTBEAT_TIMEOUT_MS;
                if (!session.isOpen() || heartbeatTimeout) {
                    if (heartbeatTimeout) {
                        log.info("心跳超时关闭会话, pictureId={}, sessionId={}, idleMs={}",
                                entry.getKey(), session.getId(), now - lastHeartbeat);
                        try {
                            session.close();
                        } catch (IOException ignored) {
                        }
                    }
                    sessionHeartbeats.remove(session);
                    it.remove();
                }
            }
            if (sessions.isEmpty()) {
                pictureSessions.remove(entry.getKey());
            }
        }
    }

    public void handleEnterEditMessage(WebSocketSession session, PictureEditRequestMessage pictureEditRequestMessage, User user, String pictureId) throws Exception {
        //判断是否存在用户正在编辑该图片
        if (!pictureEditingUsers.containsKey(pictureId)) {
            //设置当前用户为编辑用户，记录 session 和心跳时间
            pictureEditingUsers.put(pictureId, new EditLockInfo(user.getId(), session));
            //构造响应
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.ENTER_EDIT.getValue());
            String message = String.format("用户%s 开始编辑", user.getUserName());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setUser(userService.getUserVO(user));
            this.broadcastToPicture(pictureId, pictureEditResponseMessage);
        }
    }

    public void handleEditActionMessage(WebSocketSession session, PictureEditRequestMessage pictureEditRequestMessage, User user, String pictureId) throws IOException {
        EditLockInfo lock = pictureEditingUsers.get(pictureId);
        if (lock == null) {
            return;
        }
        //刷新心跳时间
        lock.lastActiveTime = System.currentTimeMillis();

        String editAction = pictureEditRequestMessage.getEditAction();
        PictureEditActionEnum pictureEditActionEnum = PictureEditActionEnum.getEnumByValue(editAction);
        if (pictureEditActionEnum == null){
            return;
        }
        //确认是当前编辑用户
        if (lock.userId.equals(user.getId())) {
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

    public void handleExitEditMessage(WebSocketSession session, PictureEditRequestMessage pictureEditRequestMessage, User user, String pictureId) throws Exception {
        EditLockInfo lock = pictureEditingUsers.get(pictureId);
        if (lock != null && lock.userId.equals(user.getId())){
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
    private void broadcastToPicture(String pictureId, PictureEditResponseMessage pictureEditResponseMessage) throws Exception {
        broadcastToPicture(pictureId, pictureEditResponseMessage, null);
    }

    /**
     * 将正在编辑用户的编辑信息，广播给所有会话；排除当前操作人
     *
     * @param pictureId       图片id
     * @param responseMessage 响应信息
     * @param excludeSession  排除的会话
     */
    private void broadcastToPicture(String pictureId, PictureEditResponseMessage responseMessage, WebSocketSession excludeSession) throws IOException {
        //获取所有会话
        Set<WebSocketSession> sessionSet = pictureSessions.get(pictureId);
        if (CollUtil.isNotEmpty(sessionSet)) {
            //创建 ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();
            String message = objectMapper.writeValueAsString(responseMessage);
            TextMessage textMessage = new TextMessage(message);
            Iterator<WebSocketSession> it = sessionSet.iterator();
            while (it.hasNext()) {
                WebSocketSession session = it.next();
                //排除当前操作人
                if (session.equals(excludeSession)) {
                    continue;
                }
                //发送消息; 已关闭的会话直接移除
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                } else {
                    it.remove();
                }
            }
        }
    }
}
