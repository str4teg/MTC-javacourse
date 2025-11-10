package com.mipt.hw10;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class TextFileAnalyzer {

  public static class AnalysisResult {
    private final long lineCount;
    private final long wordCount;
    private final long charCount;
    private final Map<Character, Long> charFrequency;

    public AnalysisResult(long lineCount, long wordCount, long charCount, Map<Character, Long> charFrequency) {
      this.lineCount = lineCount;
      this.wordCount = wordCount;
      this.charCount = charCount;
      this.charFrequency = charFrequency;
    }

    public long getLineCount() { return lineCount; }
    public long getWordCount() { return wordCount; }
    public long getCharCount() { return charCount; }
    public Map<Character, Long> getCharFrequency() { return charFrequency; }

    @Override
    public String toString() {
      return "lines=" + lineCount +
          ", words=" + wordCount +
          ", chars=" + charCount +
          ", freq=" + charFrequency;
    }
  }

  public AnalysisResult analyzeFile(String filePath) throws IOException {
    long lineCount = 0;
    long wordCount = 0;
    long charCount = 0;

    Map<Character, Long> freq = new HashMap<>();

    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
      String line;
      while ((line = br.readLine()) != null) {
        lineCount++;

        charCount += line.length();

        String[] words = line.trim().split("\\s+");
        if (!line.trim().isEmpty()) {
          wordCount += words.length;
        }

        for (char c : line.toCharArray()) {
          freq.put(c, freq.getOrDefault(c, 0L) + 1);
        }
      }
    }

    return new AnalysisResult(lineCount, wordCount, charCount, freq);
  }

  public void saveAnalysisResult(AnalysisResult result, String outputPath) throws IOException {
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath))) {
      bw.write(result.toString());
    }
  }
}
