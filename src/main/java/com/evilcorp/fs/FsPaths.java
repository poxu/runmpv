package com.evilcorp.fs;

/**
 * Abstraction used to resolve paths, containing
 * placeholders like %homedir%
 */
public interface FsPaths {
    /**
     * @param path String containing path, probably containing placeholders
     * @return file in a file system. File might not exist.
     */
    FsFile resolve(String path);
}
