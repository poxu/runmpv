package com.evilcorp;

import com.evilcorp.fs.FsFile;
import com.evilcorp.fs.LocalFsPaths;
import com.evilcorp.fs.MpvRunnerExecutable;
import com.evilcorp.fs.UserHomeDir;
import com.evilcorp.fs.VideoDir;
import com.evilcorp.mpv.MpvInstance;
import com.evilcorp.mpv.MpvInstanceWindows;
import com.evilcorp.mpv.OpenFile;
import com.evilcorp.settings.CompositeSettings;
import com.evilcorp.settings.ManualSettings;
import com.evilcorp.settings.MpvRunnerProperties;
import com.evilcorp.settings.MpvRunnerPropertiesFromSettings;
import com.evilcorp.settings.TextFileSettings;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class StartSingleMpvInstance {
    private static Logger LOGGER;

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            return;
        }

        final FsFile mpvRunnerHomeDir = new MpvRunnerExecutable();
        LogManager.getLogManager().readConfiguration(
                new FileInputStream(mpvRunnerHomeDir.path().toString() + "/logging.properties")
        );
        LOGGER = Logger.getLogger(StartSingleMpvInstance.class.getName());

        final LocalFsPaths fsPaths = new LocalFsPaths(
                new UserHomeDir(),
                mpvRunnerHomeDir,
                new VideoDir()
        );
        final MpvRunnerProperties config = new MpvRunnerPropertiesFromSettings(
                new CompositeSettings(
                        new TextFileSettings(
                                fsPaths.resolve("%r/runmpv.properties").path().toString()
                        ),
                        new ManualSettings(Map.of(
                                "waitSeconds", "10",
                                "mpvHomeDir", "%h/..",
                                "pipeName", "myPipe",
                                "mpvLogFile", "%r/runmpv-mpv.log",
                                "runnerLogFile", "%v/runmpv.log",
                                "executableDir", mpvRunnerHomeDir.path().toString(),
                                "focusAfterOpen", "true"
                        ))
                ),
                fsPaths
        );
        if (config.runnerLogFile() != null) {
            rerouteSystemOutStream(config.runnerLogFile());
        }

        final String videoFileName = args[0];

        LOGGER.info("started");
        LOGGER.info("runmpv argument is " + args[0]);

        MpvInstance mpvInstance = new MpvInstanceWindows(config);
        mpvInstance.execute(new OpenFile(videoFileName));
        if (config.focusAfterOpen()) {
            mpvInstance.focus();
        }
        /*
        if (firstLaunch) {
            sendCommand(mpvPipeStream, "set geometry 640x360");
        }
        */
        LOGGER.info("Loading file " + videoFileName);
    }

    // A small logging system to diagnose why real logging system fails
    private static void rerouteSystemOutStream(String logfile) throws FileNotFoundException {
        final OutputStream out = new FileOutputStream(logfile);
        PrintStream printWriter = new PrintStream(out);
        System.out.close();
        System.err.close();
        System.setOut(printWriter);
        System.setErr(printWriter);
    }

    public static boolean isAscii(String string) {
        for (int i = 0; i < string.length(); ++i) {
            if (string.charAt(i) > 127) {
                return false;
            }
        }
        return true;
    }
}