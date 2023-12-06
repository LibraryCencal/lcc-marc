package dev.librarycencal;

import org.marc4j.MarcJsonReader;
import org.marc4j.MarcJsonWriter;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcTxtWriter;
import org.marc4j.marc.Record;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        // Json -> ISO
        // Json -> Marc
        // Marc -> Json
        // Marc -> ISO
        // ISO -> Marc
        // ISO -> Json

        // -tI json -tO marc -in ... -out ...

        List<String> validTypes = Arrays.asList("json", "marc", "iso");
        String typeInput = null;
        String typeOutput = null;
        Path inputPath = null;
        Path outputPath = null;
        int max = 99999999;

        for (int i = 0; i < args.length; ++i) {
            if ("-tI".equals(args[i]) && i + 1 < args.length) {
                typeInput = args[++i].toLowerCase(Locale.ROOT);

                if (!validTypes.contains(typeInput)) {
                    System.err.println("Error: -tI" + typeInput + " is not valid value!");
                    return;
                }
            } else if ("-tO".equals(args[i]) && i + 1 < args.length) {
                typeOutput = args[++i].toLowerCase(Locale.ROOT);

                if (!validTypes.contains(typeOutput)) {
                    System.err.println("Error: -tO" + typeOutput + " is not valid value!");
                    return;
                }
            } else if ("-in".equals(args[i]) && i + 1 < args.length) {
                inputPath = Path.of(args[++i]);
            } else if ("-out".equals(args[i]) && i + 1 < args.length) {
                outputPath = Path.of(args[++i]);
            } else if ("-maxItems".equals(args[i]) && i + 1 < args.length) {
                try {
                    max = Integer.parseInt(args[++i]);
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

        if (typeInput.equals("json") && typeOutput.equals("iso")) {
            try (FileInputStream in = new FileInputStream(inputPath.toFile());
                 FileOutputStream out = new FileOutputStream(outputPath.toFile())) {
                MarcJsonReader reader = new MarcJsonReader(in);
                MarcStreamWriter writer = new MarcStreamWriter(out);

                int i = 0;
                while (reader.hasNext()) {
                    if (i >= max) {
                        break;
                    }

                    Record record = reader.next();
                    writer.write(record);
                    i++;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (typeInput.equals("json") && typeOutput.equals("marc")) {
            try (FileInputStream in = new FileInputStream(inputPath.toFile());
                 FileOutputStream out = new FileOutputStream(outputPath.toFile())) {
                MarcJsonReader reader = new MarcJsonReader(in);
                MarcTxtWriter writer = new MarcTxtWriter(out);

                int i = 0;
                while (reader.hasNext()) {
                    if (i >= max) {
                        break;
                    }

                    Record record = reader.next();
                    writer.write(record);

                    i++;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (typeInput.equals("marc") && typeOutput.equals("json")) {
            try (FileInputStream in = new FileInputStream(inputPath.toFile());
                 FileOutputStream out = new FileOutputStream(outputPath.toFile())) {
                MarcStreamReader reader = new MarcStreamReader(in);
                MarcJsonWriter writer = new MarcJsonWriter(out);

                int i = 0;
                while (reader.hasNext()) {
                    if (i >= max) {
                        break;
                    }

                    Record record = reader.next();
                    writer.write(record);

                    i++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (typeInput.equals("marc") && typeOutput.equals("iso")) {
            try (FileInputStream in = new FileInputStream(inputPath.toFile());
                 FileOutputStream out = new FileOutputStream(outputPath.toFile())) {
                MarcStreamReader reader = new MarcStreamReader(in);
                MarcStreamWriter writer = new MarcStreamWriter(out);

                int i = 0;
                while (reader.hasNext()) {
                    if (i >= max) {
                        break;
                    }

                    Record record = reader.next();
                    writer.write(record);

                    i++;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (typeInput.equals("iso") && typeOutput.equals("json")) {
            int i = 0;
            try (FileInputStream in = new FileInputStream(inputPath.toFile());
                 FileOutputStream out = new FileOutputStream(outputPath.toFile())) {
                MarcReader reader = new MarcStreamReader(in);
                MarcJsonWriter writer = new MarcJsonWriter(out);


                while (reader.hasNext()) {
                    if (i >= max) {
                        break;
                    }
                    Record record = reader.next();
                    writer.write(record);

                    i++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

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
        } else if (typeInput.equals("iso") && typeOutput.equals("marc")) {
            try (FileInputStream in = new FileInputStream(inputPath.toFile());
                 FileOutputStream out = new FileOutputStream(outputPath.toFile())) {
                MarcReader reader = new MarcStreamReader(in);
                MarcTxtWriter writer = new MarcTxtWriter(out);

                int i = 0;
                while (reader.hasNext()) {
                    if (i >= max) {
                        break;
                    }

                    Record record = reader.next();
                    writer.write(record);

                    i++;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Error: -tI or -tO is not supported");
        }
    }
}