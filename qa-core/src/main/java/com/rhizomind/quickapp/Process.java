package com.rhizomind.quickapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Process {

    public static Integer execute(String command) throws InterruptedException, IOException {
        log.info("Executing command: " + command);
        ProcessBuilder builder = new ProcessBuilder("/bin/sh", "-c", command);
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
                r.lines().forEach(System.err::println);
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
