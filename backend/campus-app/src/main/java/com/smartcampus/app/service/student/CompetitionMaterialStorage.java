package com.smartcampus.app.service.student;

import com.smartcampus.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Component
public class CompetitionMaterialStorage {

    private static final long MAX_SIZE = 20L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "doc", "docx", "xls", "xlsx", "zip");

    private final Path root;

    public CompetitionMaterialStorage(
            @Value("${app.competition.material-directory:data/competition-materials}") String directory) {
        this.root = Path.of(directory).toAbsolutePath().normalize();
    }

    public StoredMaterial store(MultipartFile file) {
        validate(file);
        String originalName = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = extension(originalName);
        String storageName = UUID.randomUUID() + "." + extension;
        Path destination = resolve(storageName);
        try {
            Files.createDirectories(root);
            Files.copy(file.getInputStream(), destination);
        } catch (IOException exception) {
            throw new BusinessException(CompetitionErrorCodes.MATERIAL_STORAGE_ERROR);
        }
        String contentType = file.getContentType() == null ? "application/octet-stream" : file.getContentType();
        return new StoredMaterial(storageName, originalName, contentType, file.getSize());
    }

    public Resource load(String storageName) {
        try {
            Path file = resolve(storageName);
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BusinessException(CompetitionErrorCodes.MATERIAL_FILE_NOT_FOUND);
            }
            return resource;
        } catch (IOException exception) {
            throw new BusinessException(CompetitionErrorCodes.MATERIAL_FILE_NOT_FOUND);
        }
    }

    public void deleteQuietly(String storageName) {
        if (storageName == null || storageName.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(resolve(storageName));
        } catch (IOException ignored) {
            // A failed cleanup must not invalidate a successful database update.
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getOriginalFilename() == null) {
            throw new BusinessException(CompetitionErrorCodes.MATERIAL_FILE_INVALID);
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException(CompetitionErrorCodes.MATERIAL_FILE_TOO_LARGE);
        }
        String name = StringUtils.cleanPath(file.getOriginalFilename());
        if (name.length() > 255 || name.contains("..") || name.contains("/") || name.contains("\\")
                || !ALLOWED_EXTENSIONS.contains(extension(name))) {
            throw new BusinessException(CompetitionErrorCodes.MATERIAL_FILE_INVALID);
        }
    }

    private String extension(String name) {
        int index = name.lastIndexOf('.');
        return index < 0 ? "" : name.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private Path resolve(String storageName) {
        Path path = root.resolve(storageName).normalize();
        if (!path.startsWith(root)) {
            throw new BusinessException(CompetitionErrorCodes.MATERIAL_FILE_INVALID);
        }
        return path;
    }

    public record StoredMaterial(String storageName, String originalName, String contentType, long size) {
    }
}
