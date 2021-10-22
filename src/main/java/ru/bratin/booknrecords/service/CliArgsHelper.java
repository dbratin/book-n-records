package ru.bratin.booknrecords.service;

import java.nio.file.Files;
import java.nio.file.Path;

public class CliArgsHelper {
    public static Path getFileArg(String[] args) {
        if(args.length < 1)
            throw new IllegalArgumentException();

        var path = Path.of(args[0]);

        if(!Files.exists(path) || Files.isDirectory(path) || !Files.isReadable(path))
            throw new IllegalArgumentException();

        return path;
    }
}
