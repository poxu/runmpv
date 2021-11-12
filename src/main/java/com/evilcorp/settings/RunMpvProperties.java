package com.evilcorp.settings;

/**
 * Settings for runmpv.
 */
public interface RunMpvProperties {
    /**
     * Number of seconds to wait until mpv.exe starts,
     * before quitting with error.
     */
    Integer waitSeconds();

    /** Directory, where mpv.exe is located. */
    String mpvHomeDir();

    /** Name of controlling pipe for mpv.exe */
    String pipeName();

    /**
     * File where mpv.exe should write logs.
     * Only mpv.exe, controlled by runmpv will write logs there.
     */
    String mpvLogFile();

    /** Directory, where runmpv is located */
    String executableDir();

    /** File, where runmpv will put emergency logs,
     *  in case logging system fails.
     *  Regular logging system is controlled through,
     *  logging.properties
     */
    String runnerLogFile();

    /**
     * If true, mpv window is focused after new
     * file is loaded. If false, mpv window
     * is only focused on first launch
     */
    boolean focusAfterOpen();
}
