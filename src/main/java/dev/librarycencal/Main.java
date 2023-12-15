package dev.librarycencal;

import joptsimple.OptionParser;
import joptsimple.util.PathConverter;
import joptsimple.util.RegexMatcher;
import org.marc4j.MarcJsonReader;
import org.marc4j.MarcJsonWriter;
import org.marc4j.MarcReader;
import org.marc4j.MarcReaderConfig;
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
        OptionParser parser = new OptionParser();

        var inputTypeArg = parser.accepts("tI").withRequiredArg().ofType(String.class).withValuesConvertedBy(new RegexMatcher("(json|iso|mrk8)", 0)).required();
        var outputTypeArg = parser.accepts("tO").withRequiredArg().ofType(String.class).withValuesConvertedBy(new RegexMatcher("(json|iso|marc|mrk8|xml)", 0)).required();
        var inArg = parser.accepts("in").withRequiredArg().ofType(String.class).withValuesConvertedBy(new PathConverter()).required();
        var outArg = parser.accepts("out").withRequiredArg().ofType(String.class).withValuesConvertedBy(new PathConverter()).required();
        var inEncodingArg = parser.accepts("iO").withRequiredArg().ofType(String.class).withValuesConvertedBy(new RegexMatcher("(ansel|unicode|unimarc|iso5426|iso6937)", 0)).required();
        var outEncodingArg = parser.accepts("eO").withRequiredArg().ofType(String.class).withValuesConvertedBy(new RegexMatcher("(ansel|unicode|unimarc|iso5426|iso6937)", 0)).required();
        var maxItemsArg = parser.accepts("maxItems").withRequiredArg().ofType(Integer.class).defaultsTo(99999999);
        var optionSet = parser.parse(args);

        String typeInput = optionSet.valueOf(inputTypeArg);
        String typeOutput = optionSet.valueOf(outputTypeArg);
        Path inputPath = optionSet.valueOf(inArg);
        Path outputPath = optionSet.valueOf(outArg);

        String encodingInput = optionSet.valueOf(inEncodingArg);
        String encodingOutput = optionSet.valueOf(outEncodingArg);

        int maxItems = optionSet.valueOf(maxItemsArg);

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