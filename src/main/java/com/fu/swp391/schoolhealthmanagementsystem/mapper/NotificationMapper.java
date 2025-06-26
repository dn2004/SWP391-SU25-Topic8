package com.fu.swp391.schoolhealthmanagementsystem.mapper;

import com.fu.swp391.schoolhealthmanagementsystem.dto.notification.NotificationResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "isRead", source = "read")
    @Mapping(target = "link", source = "link")
    @Mapping(target = "createdAt", source = "createdAt")
    NotificationResponseDto toDto(Notification notification);
}

