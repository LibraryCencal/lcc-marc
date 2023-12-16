package dev.librarycencal;

import java.nio.file.Path;

public record ConvertArgs(String typeInput, String typeOutput, Path inputPath,
                          Path outputPath, String encodingInput, String encodingOutput, int maxItems) {
}
