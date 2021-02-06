package com.evilcorp.fs;

import java.nio.file.Path;

/** Abstraction, representing file in the file system */
public interface FsFile {
    /** Underlying path which can be used to work with file */
    Path path();
}
