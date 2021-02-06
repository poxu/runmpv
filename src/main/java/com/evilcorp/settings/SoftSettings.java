package com.evilcorp.settings;

import java.util.Optional;

public interface SoftSettings {
    Optional<String> setting(String name);
}
