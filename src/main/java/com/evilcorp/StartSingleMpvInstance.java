package com.evilcorp;

import com.evilcorp.args.CommandLineRunMpvArguments;
import com.evilcorp.args.RunMpvArguments;
import com.evilcorp.fs.FsFile;
import com.evilcorp.fs.LocalFsPaths;
import com.evilcorp.fs.RunMpvExecutable;
import com.evilcorp.fs.UserHomeDir;
import com.evilcorp.mpv.GenericMpvMessageQueue;
import com.evilcorp.mpv.MinSettings;
import com.evilcorp.mpv.MpvCommunicationChannel;
import com.evilcorp.mpv.MpvEvents;
import com.evilcorp.mpv.MpvInstance;
import com.evilcorp.mpv.MvpInstanceProvider;
import com.evilcorp.mpv.ServerPauseCallback;
import com.evilcorp.mpv.callbacks.FilenameResponse;
import com.evilcorp.mpv.callbacks.ObservePause;
import com.evilcorp.mpv.commands.ChangeTitle;
import com.evilcorp.mpv.commands.GetProperty;
import com.evilcorp.mpv.commands.ObserveProperty;
import com.evilcorp.mpv.commands.OpenFile;
import com.evilcorp.mpv.commands.SetProperty;
import com.evilcorp.mpv.communication.MpvCommunicationChannelProvider;
import com.evilcorp.mpv.communication.SyncServerCommunicationChannel;
import com.evilcorp.os.OperatingSystem;
import com.evilcorp.os.RuntimeOperatingSystem;
import com.evilcorp.settings.CommandLineSettings;
import com.evilcorp.settings.CompositeSettings;
import com.evilcorp.settings.ManualSettings;
import com.evilcorp.settings.MpvExecutableSettings;
import com.evilcorp.settings.PipeSettings;
import com.evilcorp.settings.RunMpvProperties;
import com.evilcorp.settings.RunMpvPropertiesFromSettings;
import com.evilcorp.settings.SoftSettings;
import com.evilcorp.settings.TextFileSettings;
import com.evilcorp.settings.UniquePipePerDirectorySettings;
import com.evilcorp.util.Shortcuts;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.evilcorp.mpv.GenericMpvMessageQueue.rerouteSystemOutStream;
import static com.evilcorp.util.Shortcuts.initEmergencyLoggingSystem;

public class StartSingleMpvInstance implements AutoCloseable {
    public static final String VERSION = "RUNMPV_VERSION_NUMBER";

    private MpvCommunicationChannel channel;
    private volatile RunMpvProperties config;
    private SyncServerCommunicationChannel serverChannel;
    private long lastResponse = Long.MAX_VALUE;
    /**
     * This lock is needed, because runmpv is launched in a separate thread and
     * main thread makes sure this thread doesn't hang.
     * <p/>
     * It runmpv thread works longer than 10 seconds, main thread kills runmpv
     * thread.
     * <p/>
     * But if runmpv is used to synchronise instances of mpv on different
     * machines, then thread should not quit while mpv instance exists.
     * <p/>
     * To decide which mode runmpv works in it checks __sync__ setting in
     * runmpv.properties . So config should be read before main thread decides
     * whether it should kill runmpv thread after 10 seconds or not.
     * <p/>
     * There's more. If another instance of runmpv is already running in
     * daemon mode, then this instance of runmpv shouldn't be daemon. It should
     * just send mpv command to open the video and then quit.
     * <p/>
     * Also, runmpv shouldn't work in background if runmpv couldn't connect to
     * remote server. This behaviour will change in the future, because if there's
     * no connection when runmpv starts it might be established later. But right
     * now, during development phase if there's no connection it probably means,
     * that sync server is down, and I just want to watch a video.
     * <p/>
     * This lock should be released after it is safe to call
     * {@link StartSingleMpvInstance#runsInBackground()} method, which checks
     * if runmpv should work in background or not. Currently, it's after config
     * is fully read and after runmpv made a fair attempt to connect to remote
     * sync server and after runmpv knows if mpv was running before runmpv
     * started.
     * <p/>
     * That, by the way, makes {@link StartSingleMpvInstance#runsInBackground()}
     * {@link StartSingleMpvInstance#config},
     * {@link StartSingleMpvInstance#serverChannel} ,
     * {@link StartSingleMpvInstance#mpvInstance}
     * and lock release position tightly coupled.
     * <p/>
     * Not a good solution, but will do for now.
     */
    private final CountDownLatch latch = new CountDownLatch(1);
    private final Logger logger = Logger.getLogger(StartSingleMpvInstance.class.getName());
    private final SoftSettings commandLineSettings;
    private MpvInstance mpvInstance;

    public StartSingleMpvInstance(SoftSettings commandLineSettings) {
        this.commandLineSettings = commandLineSettings;
    }

    @Override
    public void close() {
        if (channel != null) {
            channel.detach();
        }
        if (serverChannel != null) {
            serverChannel.detach();
        }
    }

    public boolean runsInBackground() {
        return config != null && config.sync()
            && serverChannel != null && serverChannel.isOpen()
            && mpvInstance != null && mpvInstance.firstLaunch();
    }

    /**
     * If initialisation is not complete within 3 seconds, something is
     * probably wrong.
     */
    public void waitUntilInitializationComplete() {
        try {
            final boolean initCompleteInTime = latch.await(3, TimeUnit.SECONDS);
            if (!initCompleteInTime) {
                logger.warning("Couldn't complete initialisation in 3 seconds. " +
                    "Something is probably wrong");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void runAndReleaseLock() {
        Exception e = null;
        try {
            run();
        } catch (Exception ex) {
            e = ex;
        } finally {
            latch.countDown();
        }
        if (e != null) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        final SoftSettings minDefaultSettings = new ManualSettings(
            "executableDir", new RunMpvExecutable().path().toString(),
            "logSettings", "logging.properties",
            "userHome", new UserHomeDir().path().toString(),
            "runmpvSettings", "%r/runmpv.properties"
        );
        final SoftSettings startSettings = new CompositeSettings(
            commandLineSettings,
            minDefaultSettings
        );
        final MinSettings minSettings = new MinSettings(startSettings);
        final FsFile videoDir = minSettings.videoDir();
        final FsFile runMpvHomeDir = minSettings.runmpvBinDir();

        final String initLogFile = runMpvHomeDir.path()
            .resolve(minSettings.logSettings().path()).toString();
        initEmergencyLoggingSystem(initLogFile);

        final LocalFsPaths fsPaths = new LocalFsPaths(
            minSettings.userHome(),
            runMpvHomeDir,
            videoDir
        );
        OperatingSystem os = new RuntimeOperatingSystem();
        final RunMpvProperties config = new RunMpvPropertiesFromSettings(
            new PipeSettings(
                new UniquePipePerDirectorySettings(videoDir),
                new CompositeSettings(
                    commandLineSettings,
                    new TextFileSettings(
                        fsPaths.resolve(minSettings.runmpvSettings())
                            .path().toString()
                    ),
                    new MpvExecutableSettings(
                        os,
                        "%r/../",
                        ""
                    ),
                    new ManualSettings(
                        "runmpvTmpDir", System.getenv("XDG_RUNTIME_DIR")
                    ),
                    minDefaultSettings,
                    new ManualSettings(Map.of(
                        // @formatter:off
                        // checkstyle:off
                        //--------------|-----------------------------------//
                        //     name     |         default value             //
                        //--------------|-----------------------------------//
                        "waitSeconds"   , "10",
                        //--------------|-----------------------------------//
                        "openMode"      , "single-instance",
                        //--------------|-----------------------------------//
                        "pipeName"      , "myPipe",
                        //--------------|-----------------------------------//
                        "focusAfterOpen", "true",
                        //--------------|-----------------------------------//
                         "runmpvTmpDir" , "/tmp"
                        //--------------|-----------------------------------//
                        // checkstyle:on
                        // @formatter:on
                    ))
                )
            ),
            fsPaths
        );
        this.config = config;
        final String configLogFile = runMpvHomeDir.path()
            .resolve(config.logSettings().path()).toString();
        initEmergencyLoggingSystem(configLogFile);
        if (config.runnerLogFile() != null) {
            rerouteSystemOutStream(config.runnerLogFile());
        }

        final String videoFileName = config.video();
        if (!Path.of(videoFileName).isAbsolute()) {
            throw new RuntimeException("runmpv does not support relative paths yet. " +
                "Current path provided to runmpv is " + videoFileName);
        }

        serverChannel = new SyncServerCommunicationChannel(config.syncAddress(), config.syncPort());
        if (config.sync()) {
            serverChannel.attach();
        }

        logger.info("started");
        logger.info("runmpv argument is " + videoFileName);

        MpvCommunicationChannelProvider channelProvider = new MpvCommunicationChannelProvider(config,
            os.operatingSystemFamily());
        channel = channelProvider.channel();
        MvpInstanceProvider provider = new MvpInstanceProvider(config,
            os.operatingSystemFamily(), channel);
        mpvInstance = provider.mvpInstance();
        latch.countDown();
        MpvEvents events = new MpvEvents(
            new GenericMpvMessageQueue(channel.channel()),
            new GenericMpvMessageQueue(serverChannel.channel())
        );
        if (config.focusAfterOpen() && !mpvInstance.firstLaunch()) {
            events.execute(new GetProperty("pid"), mpvInstance.focusCallback());
        }
        events.execute(new GetProperty("filename"), (e, evts, __2) -> {
            final String playedFile = Path.of(config.video()).getFileName().toString();
            final FilenameResponse resp = new FilenameResponse(e);
            if (!resp.available() || !Objects.equals(resp.filename(), playedFile)) {
                evts.execute(new OpenFile(videoFileName));
                evts.execute(new ChangeTitle(videoFileName));
            }
            events.execute(new SetProperty("pause", false));
        });
        while (events.hasPendingRequests()) {
            events.receiveMessages();
            Shortcuts.sleep(50);
        }
        if (runsInBackground()) {
            events.registerServerCallback(new ServerPauseCallback());
            events.registerMessageCallback((msg, __1, __2) -> lastResponse = System.nanoTime());
            events.observe(new ObserveProperty("pause"), new ObservePause());
            while ((System.nanoTime() - lastResponse) < 1_000_000_000L) {
                events.receiveMessages();
                Shortcuts.sleep(50);
                if (((System.nanoTime() - lastResponse) > 500_000_000L)) {
                    events.execute(new GetProperty("time-pos"), (m, e, s) -> { });
                }
                events.receiveServerMessages();
            }
        }
        /*
        if (firstLaunch) {
            sendCommand(mpvPipeStream, "set geometry 640x360");
        }
        */
        logger.info("Loading file " + videoFileName);
    }

    /**
     * Main method, which runs mpv
     *
     * @param args if first argument is --version, runmpv will print version
     *             and then quit.
     *             <p/>
     *             If there's no arguments, runmpv will quit silently.
     *             <p/>
     *             Video file should be last argument.
     *             <p/>
     *             Also, same arguments as in runmpv.properties are accepted.
     *             For example, you could
     *             runmpv --openMode=instance-per-directory <video file name>
     *             to open video in separate mpv instance just this once.
     */
    @SuppressWarnings("ReturnCount")
    public static void main(String[] args) {
        if (args.length > 0 && "--version".equals(args[0])) {
            System.out.println(VERSION);
            return;
        }
        final RunMpvArguments arguments = new CommandLineRunMpvArguments(args);
        if (arguments.empty()) {
            return;
        }
        final CommandLineSettings commandLineSettings = new CommandLineSettings(args);
        final Logger logger = Logger.getLogger(StartSingleMpvInstance.class.getName());
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        try (final StartSingleMpvInstance runmpv = new StartSingleMpvInstance(commandLineSettings)) {
            final Future<?> task = executor.submit(runmpv::runAndReleaseLock);
            runmpv.waitUntilInitializationComplete();
            if (runmpv.runsInBackground()) {
                task.get();
            } else {
                task.get(10, TimeUnit.SECONDS);
            }
            logger.info("runmpv worked fine and finished successfully");
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Thread interrupted" +
                " who knows why", e);
        } catch (ExecutionException e) {
            logger.log(Level.SEVERE, "Something with executor service" +
                " who knows why", e);
        } catch (TimeoutException e) {
            logger.log(Level.SEVERE, "runmpv has been working more than 10" +
                " seconds which shouldn't happen", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected exception", e);
        } finally {
            executor.shutdownNow();
        }
    }
}
