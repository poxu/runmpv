package com.evilcorp.build;

import com.evilcorp.build.files.TextFile;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("MethodCount")
public class BuildRunMpv {
    public static final String RUNMPV_ENTRY_POINT = "src/main/java/com/evilcorp/StartSingleMpvInstance.java";
    private static final Map<String, String> vsPath = Map.of(
        "2019", "C:\\Program Files (x86)\\Microsoft Visual Studio\\2019\\",
        "2017", "C:\\Program Files (x86)\\Microsoft Visual Studio\\2017\\"
    );
    private static final Map<String, String> editbin = Map.of(
        "2017", "BuildTools\\VC\\Tools\\MSVC\\14.16.27023\\bin\\Hostx64\\x64\\editbin",
        "2019", "Community\\VC\\Tools\\MSVC\\14.28.29333\\bin\\Hostx64\\x64\\editbin"
    );

    private static final  Map<String, String> vcvars64bat = Map.of(
        "2017", "BuildTools\\VC\\Auxiliary\\Build\\vcvars64.bat",
        "2019", "Community\\VC\\Auxiliary\\Build\\vcvars64.bat"
    );

    private enum OS {
        WINDOWS,
        LINUX;
        public boolean is(String os) {
            return toString().toLowerCase().equals(os.toLowerCase());
        }
    }

    /**
     * Builder script for runmpv. Java is better than bash!
     * @param args First argunent should be operating system either linux or
     *             windows.
     *
     *             If operating system is windows, then second argument should
     *             be Visual Studio version. Either 2017 or 2019.
     *
     *             Then user can specify path to Graal, with trailing slash
     *             included.
     *
     *             Linux example: BuildRunMpv linux /path/to/graal
     *             Windows example: BuildRunMpv windows 2017 C:/path/to/graal
     */
    @SuppressWarnings("ReturnCount")
    public static void main(String[] args) throws IOException, InterruptedException {
        final String runmpv = "runmpv";
        if (args.length == 0) {
            System.out.println("You should specify version as first argument.");
            return;
        }
        final String version = args[0];
        if (args.length == 1) {
            System.out.println("You should specify operating system manually. Use \"windows\" or \"linux\" " +
                "as second argument. No defaults.");
            return;
        }
        final String os = args[1];
        if (!List.of(
            OS.LINUX.toString().toLowerCase(),
            OS.WINDOWS.toString().toLowerCase()
        ).contains(os.toLowerCase())) {
            System.out.println("First argument should be exactly windows or linux. Other operating " +
                "systems are not supported");
            return;
        }
        final String vsEdition;
        if (OS.WINDOWS.is(os) && args.length < 3) {
            System.out.println("Visual Studio edition should be specified for windows builds. 2017 or 2019");
            return;
        }
        if (OS.WINDOWS.is(os)) {
            vsEdition = args[2];
        } else {
            vsEdition = " Specifying Visual Studio edition is pointless, because we're building runmpv on linux";
        }
        if (OS.WINDOWS.is(os) && !List.of("2019", "2017").contains(vsEdition)) {
            System.out.println("Specified Visual Studio edition is " + vsEdition + ", but it should be 2017 or 2019. " +
                "Only 2017 and 2019 arguments are supported");
            return;
        }
        final String graalBin;
        if (OS.WINDOWS.is(os) && args.length == 4) {
            graalBin = args[3];
        } else if (OS.LINUX.is(os) && args.length == 3) {
            graalBin = args[2];
        } else {
            graalBin = "";
        }
        final String executableName;
        if (OS.LINUX.is(os)) {
            executableName = runmpv;
        } else {
            executableName = runmpv + ".exe";
        }

        final TextFile runmpvOrig = new TextFile(
            Path.of(RUNMPV_ENTRY_POINT));
        runmpvOrig.replace("RUNMPV_VERSION_NUMBER", version);

        final String buildDirName = "build";
        createDirectoryIfNotExists(Path.of(buildDirName));
        emptyDirectoryIfExists(Path.of(buildDirName));

        final File buildDirectory = new File(buildDirName);

        run(List.of(
            graalBin + "javac",
            "-d", "graalout",
            "-sourcepath", "../src/main/java",
            "../src/main/java/com/evilcorp/StartSingleMpvInstance.java"

        ), buildDirectory);

        final List<String> windowsArgs = List.of("cmd", "/C", "call", "\"" + vsPath.get(vsEdition) + vcvars64bat.get(vsEdition) + "\"", "&&");
        final List<String> commonArgs = List.of(
            graalBin + "native-image",
            "-H:ReflectionConfigurationFiles=../reflection.json",
//            "--gc=epsilon",
            "--gc=serial",
            "--static",
            "-cp", "graalout",
            "com.evilcorp.StartSingleMpvInstance",
            runmpv
        );

        final List<String> buildNativeImage = new ArrayList<>();
        if (OS.WINDOWS.is(os)) {
            buildNativeImage.addAll(windowsArgs);
        }
        buildNativeImage.addAll(commonArgs);

        run(buildNativeImage, buildDirectory);

        if (OS.WINDOWS.is(os)) {
            run(List.of(vsPath.get(vsEdition) + editbin.get(vsEdition),
                "/SUBSYSTEM:WINDOWS",
                executableName
            ), buildDirectory);
        }

        createDirectoryIfNotExists(Path.of(buildDirName + "/" + runmpv + "-tmp"));

        final String dest = buildDirName + "/" + runmpv + "-tmp";
        copy(List.of(
            buildDirName + "/" + executableName,
            "runmpv.properties",
            "logging.properties",
            "runmpv.desktop",
            "focus.vbs",
            "runmpv-install.bat",
            "runmpv-uninstall.bat",
            "runmpv-document.ico"), dest);

        try (PrintStream out = new PrintStream(dest + "/version")) {
            out.println(version);
        }

        if (Files.exists(Path.of("runmpv-local.properties"))) {
            copy("runmpv-local.properties", dest + "/runmpv.properties");
        }

        Files.move(Path.of(buildDirName + "/" + executableName), Path.of(buildDirName + "/" + runmpv + "-tmp" + executableName), StandardCopyOption.REPLACE_EXISTING);
        Files.move(Path.of(buildDirName + "/" + runmpv + "-tmp"), Path.of(buildDirName + "/" + runmpv), StandardCopyOption.REPLACE_EXISTING);

        run(List.of(
            "tar", "-a", "-c", "-f", runmpv + ".zip", runmpv
        ), buildDirectory);

        final TextFile runmpvChanged = new TextFile(
            Path.of(RUNMPV_ENTRY_POINT));
        runmpvChanged.replace(version, "RUNMPV_VERSION_NUMBER");
    }

    private static void createDirectoryIfNotExists(Path buildDir) throws IOException {
        if (!Files.exists(buildDir)) {
            Files.createDirectory(buildDir);
        }
    }

    private static void emptyDirectoryIfExists(Path runmpvDir) throws IOException {
        if (Files.exists(runmpvDir)) {
            final List<Path> allFilesInBuildDirectory =
                Files.walk(runmpvDir)
                    .sorted(Comparator.reverseOrder())
                    .collect(Collectors.toList());
            for (int i = 0; i < allFilesInBuildDirectory.size() - 1; i++) {
                Files.delete(allFilesInBuildDirectory.get(i));
            }
        }
    }

    private static void copy(String src, String dest) {
        try {
            Files.copy(Path.of(src), Path.of(dest), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void copy(List<String> filenames, String dest) {
        filenames.stream()
            .map(Path::of)
            .forEach(f -> {
                try {
                    Files.copy(f, Path.of(dest).normalize().resolve(f.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    private static void run(List<String> arguments, File buildDirectory) throws InterruptedException {
        final ProcessBuilder processBuilder = new ProcessBuilder(arguments);
        processBuilder.directory(buildDirectory);

        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        try {
            final Process process = processBuilder.start();
            process.waitFor();
            final int exitValue = process.exitValue();
            if (exitValue != 0) {
                throw new RuntimeException("Process exited with bad code " + exitValue);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
