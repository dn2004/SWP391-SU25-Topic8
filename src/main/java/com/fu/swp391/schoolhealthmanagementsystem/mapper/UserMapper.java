package com.fu.swp391.schoolhealthmanagementsystem.mapper;

import com.fu.swp391.schoolhealthmanagementsystem.dto.user.UserDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "active", target = "isActive")
    @Mapping(source = "childLinks", target = "linkedToStudent", qualifiedByName = "isParentLinked")
    UserDto userToUserDto(User user);

    @Named("isParentLinked")
    default boolean isParentLinked(List<?> childLinks) {
        return childLinks != null && !childLinks.isEmpty();
    }
}