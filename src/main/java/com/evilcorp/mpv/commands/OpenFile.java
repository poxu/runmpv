package com.evilcorp.mpv.commands;

import com.evilcorp.mpv.MpvCommand;

public class OpenFile implements MpvCommand {
    private final String videoFileName;

    public OpenFile(String videoFileName) {
        this.videoFileName = videoFileName;
    }

    @Override
    public String content() {
        return "loadfile   \"" + videoFileName.replaceAll("\\\\", "\\\\\\\\") + "\" replace";
    }
}
