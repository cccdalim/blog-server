package com.ljlblogserver.service;

import com.ljlblogserver.common.BusinessException;
import com.ljlblogserver.common.PageResult;
import com.ljlblogserver.dto.AlbumTimelineDto;
import com.ljlblogserver.dto.FilterOptionDto;
import com.ljlblogserver.dto.PhotoDto;
import com.ljlblogserver.dto.PhotoSaveRequest;
import com.ljlblogserver.entity.Photo;
import com.ljlblogserver.mapper.PhotoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PhotoService {

    private final PhotoMapper photoMapper;
    private final FileStorageService fileStorageService;

    public PageResult<PhotoDto> list(String category, String country, String city, int page, int pageSize) {
        int offset = Math.max(page - 1, 0) * pageSize;
        List<Photo> photos = photoMapper.findPage(category, country, city, offset, pageSize);
        long total = photoMapper.count(category, country, city);
        return new PageResult<>(photos.stream().map(this::toDto).toList(), total, page, pageSize);
    }

    public List<PhotoDto> recent(int limit) {
        return photoMapper.findRecent(limit).stream().map(this::toDto).toList();
    }

    public Map<String, List<FilterOptionDto>> filters(String category, String country) {
        List<Photo> allPhotos = photoMapper.findAll(null, null, null);
        List<Photo> scopedPhotos = photoMapper.findAll(category, country, null);
        Map<String, List<FilterOptionDto>> result = new HashMap<>();
        result.put("categories", buildOptions(allPhotos, Photo::getCategory));
        result.put("countries", buildOptions(allPhotos, Photo::getCountry));
        result.put("cities", buildOptions(scopedPhotos, Photo::getCity));
        return result;
    }

    public List<AlbumTimelineDto> timeline(String category, String country, String city) {
        return photoMapper.findAll(category, country, city).stream()
                .map(photo -> new AlbumTimelineDto(
                        photo.getPhotoDate().toString(),
                        photo.getCity() + " · " + photo.getCountry(),
                        photo.getTitle()))
                .toList();
    }

    @Transactional
    public PhotoDto create(PhotoSaveRequest request) {
        Photo photo = new Photo();
        photo.setFilePath(fileStorageService.normalizeStoredPath(request.getUrl()));
        photo.setTitle(request.getTitle());
        photo.setCity(request.getCity());
        photo.setCountry(request.getCountry());
        photo.setCategory(request.getCategory());
        photo.setPhotoDate(request.getDate());
        photo.setWidth(request.getWidth());
        photo.setHeight(request.getHeight());
        photoMapper.insert(photo);
        return toDto(photo);
    }

    @Transactional
    public PhotoDto update(String id, PhotoSaveRequest request) {
        Long photoId = Long.valueOf(id);
        Photo photo = photoMapper.findById(photoId);
        if (photo == null) {
            throw BusinessException.notFound("照片不存在");
        }
        String newPath = fileStorageService.normalizeStoredPath(request.getUrl());
        String oldPath = photo.getFilePath();
        if (oldPath != null && newPath != null && !oldPath.equals(newPath)) {
            fileStorageService.deleteIfExists(oldPath);
        }
        photo.setFilePath(newPath);
        photo.setTitle(request.getTitle());
        photo.setCity(request.getCity());
        photo.setCountry(request.getCountry());
        photo.setCategory(request.getCategory());
        photo.setPhotoDate(request.getDate());
        photo.setWidth(request.getWidth());
        photo.setHeight(request.getHeight());
        photoMapper.update(photo);
        return toDto(photo);
    }

    @Transactional
    public void delete(String id) {
        Long photoId = Long.valueOf(id);
        Photo photo = photoMapper.findById(photoId);
        if (photo == null) {
            throw BusinessException.notFound("照片不存在");
        }
        fileStorageService.deleteIfExists(photo.getFilePath());
        photoMapper.deleteById(photoId);
    }

    private List<FilterOptionDto> buildOptions(List<Photo> photos, java.util.function.Function<Photo, String> getter) {
        Map<String, Integer> counter = new HashMap<>();
        for (Photo photo : photos) {
            String value = getter.apply(photo);
            if (value == null || value.isBlank()) {
                continue;
            }
            counter.merge(value, 1, Integer::sum);
        }
        List<FilterOptionDto> options = new ArrayList<>();
        counter.forEach((value, count) -> options.add(new FilterOptionDto(value, value, count)));
        options.sort(Comparator.comparingInt(FilterOptionDto::getCount).reversed());
        return options;
    }

    private PhotoDto toDto(Photo photo) {
        PhotoDto dto = new PhotoDto();
        dto.setId(String.valueOf(photo.getId()));
        dto.setUrl(fileStorageService.toPublicUrl(photo.getFilePath()));
        dto.setTitle(photo.getTitle());
        dto.setCity(photo.getCity());
        dto.setCountry(photo.getCountry());
        dto.setCategory(photo.getCategory());
        dto.setDate(photo.getPhotoDate() != null ? photo.getPhotoDate().toString() : null);
        dto.setWidth(photo.getWidth());
        dto.setHeight(photo.getHeight());
        return dto;
    }
}
