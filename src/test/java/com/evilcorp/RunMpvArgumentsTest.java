package com.evilcorp;

import com.evilcorp.args.RunMpvArguments;
import com.evilcorp.fs.FsFile;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RunMpvArgumentsTest {
    @Test
    public void noArguments() {
        String[] args = new String[0];
        RunMpvArguments arguments = new RunMpvArguments(args);
        assertTrue(arguments.empty());
    }

    @Test
    public void arguments() {
        String[] args = {"video-file"};
        RunMpvArguments arguments = new RunMpvArguments(args);
        assertFalse(arguments.empty());
    }

    @Test
    public void onlyVideoFile() {
        String[] args = {"video-file"};
        RunMpvArguments arguments = new RunMpvArguments(args);
        assertTrue(arguments.runMpvHome().isEmpty());
        final FsFile video = arguments.video();
        assertEquals("video-file", video.path().toString());
    }

    @Test
    public void videoAndExecutableDir() {
        String[] args = {"--runmpv-executable-dir=/non/existent/dir", "video-file"};
        RunMpvArguments arguments = new RunMpvArguments(args);
        final Optional<FsFile> fsFile = arguments.runMpvHome();
        assertEquals("/non/existent/dir", fsFile.orElseThrow().path().toString());
        final FsFile video = arguments.video();
        assertEquals("video-file", video.path().toString());
    }

    @Test
    public void executableWithNoEqualsSign() {
        String[] args = {"--runmpv-executable-dir/non/existent/dir", "video-file"};
        RunMpvArguments arguments = new RunMpvArguments(args);
        assertThrows(IllegalArgumentException.class, arguments::runMpvHome);
    }

    @Test
    public void executableWithWrongArgumentName() {
        String[] args = {"--not-runmpv-executable-dir/non/existent/dir", "video-file"};
        RunMpvArguments arguments = new RunMpvArguments(args);
        assertThrows(IllegalArgumentException.class, arguments::runMpvHome);
    }

    @Test
    public void donddd() {
        final String[] ray = "--runmpv-executable-dir=/path/to/runmpv-executable"
            .split("=");
        System.out.println(Arrays.toString(ray));
        final String substring = ray[0].substring(2);
        System.out.println(substring);
        final boolean b = substring.equals("runmpv-executable-dir");
        System.out.println(b);
    }

}