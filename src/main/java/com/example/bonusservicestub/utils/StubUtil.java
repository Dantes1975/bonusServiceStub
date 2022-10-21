package com.example.bonusservicestub.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
@Slf4j
public class StubUtil {

    public static String getMessageFromFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        InputStream inputStream = Files.newInputStream(path);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

}
