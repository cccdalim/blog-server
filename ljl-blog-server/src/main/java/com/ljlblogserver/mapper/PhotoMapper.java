package com.ljlblogserver.mapper;

import com.ljlblogserver.entity.Photo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PhotoMapper {

    List<Photo> findPage(@Param("category") String category,
                         @Param("country") String country,
                         @Param("city") String city,
                         @Param("offset") int offset,
                         @Param("limit") int limit);

    long count(@Param("category") String category,
               @Param("country") String country,
               @Param("city") String city);

    List<Photo> findRecent(@Param("limit") int limit);

    List<Photo> findAll(@Param("category") String category,
                        @Param("country") String country,
                        @Param("city") String city);

    Photo findById(@Param("id") Long id);

    int insert(Photo photo);

    int update(Photo photo);

    int deleteById(@Param("id") Long id);

    @Delete("DELETE FROM photos")
    int deleteAll();
}
