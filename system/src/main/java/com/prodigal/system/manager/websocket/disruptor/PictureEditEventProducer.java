package com.prodigal.system.manager.websocket.disruptor;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.prodigal.system.manager.websocket.model.PictureEditRequestMessage;
import com.prodigal.system.model.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 事件生产者
 **/
@Slf4j
@Component
public class PictureEditEventProducer {
    @Resource
    private Disruptor<PictureEditEvent> disruptor;

    public void publishEvent(WebSocketSession session, PictureEditRequestMessage pictureEditRequestMessage, User user, Long pictureId) {
        RingBuffer<PictureEditEvent> ringBuffer = disruptor.getRingBuffer();
        //获取可以生成的位置
        long sequence = ringBuffer.next();
        PictureEditEvent pictureEditEvent = ringBuffer.get(sequence);
        pictureEditEvent.setSession(session);
        pictureEditEvent.setPictureEditRequestMessage(pictureEditRequestMessage);
        pictureEditEvent.setUser(user);
        pictureEditEvent.setPictureId(pictureId);

      ringBuffer.publish(sequence);
    }

    @PreDestroy
    public void close(){
        disruptor.shutdown();
    }
}
