package org.jetbrains.bsp.bazel.server.bsp.managers;

import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos;
import org.apache.logging.log4j.Logger;
import org.jetbrains.bsp.bazel.server.bep.BepServer;

import java.io.File;
import java.io.FileInputStream;
import org.apache.logging.log4j.LogManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BepReader {
    private final BepServer bepServer;
    private final File eventFile;

    private final CompletableFuture<Boolean> bazelBuildFinished;
    private final CompletableFuture<Boolean> bepReaderFinished;

    private final Logger logger = LogManager.getLogger(BepReader.class);
    void start() {
        new Thread(() -> {
            try {
                logger.info("Start listening to BEP events");
                var stream = new FileInputStream(eventFile);
                BuildEventStreamProtos.BuildEvent event = null;
                while (!bazelBuildFinished.isDone() || (event = BuildEventStreamProtos.BuildEvent.parseDelimitedFrom(stream)) != null) {
                    if (event != null) {
                        bepServer.handleBuildEventStreamProtosEvent(event);
                    } else {
                        Thread.sleep(50);
                    }
                }
                logger.info("BEP events listening finished");
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                bepReaderFinished.complete(true);
            }
        }).start();
    }

    void finishBuild() {
        bazelBuildFinished.complete(true);
    }

    void await() throws ExecutionException, InterruptedException {
        bepReaderFinished.get();
    }

    public BepReader(BepServer bepServer) {
        this.bepServer = bepServer;
        this.bazelBuildFinished = new CompletableFuture<>();
        this.bepReaderFinished = new CompletableFuture<>();
        var attrs = PosixFilePermissions.asFileAttribute(
                Stream.of(PosixFilePermission.OWNER_WRITE,
                        PosixFilePermission.OWNER_READ).collect(Collectors.toSet()));
        File file;
        try {
            file = Files.createTempFile("bazel-bsp-binary", null, attrs).toFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        eventFile = file;
    }

    public File getEventFile() {
        return eventFile;
    }
}
