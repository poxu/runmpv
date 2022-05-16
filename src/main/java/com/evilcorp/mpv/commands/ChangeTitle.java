package com.evilcorp.mpv.commands;

import com.evilcorp.mpv.MpvCommand;

public class ChangeTitle implements MpvCommand {
    private final String videoFileName;

    public ChangeTitle(String videoFileName) {
        this.videoFileName = videoFileName;
    }

    @Override
    public String content() {
        final int lastSlashIdx = videoFileName.lastIndexOf("/");

        return "set title   \"" + videoFileName.substring(lastSlashIdx + 1) + "\" ";
    }
}
