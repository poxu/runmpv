package com.evilcorp.settings;

/**
 * Settings for runmpv.
 *
 * Unlike {@link SoftSettings}, this interface provides a separate
 * method for each setting. This is done for programmer's convenience.
 *
 * Code completion works and all settings are typed.
 */
@SuppressWarnings("MethodCount")
public interface RunMpvProperties {
    /**
     * Number of seconds to wait until mpv executable starts,
     * before quitting with error.
     */
    Integer waitSeconds();

    /**
     * Directory, where mpv executable is located.
     */
    String mpvHomeDir();

    /**
     * Name of controlling pipe or unix domain socket for mpv
     */
    String pipeName();

    /**
     * File where mpv executable should write logs.
     * Only mpv executable, controlled by runmpv will write logs there.
     */
    String mpvLogFile();

    /**
     * Directory, where runmpv is located
     */
    String executableDir();

    /**
     * File, where runmpv will put emergency logs, in case logging system fails.
     * Regular logging system is controlled through logging.properties
     */
    String runnerLogFile();

    /**
     * If true, mpv window is focused after new file is loaded.
     * If false, mpv window is only focused on first launch
     */
    boolean focusAfterOpen();

    /**
     * This turns on syncing with remote server. Currently, works as a feature
     * toggle.
     *
     * What it actually means is that runmpv wouldn't shutdown right after mpv
     * is started, but instead would work until mpv instance exits.
     *
     * Runmpv would listen to mpv and if playback is paused, runmpv would send
     * pause command to remote server, so that server could broadcast that
     * command to other runmpv instances, working on remote machines.
     * Same goes with unpause.
     *
     * Also, runmpv would listen to remote server commands to pause and unpause
     * in sync with other runmpv instances.
     *
     * @return current state of sync feature toggle
     */
    boolean sync();

    /**
     * Address for remote server to sync mpv instances
     * @return remote server address
     */
    String syncAddress();

    /**
     * Port for remote server to sync mpv instances
     * @return remote server port
     */
    int syncPort();

    String video();
}
