package dev.librarycencal;

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
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Main {
    public static int maxItems = 999999999;

    public static void main(String[] args) {
        List<String> validInputTypes = Arrays.asList("json", "iso");
        List<String> validOutputTypes = Arrays.asList("json", "marc", "iso");
        String typeInput = null;
        String typeOutput = null;
        Path inputPath = null;
        Path outputPath = null;
        int max = 99999999;

        for (int i = 0; i < args.length; ++i) {
            if ("-tI".equals(args[i]) && i + 1 < args.length) {
                typeInput = args[++i].toLowerCase(Locale.ROOT);

                if (!validInputTypes.contains(typeInput)) {
                    System.err.println("Error: -tI" + typeInput + " is not valid value!");
                    return;
                }
            } else if ("-tO".equals(args[i]) && i + 1 < args.length) {
                typeOutput = args[++i].toLowerCase(Locale.ROOT);

                if (!validOutputTypes.contains(typeOutput)) {
                    System.err.println("Error: -tO" + typeOutput + " is not valid value!");
                    return;
                }
            } else if ("-in".equals(args[i]) && i + 1 < args.length) {
                inputPath = Path.of(args[++i]);
            } else if ("-out".equals(args[i]) && i + 1 < args.length) {
                outputPath = Path.of(args[++i]);
            } else if ("-maxItems".equals(args[i]) && i + 1 < args.length) {
                try {
                    maxItems = Integer.parseInt(args[++i]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }

        if (typeInput == null) {
            System.err.println("Error: argument -tI is missing!");
            return;
        }

        if (typeOutput == null) {
            System.err.println("Error: argument -tO is missing!");
            return;
        }

        if (typeInput.equals(typeOutput)) {
            System.err.println("Error: output type cannot be the same as input!");
        }

        if (inputPath == null) {
            System.err.println("Error: argument -in is missing!");
            return;
        }

        if (outputPath == null) {
            System.err.println("Error: argument -out is missing!");
            return;
        }

        if (!Files.exists(inputPath)) {
            System.err.println("Error: input file does not exist!");
            return;
        }

        try (FileInputStream in = new FileInputStream(inputPath.toFile());
             FileOutputStream out = new FileOutputStream(outputPath.toFile())) {
            if (typeInput.equals("json") && typeOutput.equals("iso")) {
                convert(new MarcJsonReader(in), new MarcStreamWriter(out));
            } else if (typeInput.equals("json") && typeOutput.equals("marc")) {
                convert(new MarcJsonReader(in), new MarcTxtWriter(out));
            } else if (typeInput.equals("iso") && typeOutput.equals("marc")) {
                convert(new MarcStreamReader(in), new MarcTxtWriter(out));
            } else if (typeInput.equals("iso") && typeOutput.equals("json")) {
                int i = convert(new MarcStreamReader(in), new MarcJsonWriter(out));

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

    public static int convert(MarcReader reader, MarcWriter writer) {
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