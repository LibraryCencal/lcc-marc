package dev.librarycencal;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.util.PathConverter;
import joptsimple.util.RegexMatcher;
import org.marc4j.MarcJsonReader;
import org.marc4j.MarcJsonWriter;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcTxtWriter;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlWriter;
import org.marc4j.Mrk8StreamReader;
import org.marc4j.Mrk8StreamWriter;
import org.marc4j.converter.CharConverter;
import org.marc4j.converter.impl.AnselToUnicode;
import org.marc4j.converter.impl.UnicodeToAnsel;
import org.marc4j.marc.Record;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        OptionParser guiParser = new OptionParser();
        guiParser.accepts("gui");

        if (guiParser.parse(args).has("gui")) {
            ConverterGui.start();
        } else {
            OptionParser parser = new OptionParser();

            OptionSpec<String> inputTypeArg = parser.accepts("tI").withRequiredArg().ofType(String.class).withValuesConvertedBy(new RegexMatcher("(json|iso|mrk8)", 0)).required();
            OptionSpec<String> outputTypeArg = parser.accepts("tO").withRequiredArg().ofType(String.class).withValuesConvertedBy(new RegexMatcher("(json|iso|marc|mrk8|xml)", 0)).required();
            OptionSpec<Path> inArg = parser.accepts("in").withRequiredArg().ofType(String.class).withValuesConvertedBy(new PathConverter()).required();
            OptionSpec<Path> outArg = parser.accepts("out").withRequiredArg().ofType(String.class).withValuesConvertedBy(new PathConverter()).required();
            OptionSpec<String> inEncodingArg = parser.accepts("iO").withRequiredArg().ofType(String.class).withValuesConvertedBy(new RegexMatcher("(ansel|unicode|unimarc|iso5426|iso6937)", 0)).required();
            OptionSpec<String> outEncodingArg = parser.accepts("eO").withRequiredArg().ofType(String.class).withValuesConvertedBy(new RegexMatcher("(ansel|unicode|unimarc|iso5426|iso6937)", 0)).required();
            OptionSpec<Integer> maxItemsArg = parser.accepts("maxItems").withRequiredArg().ofType(Integer.class).defaultsTo(99999999);
            OptionSet optionSet = parser.parse(args);

            ConvertArgs convertArgs = new ConvertArgs(
                    optionSet.valueOf(inputTypeArg),
                    optionSet.valueOf(outputTypeArg),
                    optionSet.valueOf(inArg),
                    optionSet.valueOf(outArg),
                    optionSet.valueOf(inEncodingArg),
                    optionSet.valueOf(outEncodingArg),
                    optionSet.valueOf(maxItemsArg));

            convert(convertArgs);
        }
    }

    public static void convert(ConvertArgs args) {
        String typeInput = args.typeInput();
        String typeOutput = args.typeOutput();
        Path inputPath = args.inputPath();
        Path outputPath = args.outputPath();
        String encodingInput = args.encodingInput();
        String encodingOutput = args.encodingOutput();
        int maxItems = args.maxItems();

        if (typeInput.equals(typeOutput)) {
            System.err.println("Error: output type cannot be the same as input!");
        }

        if (!Files.exists(inputPath)) {
            System.err.println("Error: input file does not exist!");
            return;
        }
        try (FileInputStream in = new FileInputStream(inputPath.toFile());
             OutputStream out = new FileOutputStream(outputPath.toFile())) {

            MarcReader reader = switch (typeInput) {
                case "json" -> new MarcJsonReader(in);
                case "iso" -> new MarcStreamReader(in);
                case "mrk8" -> new Mrk8StreamReader(in);
                default -> null;
            };

            MarcWriter writer = switch (typeOutput) {
                case "json" -> new MarcJsonWriter(out);
                case "iso" -> new MarcStreamWriter(out);
                case "mrk8" -> new Mrk8StreamWriter(out);
                case "mrc" -> new MarcTxtWriter(out);
                case "xml" -> new MarcXmlWriter(out, true);
                default -> null;
            };


            CharConverter charConverter = null;
            if ("ansel".equals(encodingInput) && "unicode".equals(encodingOutput)) {
                charConverter = new AnselToUnicode();
            } else if ("unicode".equals(encodingInput) && "ansel".equals(encodingOutput)) {
                charConverter = new UnicodeToAnsel();
            }

            if (reader == null || writer == null) {
                System.err.println("Error: No reader or writer for types given!");
            } else {
                if (charConverter != null) {
                    writer.setConverter(charConverter);
                }

                int i = convert(reader, writer, maxItems);

                if (typeInput.equals("iso") && typeOutput.equals("json")) {
                    if (i > 1) {
                        Path outputTemp = outputPath.getParent().resolve(outputPath.getFileName() + "_temp");
                        try (BufferedReader rreader = Files.newBufferedReader(outputPath)) {
                            BufferedWriter wwriter = Files.newBufferedWriter(outputTemp);

                            wwriter.write("[");
                            String line;
                            int j = 0;
                            while ((line = rreader.readLine()) != null) {
                                wwriter.write(line);
                                if (j + 1 < i) {
                                    wwriter.write(",\n");
                                }
                                ++j;
                            }

                            wwriter.write("]");
                            wwriter.close();
                        } catch (IOException e) {
                            return;
                        }
                        try {
                            Files.deleteIfExists(outputPath);
                            Files.move(outputTemp, outputPath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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