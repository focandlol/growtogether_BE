package com.campfiredev.growtogether.notification.dto;

import com.campfiredev.growtogether.notification.type.NotiType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {

    private Long id;
    private String content;
    private NotiType type;
    private Boolean read;
    private String url;
}
