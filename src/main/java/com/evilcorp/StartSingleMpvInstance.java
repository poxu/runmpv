package com.evilcorp;

import com.evilcorp.args.RunMpvArguments;
import com.evilcorp.fs.FsFile;
import com.evilcorp.fs.LocalFsPaths;
import com.evilcorp.fs.ManualFsFile;
import com.evilcorp.fs.MpvRunnerExecutable;
import com.evilcorp.fs.UserHomeDir;
import com.evilcorp.mpv.MpvInstance;
import com.evilcorp.mpv.MvpInstanceProvider;
import com.evilcorp.mpv.OpenFile;
import com.evilcorp.os.OperatingSystem;
import com.evilcorp.os.RuntimeOperatingSystem;
import com.evilcorp.settings.CompositeSettings;
import com.evilcorp.settings.ManualSettings;
import com.evilcorp.settings.MpvExecutableSettings;
import com.evilcorp.settings.MpvRunnerProperties;
import com.evilcorp.settings.MpvRunnerPropertiesFromSettings;
import com.evilcorp.settings.PipeSettings;
import com.evilcorp.settings.TextFileSettings;
import com.evilcorp.settings.UniquePipePerDirectorySettings;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class StartSingleMpvInstance {
    /**
     * Main method, which runs mpv
     *
     * @param args one argument supported - video file name
     * @throws IOException exception might be thrown when starting logging system
     *                     or when starting emergency logging system
     */
    public static void main(String[] args) throws IOException {
        final RunMpvArguments arguments = new RunMpvArguments(args);
        if (arguments.empty()) {
            return;
        }
        final FsFile videoDir = new ManualFsFile(arguments.video().path().getParent());

        final FsFile mpvRunnerHomeDir = arguments.mpvRunnerHome()
                .orElse(new MpvRunnerExecutable());
        LogManager.getLogManager().readConfiguration(
                new FileInputStream(mpvRunnerHomeDir.path().toString() + "/logging.properties")
        );
        final Logger logger = Logger.getLogger(StartSingleMpvInstance.class.getName());

        final LocalFsPaths fsPaths = new LocalFsPaths(
                new UserHomeDir(),
                mpvRunnerHomeDir,
                videoDir
        );
        OperatingSystem os = new RuntimeOperatingSystem();
        final MpvRunnerProperties config = new MpvRunnerPropertiesFromSettings(
                new PipeSettings(
                        new UniquePipePerDirectorySettings(videoDir),
                        new CompositeSettings(
                                new TextFileSettings(
                                        fsPaths.resolve("%r/runmpv.properties").path().toString()
                                ),
                                new CompositeSettings(
                                        new MpvExecutableSettings(
                                                os,
                                                "%r/../",
                                                ""
                                        ),
                                        new ManualSettings(Map.of(
                                                // @formatter:off
                                                // checkstyle:off
                                                //--------------|-----------------------------------//
                                                //     name     |         default value             //
                                                //--------------|-----------------------------------//
                                                "waitSeconds"   , "10",
                                                //--------------|-----------------------------------//
                                                "openMode"      , "instance-per-directory",
                                                //--------------|-----------------------------------//
                                                "pipeName"      , "myPipe",
                                                //--------------|-----------------------------------//
                                                "executableDir" , mpvRunnerHomeDir.path().toString(),
                                                //--------------|-----------------------------------//
                                                "focusAfterOpen", "true"
                                                //--------------|-----------------------------------//
                                                // checkstyle:on
                                                // @formatter:on
                                        ))
                                )
                        )
                ),
                fsPaths
        );
        if (config.runnerLogFile() != null) {
            rerouteSystemOutStream(config.runnerLogFile());
        }

        final String videoFileName = arguments.video().path().toString();

        logger.info("started");
        logger.info("runmpv argument is " + videoFileName);

        MvpInstanceProvider provider = new MvpInstanceProvider(config,
                os.operatingSystemFamily());
        MpvInstance mpvInstance = provider.mvpInstance();
        mpvInstance.execute(new OpenFile(videoFileName));
        if (config.focusAfterOpen()) {
            mpvInstance.focus();
        }
        /*
        if (firstLaunch) {
            sendCommand(mpvPipeStream, "set geometry 640x360");
        }
        */
        logger.info("Loading file " + videoFileName);
    }

    // A small logging system to diagnose why real logging system fails
    public static void rerouteSystemOutStream(String logfile) throws FileNotFoundException {
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