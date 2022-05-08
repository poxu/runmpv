package com.evilcorp.args;

import com.evilcorp.fs.FsFile;

import java.util.Optional;

public interface RunMpvArguments {
    Optional<FsFile> runMpvHome();

    FsFile video();

    boolean empty();
}
