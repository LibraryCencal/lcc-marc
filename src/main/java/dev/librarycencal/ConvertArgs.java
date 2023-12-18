package dev.librarycencal;

import java.nio.file.Path;

public final class ConvertArgs {
    private final String typeInput;
    private final String typeOutput;
    private final Path inputPath;
    private final Path outputPath;
    private final String encodingInput;
    private final String encodingOutput;
    private final int maxItems;

    public ConvertArgs(String typeInput, String typeOutput, Path inputPath,
                       Path outputPath, String encodingInput, String encodingOutput, int maxItems) {
        this.typeInput = typeInput;
        this.typeOutput = typeOutput;
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        this.encodingInput = encodingInput;
        this.encodingOutput = encodingOutput;
        this.maxItems = maxItems;
    }

    public String typeInput() {
        return this.typeInput;
    }

    public String typeOutput() {
        return this.typeOutput;
    }

    public Path inputPath() {
        return this.inputPath;
    }

    public Path outputPath() {
        return this.outputPath;
    }

    public String encodingInput() {
        return this.encodingInput;
    }

    public String encodingOutput() {
        return this.encodingOutput;
    }

    public int maxItems() {
        return this.maxItems;
    }
}
