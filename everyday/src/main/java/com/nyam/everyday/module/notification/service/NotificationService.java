package com.nyam.everyday.module.notification.service;

import com.nyam.everyday.web.notification.dto.NotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class NotificationService {

  private final SimpMessagingTemplate simpMessagingTemplate;

  // 알림 Broadcast
  public void BroadcastContent(NotificationDto notificationDto) {
    simpMessagingTemplate.convertAndSend("/topic/notification", notificationDto.getContent());
    log.info("notification 채널에 브로드캐스트 완료");
  }}
