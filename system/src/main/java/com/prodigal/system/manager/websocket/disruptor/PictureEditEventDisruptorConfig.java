package com.prodigal.system.manager.websocket.disruptor;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.lmax.disruptor.dsl.Disruptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: Disruptor 配置
 **/
@Configuration
public class PictureEditEventDisruptorConfig {
    @Resource
    private PictureEditEventWorkHandler pictureEditEventWorkHandler;
    @Bean("pictureEditEventDisruptor")
    public Disruptor<PictureEditEvent> messageDisruptorRingBuffer() {
        //ringBuffer 大小
        int bufferSize = 1024 * 128;
        Disruptor<PictureEditEvent> disruptor = new Disruptor<>(
                PictureEditEvent::new,
                bufferSize,
                ThreadFactoryBuilder.create().setNamePrefix("pictureEditEventDisruptor").build()
        );
        //设置消费者
        disruptor.handleEventsWithWorkerPool(pictureEditEventWorkHandler);
        //开启 Disruptor
        disruptor.start();
        return disruptor;
    }

}