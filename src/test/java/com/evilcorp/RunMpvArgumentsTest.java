package com.evilcorp;

import com.evilcorp.args.CommandLineRunMpvArguments;
import com.evilcorp.args.RunMpvArguments;
import com.evilcorp.fs.FsFile;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RunMpvArgumentsTest {
    @Test
    public void noArguments() {
        String[] args = new String[0];
        RunMpvArguments arguments = new CommandLineRunMpvArguments(args);
        assertTrue(arguments.empty());
    }

    @Test
    public void arguments() {
        String[] args = {"video-file"};
        RunMpvArguments arguments = new CommandLineRunMpvArguments(args);
        assertFalse(arguments.empty());
    }

    @Test
    public void onlyVideoFile() {
        String[] args = {"video-file"};
        RunMpvArguments arguments = new CommandLineRunMpvArguments(args);
        assertTrue(arguments.runMpvHome().isEmpty());
        final FsFile video = arguments.video();
        assertEquals("video-file", video.path().toString());
    }

    @Test
    public void videoAndExecutableDir() {
        String[] args = {"--runmpv-executable-dir=/non/existent/dir", "video-file"};
        RunMpvArguments arguments = new CommandLineRunMpvArguments(args);
        final Optional<FsFile> fsFile = arguments.runMpvHome();
        assertEquals("/non/existent/dir", fsFile.orElseThrow().path().toString());
        final FsFile video = arguments.video();
        assertEquals("video-file", video.path().toString());
    }

    @Test
    public void executableWithNoEqualsSign() {
        String[] args = {"--runmpv-executable-dir/non/existent/dir", "video-file"};
        RunMpvArguments arguments = new CommandLineRunMpvArguments(args);
        assertThrows(IllegalArgumentException.class, arguments::runMpvHome);
    }

    @Test
    public void executableWithWrongArgumentName() {
        String[] args = {"--not-runmpv-executable-dir/non/existent/dir", "video-file"};
        RunMpvArguments arguments = new CommandLineRunMpvArguments(args);
        assertThrows(IllegalArgumentException.class, arguments::runMpvHome);
    }
}