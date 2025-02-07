package com.nowcoder.community.web.core.event;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.web.config.WKProperties;
import com.nowcoder.community.web.modular.discusspost.entity.DiscussPost;
import com.nowcoder.community.web.core.event.Event;
import com.nowcoder.community.web.modular.message.entity.Message;
import com.nowcoder.community.web.modular.discusspost.service.DiscussPostService;
import com.nowcoder.community.web.modular.search.service.ElasticSearchService;
import com.nowcoder.community.web.modular.message.service.MessageService;
import com.nowcoder.community.web.core.constant.EventTopicConstants;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer {
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private ElasticSearchService elasticSearchService;
    @Autowired
    private WKProperties wkProperties;

    @KafkaListener(topics = {EventTopicConstants.TOPIC_COMMENT, EventTopicConstants.TOPIC_FOLLOW, EventTopicConstants.TOPIC_LIKE})
    public void handleCommentMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息内容为空");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误");
            return;
        }

        // 发送站内通知
        Message message = new Message();
        message.setFromId(1);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());

        if (!event.getData().isEmpty()) {
            for(Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }

        message.setContent(JSONObject.toJSONString(content));

        messageService.addMessage(message);
    }

    @KafkaListener(topics = {EventTopicConstants.TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息内容为空");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误");
            return;
        }

        DiscussPost post = discussPostService.findDiscussPostById(event.getEntityId());
        elasticSearchService.saveDiscussPost(post);
    }

    @KafkaListener(topics = {EventTopicConstants.TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息内容为空");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误");
            return;
        }

        elasticSearchService.deleteDiscussPost(event.getEntityId());
    }

    @KafkaListener(topics = {EventTopicConstants.TOPIC_SHARE})
    public void handleShareMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息内容为空");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误");
            return;
        }

        String url = (String)event.getData().get("url");
        String fileName = (String)event.getData().get("fileName");

        String cmd = wkProperties.getCommand() + " --quality 75 " + url + " " + wkProperties.getStorage() + "/" + fileName;
        try {
            Runtime.getRuntime().exec(cmd);
            logger.info("生成长图成功：" + cmd);
        } catch (IOException exception) {
            logger.error("生成长图失败:" + exception.getMessage());
        }
    }
}
