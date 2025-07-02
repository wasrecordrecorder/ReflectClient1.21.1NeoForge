package com.dsp.main.Managers;

import com.dsp.main.Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FileUtil {

    public static List<String> getShaderSource(String fileName) {
        String source = "";
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(Main.class.getResourceAsStream("/assets/minecraft/shaders/" + fileName))));
        source = bufferedReader.lines().filter(str -> !str.isEmpty()).map(str -> str = str.replace("\t", "")).collect(Collectors.joining("\n"));
        try {
            bufferedReader.close();
        } catch (IOException ignored) {

        }
        return Collections.singletonList(source);
    }
    public static InputStream getResource(String str) {
        return FileUtil.class.getClassLoader().getResourceAsStream("assets/minecraft/" + str);
    }
    public static int getPd() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        return Integer.parseInt(name.split("@")[0]);
    }
}
