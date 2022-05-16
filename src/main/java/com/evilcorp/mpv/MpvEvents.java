package com.evilcorp.mpv;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class MpvEvents {
    private final Logger logger;
    private final MpvMessageQueue queue;
    private final MpvMessageQueue serverQueue;
    private final Map<Integer, MpvCallback> callbacks = new HashMap<>();
    private final Map<Integer, MpvCallback> permanentCallbacks = new HashMap<>();
    private MpvCallback anyMessageCallback;
    private MpvCallback serverCallback;

    public MpvEvents(MpvMessageQueue queue, MpvMessageQueue serverQueue) {
        this.queue = queue;
        this.serverQueue = serverQueue;
        logger = Logger.getLogger(MpvEvents.class.getName());
    }

    /**
     * Send a command to mpv instance
     *
     * @param command action mpv should execute
     */
    public void execute(MpvCommand command) {
        queue.send(command.content());
    }

    /**
     * Send a command to mpv instance
     * executing callback after command is finished
     *
     * @param command action mpv should execute
     */
    public void execute(MpvRequest command, MpvCallback callback) {
        callbacks.put(command.requestId(), callback);
        queue.send(command.content());
    }

    public void registerServerCallback(MpvCallback callback) {
        this.serverCallback = callback;
    }

    public void observe(MpvRequest command, MpvCallback callback) {
        queue.send(command.content());
        permanentCallbacks.put(command.requestId(), callback);
    }

    public boolean hasPendingRequests() {
        return !callbacks.isEmpty();
    }

    public void registerMessageCallback(MpvCallback callback) {
        this.anyMessageCallback = callback;
    }

    public void receiveServerMessages() {
        for (
            Optional<String> rawResponse = serverQueue.nextMessage();
            rawResponse.isPresent();
            rawResponse = serverQueue.nextMessage()
        ) {
            rawResponse.ifPresent(l -> logger.info("mpv msg: " + l));
            final String response = rawResponse.orElseThrow();
            serverCallback.execute(response, this, serverQueue);
        }
    }

    public void receiveMessages() {
        for (
            Optional<String> rawResponse = queue.nextMessage();
            rawResponse.isPresent();
            rawResponse = queue.nextMessage()
        ) {
            rawResponse.ifPresent(l -> logger.info("mpv msg: " + l));
            if (anyMessageCallback != null) {
                anyMessageCallback.execute(rawResponse.orElseThrow(), this, serverQueue);
            }
            final String response = rawResponse.orElseThrow();

            final Optional<Integer> permaRequest = permanentCallbacks.keySet().stream()
                .filter(key -> response.contains(key.toString()))
                .findAny();
            if (permaRequest.isPresent()) {
                permanentCallbacks.get(permaRequest.orElseThrow())
                    .execute(rawResponse.orElseThrow(), this, serverQueue);
            }
            final Optional<Integer> currentRequest = callbacks.keySet().stream()
                .filter(key -> response.contains(key.toString()))
                .findAny();
            if (currentRequest.isEmpty()) {
                return;
            }
            callbacks.get(currentRequest.orElseThrow())
                .execute(rawResponse.orElseThrow(), this, serverQueue);
            callbacks.remove(currentRequest.orElseThrow());
        }
    }
}
