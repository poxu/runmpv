package com.evilcorp.settings;

/**
 * Settings for runmpv.
 *
 * Unlike {@link SoftSettings}, this interface provides a separate
 * method for each setting. This is done for programmer's convenience.
 *
 * Code completion works and all settings are typed.
 */
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
}
