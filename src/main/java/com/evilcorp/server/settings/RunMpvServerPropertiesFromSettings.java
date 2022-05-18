package com.evilcorp.server.settings;

import com.evilcorp.settings.SoftSettings;

public class RunMpvServerPropertiesFromSettings implements RunMpvServerProperties {
    private final SoftSettings settings;

    public RunMpvServerPropertiesFromSettings(SoftSettings settings) {
        this.settings = settings;
    }

    @Override
    public int port() {
        return settings.setting("port")
            .map(Integer::valueOf)
            .orElse(34218);
    }
}
