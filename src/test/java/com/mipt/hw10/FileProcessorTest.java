package com.mipt.hw10;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class FileProcessorTest {

  @Test
  void testSplitAndMergeFile() throws IOException {
    FileProcessor processor = new FileProcessor();

    Path testFile = Files.createTempFile("test", ".dat");
    byte[] testData = new byte[1500];
    new Random().nextBytes(testData);
    Files.write(testFile, testData);

    String outputDir = Files.createTempDirectory("parts").toString();
    List<Path> parts = processor.splitFile(testFile.toString(), outputDir, 500);

    // ===== asserts на split =====
    assertEquals(3, parts.size());
    for (Path p : parts) {
      assertTrue(Files.exists(p));
      assertTrue(Files.size(p) > 0);
    }

    Path mergedFile = Files.createTempFile("merged", ".dat");
    processor.mergeFiles(parts, mergedFile.toString());

    // ===== финальная проверка =====
    assertArrayEquals(Files.readAllBytes(testFile), Files.readAllBytes(mergedFile));
  }
}
