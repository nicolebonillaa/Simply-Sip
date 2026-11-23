package com.simplysip.service;

import com.simplysip.model.User;
import com.simplysip.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

@Service
public class ProfilePhotoService {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png");
    private static final String URL_PREFIX = "/uploads/avatars/";

    private final Path avatarDir;
    private final UserRepository userRepository;

    public ProfilePhotoService(
            @Value("${app.uploads.dir:uploads}") String uploadsDir,
            UserRepository userRepository) throws IOException {
        this.avatarDir = Path.of(uploadsDir, "avatars").toAbsolutePath().normalize();
        Files.createDirectories(avatarDir);
        this.userRepository = userRepository;
    }

    public User replacePhoto(User user, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Choose a photo to upload.");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Photo must be a JPEG or PNG image.");
        }
        BufferedImage source = ImageIO.read(file.getInputStream());
        if (source == null) {
            throw new IllegalArgumentException("That file isn't a valid image.");
        }

        // Re-encode as JPEG: proves it's a real image and strips metadata like GPS location.
        BufferedImage rgb = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
        g.drawImage(source, 0, 0, null);
        g.dispose();

        String filename = user.getId() + "-" + UUID.randomUUID() + ".jpg";
        ImageIO.write(rgb, "jpg", avatarDir.resolve(filename).toFile());

        String oldUrl = user.getPhotoUrl();
        user.setPhotoUrl(URL_PREFIX + filename);
        User saved = userRepository.save(user);
        deleteFile(oldUrl);   // only after the new one is saved
        return saved;
    }

    public User removePhoto(User user) {
        String oldUrl = user.getPhotoUrl();
        user.setPhotoUrl(null);
        User saved = userRepository.save(user);
        deleteFile(oldUrl);
        return saved;
    }

    private void deleteFile(String url) {
        if (url == null || !url.startsWith(URL_PREFIX)) return;
        Path target = avatarDir.resolve(url.substring(URL_PREFIX.length())).normalize();
        if (!target.startsWith(avatarDir)) return;   // never delete outside the folder
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
        }
    }
}