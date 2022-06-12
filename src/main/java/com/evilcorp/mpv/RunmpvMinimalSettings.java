package com.evilcorp.mpv;

import com.evilcorp.fs.FsFile;

/**
 * Settings, which should be known before main config is fully read.
 * If one of those is unknown, runmpv might not be able to build configuration,
 * and maybe it will simply crash.
 */
public interface RunmpvMinimalSettings {
    /**
     * Directory, where currently launched video file is located.
     *
     * Currently, runmpv is supposed to work with local file systems.
     * Opening youtube videos will probably work, but needs testing.
     *
     * @return directory, where currently launched video file is located.
     */
    FsFile videoDir();

    /**
     * Directory, where runmpv executable is placed. runmpv.properties and
     * other files are supposed to be in the same directory by default.
     *
     * In case of native executable java can find this directory automatically.
     * But if launched from IDE, executable is actually a jar file in __target__
     * directory. Because of that, runmpv thinks bin dir is __target__ .
     *
     * runmpv.propertis is located it ../target, though. So, to debug runmpv in
     * IDE, developer should specify __executableDir__ in command line switches
     * manually.
     *
     * For example, developer can set __executableDir__ command line argument to
     * the directory, where binary runmpv executable resides, so that they could
     * debug with currently used configuration, instead of default.
     *
     * @return directory, where runmpv executable is placed.
     */
    FsFile runmpvBinDir();

    /**
     * @return File, containing runmpv logging settings.
     * logging.properties by default.
     */
    FsFile logSettings();

    FsFile userHome();
}
