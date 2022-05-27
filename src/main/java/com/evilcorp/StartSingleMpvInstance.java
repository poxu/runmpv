package com.evilcorp;

import com.evilcorp.args.CommandLineRunMpvArguments;
import com.evilcorp.args.RunMpvArguments;
import com.evilcorp.fs.FsFile;
import com.evilcorp.fs.LocalFsPaths;
import com.evilcorp.fs.ManualFsFile;
import com.evilcorp.fs.RunMpvExecutable;
import com.evilcorp.fs.UserHomeDir;
import com.evilcorp.mpv.GenericMpvMessageQueue;
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
import com.evilcorp.settings.CompositeSettings;
import com.evilcorp.settings.ManualSettings;
import com.evilcorp.settings.MpvExecutableSettings;
import com.evilcorp.settings.PipeSettings;
import com.evilcorp.settings.RunMpvProperties;
import com.evilcorp.settings.RunMpvPropertiesFromSettings;
import com.evilcorp.settings.TextFileSettings;
import com.evilcorp.settings.UniquePipePerDirectorySettings;
import com.evilcorp.util.Shortcuts;

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

public class StartSingleMpvInstance {
    private final RunMpvArguments args;
    private MpvCommunicationChannel channel;
    private volatile RunMpvProperties config;
    private SyncServerCommunicationChannel serverChannel;
    private long lastResponse = Long.MAX_VALUE;
    private final CountDownLatch latch = new CountDownLatch(1);

    public StartSingleMpvInstance(RunMpvArguments args) {
        this.args = args;
    }

    public void close() {
        channel.detach();
        serverChannel.detach();
    }

    public boolean runsInBackground() {
        return config.sync() && serverChannel.isOpen();
    }

    public void waitUntilInitializationComplete() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        final FsFile videoDir = new ManualFsFile(args.video().path().getParent());

        final FsFile runMpvHomeDir = args.runMpvHome()
            .orElse(new RunMpvExecutable());
        initEmergencyLoggingSystem(runMpvHomeDir.path().toString() + "/logging.properties");

        final Logger logger = Logger.getLogger(StartSingleMpvInstance.class.getName());
        final LocalFsPaths fsPaths = new LocalFsPaths(
            new UserHomeDir(),
            runMpvHomeDir,
            videoDir
        );
        OperatingSystem os = new RuntimeOperatingSystem();
        final RunMpvProperties config = new RunMpvPropertiesFromSettings(
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
                            "openMode"      , "single-instance",
                            //--------------|-----------------------------------//
                            "pipeName"      , "myPipe",
                            //--------------|-----------------------------------//
                            "executableDir" , runMpvHomeDir.path().toString(),
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
        this.config = config;
        if (config.runnerLogFile() != null) {
            rerouteSystemOutStream(config.runnerLogFile());
        }

        final String videoFileName = args.video().path().toString();

        serverChannel = new SyncServerCommunicationChannel(config.syncAddress(), config.syncPort());
        if (config.sync()) {
            serverChannel.attach();
        }
        latch.countDown();

        logger.info("started");
        logger.info("runmpv argument is " + videoFileName);

        MpvCommunicationChannelProvider channelProvider = new MpvCommunicationChannelProvider(config,
            os.operatingSystemFamily());
        channel = channelProvider.channel();
        MvpInstanceProvider provider = new MvpInstanceProvider(config,
            os.operatingSystemFamily(), channel);
        MpvInstance mpvInstance = provider.mvpInstance();
        MpvEvents events = new MpvEvents(
            new GenericMpvMessageQueue(channel.channel()),
            new GenericMpvMessageQueue(serverChannel.channel())
        );
        if (config.sync() && serverChannel.isOpen()) {
            events.registerServerCallback(new ServerPauseCallback());
        }

        if (config.focusAfterOpen() && !mpvInstance.firstLaunch()) {
            events.execute(new GetProperty("pid"), mpvInstance.focusCallback());
        }
        events.registerMessageCallback((msg, __1, __2) -> lastResponse = System.nanoTime());
        events.execute(new GetProperty("filename"), (e, evts, __2) -> {
            final String playedFile = args.video().path().getFileName().toString();
            final FilenameResponse resp = new FilenameResponse(e);
            if (!resp.available() || !Objects.equals(resp.filename(), playedFile)) {
                evts.execute(new OpenFile(videoFileName));
                evts.execute(new ChangeTitle(videoFileName));
            }
            events.execute(new SetProperty("pause", false));
        });
        if (config.sync() && serverChannel.isOpen()) {
            events.observe(new ObserveProperty("pause"), new ObservePause());
        }
        while (events.hasPendingRequests()
            || (config.sync() && serverChannel.isOpen() && ((System.nanoTime() - lastResponse) < 1_000_000_000L))
        ) {
            events.receiveMessages();
            Shortcuts.sleep(50);
            if (config.sync() && serverChannel.isOpen()) {
                if (((System.nanoTime() - lastResponse) > 500_000_000L)) {
                    events.execute(new GetProperty("time-pos"), (m, e, s) -> {
                    });
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
     * @param args one argument supported - video file name
     * @throws RuntimeException exception might be thrown when starting logging system
     *                     or when starting emergency logging system
     */
    public static void main(String[] args) {
        final RunMpvArguments arguments = new CommandLineRunMpvArguments(args);
        if (arguments.empty()) {
            return;
        }
        final StartSingleMpvInstance runmpv = new StartSingleMpvInstance(arguments);
        final Logger logger = Logger.getLogger(StartSingleMpvInstance.class.getName());
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Future<?> task = executor.submit(runmpv::run);
        try {
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
            runmpv.close();
            executor.shutdownNow();
        }
    }
}