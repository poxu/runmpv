import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BuildRunMpv {
    private final static Map<String, String> editbin = Map.of(
        "2017", "Don't forget to specify",
        "2019", "Community\\VC\\Tools\\MSVC\\14.28.29333\\bin\\Hostx64\\x64\\editbin"
    );

    private final static Map<String, String> vcvars64bat = Map.of(
        "2017", "BuildTools\\VC\\Auxiliary\\Build\\vcvars64.bat",
        "2019", "Community\\VC\\Auxiliary\\Build\\vcvars64.bat"
    );

    public static void main(String[] args) throws IOException, InterruptedException {
        final String vsPath = "C:\\Program Files (x86)\\Microsoft Visual Studio\\2019\\";

//        String os = "windows";
        String os = "linux";
        String executableName;
        if ("linux".equals(os)) {
            executableName = "runmpv";
        } else {
            executableName = "runmpv.exe";
        }

        final String buildDirName = "build";

        final Path runmpvDir = Path.of(buildDirName);
        if (Files.exists(runmpvDir)) {
            final List<Path> allFilesInBuildDirectory =
                Files.walk(runmpvDir)
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
            for (int i = 0; i < allFilesInBuildDirectory.size() - 1; i++) {
                Files.delete(allFilesInBuildDirectory.get(i));
            }
        }

        File buildDirectory = new File(buildDirName);
        buildDirectory.mkdir();

//        final PrintWriter printWriter = new PrintWriter(buildDirName + "/sources.txt");
//        Files.find(Path.of("src/main/java"), 999
//                , (path, attr) -> attr.isRegularFile() && path.toString().endsWith(".java"))
//            .forEach(f -> printWriter.println(f.normalize().toAbsolutePath()));
//        printWriter.close();

        run(List.of(
//            "javac", "-d", "graalout", "@sources.txt"
            "javac",
            "-d", "graalout",
            "-sourcepath", "../src/main/java",
            "../src/main/java/com/evilcorp/StartSingleMpvInstance.java"

        ), buildDirectory);

        final List<String> windowsArgs = List.of("cmd", "/C", "call", "\"" + vsPath + vcvars64bat.get("2019") + "\"", "&&");
        final List<String> commonArgs = List.of(
            "native-image",
            "-H:ReflectionConfigurationFiles=../reflection.json",
            "--static",
            "-cp", "graalout",
            "com.evilcorp.StartSingleMpvInstance",
            "runmpv"
        );

        final List<String> buildNativeImage = new ArrayList<>();
        if ("windows".equals(os)) {
            buildNativeImage.addAll(windowsArgs);
        }
        buildNativeImage.addAll(commonArgs);

        run(buildNativeImage, buildDirectory);

        if ("windows".equals(os)) {
            run(List.of(vsPath + editbin.get("2019"),
                "/SUBSYSTEM:WINDOWS",
                executableName
            ), buildDirectory);
        }


        File runmpvProg = new File(buildDirName + "/runmpv-prog");
        runmpvProg.mkdir();

        final String dest = buildDirName + "/runmpv-prog";
        copy(List.of(
            buildDirName + "/" + executableName,
            "runmpv.properties",
            "logging.properties",
            "runmpv-install.bat",
            "runmpv-uninstall.bat",
            "runmpv-document.ico"), dest);

        Files.move(Path.of(buildDirName + "/" + executableName), Path.of(buildDirName + "/runmpv-prog/" + executableName), StandardCopyOption.REPLACE_EXISTING);
        Files.move(Path.of(buildDirName + "/runmpv-prog"), Path.of(buildDirName + "/runmpv"), StandardCopyOption.REPLACE_EXISTING);

        run(List.of(
            "tar", "-a", "-c", "-f", "runmpv.zip", "runmpv"
        ), buildDirectory);
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
