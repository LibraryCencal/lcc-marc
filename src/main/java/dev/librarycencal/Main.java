package dev.librarycencal;

import joptsimple.OptionParser;
import joptsimple.util.PathConverter;
import joptsimple.util.RegexMatcher;
import org.marc4j.MarcJsonReader;
import org.marc4j.MarcJsonWriter;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcTxtWriter;
import org.marc4j.MarcWriter;
import org.marc4j.marc.Record;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

public class Main {
    public static void main(String[] args) {
        var parser = new OptionParser();

        var inputTypeArg = parser.accepts("tI").withRequiredArg().ofType(String.class).withValuesConvertedBy(new RegexMatcher("(json|iso)", 0)).required();
        var outputTypeArg = parser.accepts("tO").withRequiredArg().ofType(String.class).withValuesConvertedBy(new RegexMatcher("(json|iso|marc)", 0)).required();
        var inArg = parser.accepts("in").withRequiredArg().ofType(String.class).withValuesConvertedBy(new PathConverter()).required();
        var outArg = parser.accepts("out").withRequiredArg().ofType(String.class).withValuesConvertedBy(new PathConverter()).required();
        var maxItemsArg = parser.accepts("maxItems").withRequiredArg().ofType(Integer.class).defaultsTo(99999999);

        var optionSet = parser.parse(args);

        var typeInput = optionSet.valueOf(inputTypeArg);
        var typeOutput = optionSet.valueOf(outputTypeArg);
        var inputPath = optionSet.valueOf(inArg);
        var outputPath = optionSet.valueOf(outArg);
        var maxItems = optionSet.valueOf(maxItemsArg);

        if (typeInput.equals(typeOutput)) {
            System.err.println("Error: output type cannot be the same as input!");
        }

        if (!Files.exists(inputPath)) {
            System.err.println("Error: input file does not exist!");
            return;
        }

        try (FileInputStream in = new FileInputStream(inputPath.toFile());
             FileOutputStream out = new FileOutputStream(outputPath.toFile())) {
            if (typeInput.equals("json") && typeOutput.equals("iso")) {
                convert(new MarcJsonReader(in), new MarcStreamWriter(out), maxItems);
            } else if (typeInput.equals("json") && typeOutput.equals("marc")) {
                convert(new MarcJsonReader(in), new MarcTxtWriter(out), maxItems);
            } else if (typeInput.equals("iso") && typeOutput.equals("marc")) {
                convert(new MarcStreamReader(in), new MarcTxtWriter(out), maxItems);
            } else if (typeInput.equals("iso") && typeOutput.equals("json")) {
                int i = convert(new MarcStreamReader(in), new MarcJsonWriter(out), maxItems);

                if (i > 1) {
                    try (BufferedReader reader = Files.newBufferedReader(outputPath)) {
                        BufferedWriter writer = Files.newBufferedWriter(outputPath.getParent().resolve(outputPath.getFileName() + "_temp"));

                        writer.write("[");
                        String line;
                        int j = 0;
                        while ((line = reader.readLine()) != null) {
                            writer.write(line);
                            if (j + 1 < i) {
                                writer.write(",\n");
                            }
                            ++j;
                        }

                        writer.write("]");
                        writer.close();
                    } catch (IOException e) {
                        return;
                    }
                    try {
                        Files.deleteIfExists(outputPath);
                        Files.move(outputPath.getParent().resolve(outputPath.getFileName() + "_temp"),
                                outputPath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int convert(MarcReader reader, MarcWriter writer, int maxItems) {
        int i = 0;
        while (reader.hasNext()) {
            if (i >= maxItems) {
                break;
            }
            Record record = reader.next();
            writer.write(record);

            i++;
        }
        return i;
    }
}