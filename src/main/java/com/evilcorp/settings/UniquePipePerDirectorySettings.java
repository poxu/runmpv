package com.evilcorp.settings;

import com.evilcorp.fs.FsFile;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import static com.evilcorp.util.Shortcuts.bytesToHex;

/**
 * Settings implementation, which overrides pipeName for instance-per-directory
 * openMode. Setting value is computed with SHA-3 of path to video directory.
 */
public class UniquePipePerDirectorySettings implements SoftSettings {
    private final FsFile videoDir;
    private final MessageDigest digest;

    {
        try {
            digest = MessageDigest.getInstance("SHA3-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public UniquePipePerDirectorySettings(FsFile videoDir) {
        this.videoDir = videoDir;
    }

    @Override
    public Optional<String> setting(String name) {
        if (!"pipeName".equals(name)) {
            return Optional.empty();
        }
        final byte[] hashBytes = digest.digest(
            videoDir.path().toString().getBytes(StandardCharsets.UTF_8)
        );
        final String hashString = bytesToHex(hashBytes);
        return Optional.of(hashString);
    }
}
