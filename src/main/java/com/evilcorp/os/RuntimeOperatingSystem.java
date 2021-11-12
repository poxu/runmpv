package com.evilcorp.os;

public class RuntimeOperatingSystem implements OperatingSystem {
    @Override
    public OperatingSystemFamily operatingSystemFamily() {
        final String os = System.getProperty("os.name");
        if (os.toLowerCase().startsWith("windows")) {
            return OperatingSystemFamily.WINDOWS;
        } else if (os.toLowerCase().startsWith("linux")) {
            return OperatingSystemFamily.LINUX;
        } else {
            throw new RuntimeException("Unknown operating system " + os);
        }
    }
}
