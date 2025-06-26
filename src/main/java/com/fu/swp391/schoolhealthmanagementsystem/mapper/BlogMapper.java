package com.fu.swp391.schoolhealthmanagementsystem.mapper;

import com.fu.swp391.schoolhealthmanagementsystem.dto.blog.BlogResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.blog.CreateBlogRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.blog.UpdateBlogRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.Blog;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface BlogMapper {

    @Mapping(source = "author.fullName", target = "authorName")
    BlogResponseDto toResponseDto(Blog blog);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", source = "author")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Blog toEntity(CreateBlogRequestDto dto, User author);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(UpdateBlogRequestDto dto, @MappingTarget Blog blog);
}
