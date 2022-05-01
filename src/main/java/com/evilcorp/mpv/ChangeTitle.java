package com.evilcorp.mpv;

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
