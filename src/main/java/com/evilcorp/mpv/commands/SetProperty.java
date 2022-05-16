package com.evilcorp.mpv.commands;

import com.evilcorp.mpv.MpvCommand;

import java.math.BigDecimal;

public class SetProperty implements MpvCommand {
    private final String name;
    private final String value;

    public SetProperty(String name, String value) {
        this.name = name;
        this.value = "\"" + value + "\"";
    }

    public SetProperty(String name, Boolean value) {
        this.name = name;
        this.value = value.toString().toLowerCase();
    }

    public SetProperty(String name, BigDecimal value) {
        this.name = name;
        this.value = value.toString().toLowerCase();
    }

    @Override
    public String content() {
        return "{ \"command\": [\"set_property\", \"" + name + "\", " + value + "] }";
    }
}
