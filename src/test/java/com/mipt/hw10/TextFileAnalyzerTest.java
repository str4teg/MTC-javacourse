package com.mipt.hw10;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class TextFileAnalyzerTest {

  @Test
  void testAnalyzeFile() throws IOException {
    TextFileAnalyzer analyzer = new TextFileAnalyzer();

    Path testFile = Files.createTempFile("test", ".txt");
    Files.write(testFile, Arrays.asList("1 2", "3 4 5"));

    TextFileAnalyzer.AnalysisResult result = analyzer.analyzeFile(testFile.toString());

    assertEquals(2, result.getLineCount());
    assertEquals(5, result.getWordCount());
    assertTrue(result.getCharCount() > 0);
    assertTrue(result.getCharFrequency().size() > 0);
    assertEquals(1, result.getCharFrequency().get('5'));
  }

  @Test
  void testSaveAnalysisResult() throws IOException {
    TextFileAnalyzer analyzer = new TextFileAnalyzer();

    TextFileAnalyzer.AnalysisResult result =
        new TextFileAnalyzer.AnalysisResult(2, 5, 20, new java.util.HashMap<>());

    Path outputFile = Files.createTempFile("analysis", ".txt");
    analyzer.saveAnalysisResult(result, outputFile.toString());

    assertTrue(Files.size(outputFile) > 0);

    String read = new String(Files.readAllBytes(outputFile));
    assertTrue(read.contains("lines=2"));
    assertTrue(read.contains("words=5"));
    assertTrue(read.contains("chars=20"));
  }
}
