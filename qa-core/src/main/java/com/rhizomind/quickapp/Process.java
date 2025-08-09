package com.rhizomind.quickapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Process {

    public static Integer execute(List<String> command) throws InterruptedException, IOException {
        var shCommand = new ArrayList<String>();
        shCommand.add("/bin/sh");
        shCommand.add("-c");
        shCommand.add(Joiner.join(command));

        log.info("Executing process: " + shCommand);

        ProcessBuilder builder = new ProcessBuilder(shCommand);
        java.lang.Process process = builder.start();

        // stdout
        Thread out = new Thread(() -> {
            try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                r.lines().forEach(log::info);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // stderr
        Thread err = new Thread(() -> {
            try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) {
                r.lines().forEach(log::error);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        out.start();
        err.start();

        int code = process.waitFor();
        out.join();
        err.join();
        return code;
    }
}
