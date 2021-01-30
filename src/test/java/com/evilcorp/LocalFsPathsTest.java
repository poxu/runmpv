package com.evilcorp;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LocalFsPathsTest {

    private FsPaths localFsPaths;

    @BeforeAll
    public void setUp() {
        localFsPaths = new LocalFsPaths(
                new ManualFsFile(Path.of("c:/windows")),
                new ManualFsFile(Path.of("c:/windows1")),
                new ManualFsFile(Path.of("c:/windows2"))
        );
    }

    @Test
    public void absolutePath() {
        final FsFile hiDir = localFsPaths.resolve("c:/hi");
        assertNotNull(hiDir);
        assertEquals(Path.of("C:/hi"), hiDir.path());
    }

    @Test
    public void relativeToHomeDir() {
        final FsFile hiDir = localFsPaths.resolve("%h/hi");
        assertNotNull(hiDir);
        assertEquals(Path.of("C:/windows/hi"), hiDir.path());
    }

    @Test
    public void relativeToMpvRunnerDir() {
        final FsFile hiDir = localFsPaths.resolve("%r/hi");
        assertNotNull(hiDir);
        assertEquals(Path.of("C:/windows1/hi"), hiDir.path());
    }

    @Test
    public void relativeToVideoDir() {
        final FsFile hiDir = localFsPaths.resolve("%v/hi");
        assertNotNull(hiDir);
        assertEquals(Path.of("C:/windows2/hi"), hiDir.path());
    }
}
