package com.jobportal.util;

import com.jobportal.exception.PortalException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** File handling: resume path validation, report export and an activity log. */
public final class FileUtil {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FILE_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private FileUtil() { }

    /** Verifies that a resume path points to an existing .pdf/.doc/.docx file. */
    public static String validateResumePath(String path) throws PortalException {
        File f = new File(path);
        String lower = path.toLowerCase();
        if (!(lower.endsWith(".pdf") || lower.endsWith(".doc") || lower.endsWith(".docx"))) {
            throw new PortalException("Resume must be a .pdf, .doc or .docx file.");
        }
        if (!f.isFile()) {
            throw new PortalException("File not found: " + path);
        }
        return f.getAbsolutePath();
    }

    /** Writes lines into reports/<prefix>_<timestamp>.txt and returns the file path. */
    public static String exportReport(String prefix, List<String> lines) throws PortalException {
        try {
            Path dir = Paths.get("reports");
            Files.createDirectories(dir);
            Path file = dir.resolve(prefix + "_" + LocalDateTime.now().format(FILE_STAMP) + ".txt");
            Files.write(file, lines, StandardCharsets.UTF_8);
            return file.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new PortalException("Could not write report: " + e.getMessage(), e);
        }
    }

    /** Appends an audit line to logs/activity.log; logging never breaks the app. */
    public static void log(String message) {
        try {
            Path dir = Paths.get("logs");
            Files.createDirectories(dir);
            try (BufferedWriter w = Files.newBufferedWriter(dir.resolve("activity.log"),
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                w.write(LocalDateTime.now().format(STAMP) + " | " + message);
                w.newLine();
            }
        } catch (IOException ignored) {
            // intentionally ignored
        }
    }
}
