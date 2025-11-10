package com.mipt.hw10;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class FileProcessor {

  public List<Path> splitFile(String sourcePath, String outputDir, int partSize) throws IOException {
    List<Path> partPaths = new ArrayList<>();

    Path source = Paths.get(sourcePath);
    Path dir = Paths.get(outputDir);
    if (!Files.exists(dir)) Files.createDirectories(dir);

    String fileName = source.getFileName().toString();

    try (FileChannel inChannel = FileChannel.open(source, StandardOpenOption.READ)) {
      ByteBuffer buffer = ByteBuffer.allocate(partSize);

      int partIndex = 1;
      while (inChannel.read(buffer) > 0) {
        buffer.flip();
        Path partPath = dir.resolve(fileName + ".part" + partIndex++);
        try (FileChannel partChannel = FileChannel.open(partPath,
            StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
          partChannel.write(buffer);
          partPaths.add(partPath);
        }
        buffer.clear();
      }
    }

    return partPaths;
  }

  public void mergeFiles(List<Path> partPaths, String outputPath) throws IOException {
    Path output = Paths.get(outputPath);
    if (Files.exists(output)) Files.delete(output);

    try (FileChannel outChannel = FileChannel.open(output,
        StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {

      ByteBuffer buffer = ByteBuffer.allocate(1024);

      for (Path part : partPaths) {
        if (!Files.exists(part)) {
          throw new IOException("Part not found: " + part);
        }

        try (FileChannel partChannel = FileChannel.open(part, StandardOpenOption.READ)) {
          while (partChannel.read(buffer) > 0) {
            buffer.flip();
            outChannel.write(buffer);
            buffer.clear();
          }
        }
      }
    }
  }
}