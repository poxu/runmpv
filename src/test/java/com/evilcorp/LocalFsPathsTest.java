package com.evilcorp;

import com.evilcorp.fs.FsFile;
import com.evilcorp.fs.FsPaths;
import com.evilcorp.fs.LocalFsPaths;
import com.evilcorp.fs.ManualFsFile;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class LocalFsPathsTest {

    private FsPaths localFsPaths = new LocalFsPaths(
            new ManualFsFile(Path.of("c:/home")),
            new ManualFsFile(Path.of("c:/runmpv")),
            new ManualFsFile(Path.of("c:/video"))
    );

    @Test
    public void absolutePath() {
        final FsFile hiDir = localFsPaths.resolve("c:/hi");
        assertNotNull(hiDir);
        assertEquals(Path.of("c:/hi"), hiDir.path());
    }

    @Test
    public void relativeToHomeDir() {
        final FsFile hiDir = localFsPaths.resolve("%h/hi");
        assertNotNull(hiDir);
        assertEquals(Path.of("c:/home/hi"), hiDir.path());
    }

    @Test
    public void relativeToRunmpvDir() {
        final FsFile hiDir = localFsPaths.resolve("%r/hi");
        assertNotNull(hiDir);
        assertEquals(Path.of("c:/runmpv/hi"), hiDir.path());
    }

    @Test
    public void relativeToVideoDir() {
        final FsFile hiDir = localFsPaths.resolve("%v/hi");
        assertNotNull(hiDir);
        assertEquals(Path.of("c:/video/hi"), hiDir.path());
    }
}
