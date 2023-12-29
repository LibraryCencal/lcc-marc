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
import org.marc4j.converter.impl.Iso5426ToUnicode;
import org.marc4j.converter.impl.Iso6937ToUnicode;
import org.marc4j.converter.impl.UnicodeToAnsel;
import org.marc4j.converter.impl.UnicodeToIso5426;
import org.marc4j.converter.impl.UnicodeToIso6937;
import org.marc4j.converter.impl.UnicodeToUnimarc;
import org.marc4j.converter.impl.UnimarcToUnicode;
import org.marc4j.marc.Record;
import org.marc4j.util.MarcXmlDriver;

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
        boolean gui = false;
        for (int i = 0; i < args.length; ++i) {
            if ("--gui".equals(args[i]) || "-gui".equals(args[i])) {
                gui = true;
                break;
            }
        }

        if (gui) {
            ConverterGui.start();
        } else {
            OptionParser parser = new OptionParser();

            OptionSpec<String> inputTypeArg = parser.accepts("tI").withRequiredArg().ofType(String.class).withValuesConvertedBy(new RegexMatcher("(json|iso|mrk8)", 0)).required();
            OptionSpec<String> outputTypeArg = parser.accepts("tO").withRequiredArg().ofType(String.class).withValuesConvertedBy(new RegexMatcher("(json|iso|marc|mrk8|xml)", 0)).required();
            OptionSpec<Path> inArg = parser.accepts("in").withRequiredArg().ofType(String.class).withValuesConvertedBy(new PathConverter()).required();
            OptionSpec<Path> outArg = parser.accepts("out").withRequiredArg().ofType(String.class).withValuesConvertedBy(new PathConverter()).required();
            OptionSpec<String> inEncodingArg = parser.accepts("eI").withRequiredArg().ofType(String.class).withValuesConvertedBy(new RegexMatcher("(ansel|unicode|unimarc|iso5426|iso6937|null)", 0)).defaultsTo("null").required();
            OptionSpec<String> outEncodingArg = parser.accepts("eO").withRequiredArg().ofType(String.class).withValuesConvertedBy(new RegexMatcher("(ansel|unicode|unimarc|iso5426|iso6937|null)", 0)).defaultsTo("null").required();
            OptionSpec<Integer> maxItemsArg = parser.accepts("maxItems").withRequiredArg().ofType(Integer.class).defaultsTo(99999999);
            parser.accepts("normalize");
            OptionSet optionSet = parser.parse(args);

            ConvertArgs convertArgs = new ConvertArgs(
                    optionSet.valueOf(inputTypeArg),
                    optionSet.valueOf(outputTypeArg),
                    optionSet.valueOf(inArg),
                    optionSet.valueOf(outArg),
                    optionSet.valueOf(inEncodingArg),
                    optionSet.valueOf(outEncodingArg),
                    optionSet.valueOf(maxItemsArg),
                    optionSet.has("normalize"));

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

            MarcReader reader = null;
            switch (typeInput) {
                case "json": {
                    reader = new MarcJsonReader(in);
                    break;
                }
                case "iso": {
                    reader = new MarcStreamReader(in);
                    break;
                }
                case "mrk8": {
                    reader = new Mrk8StreamReader(in);
                    break;
                }
            };

            MarcWriter writer = null;
            switch (typeOutput) {
                case "json": {
                    writer = new MarcJsonWriter(out);
                    break;
                }
                case "iso": {
                    writer = new MarcStreamWriter(out);
                    break;
                }
                case "mrk8": {
                    writer = new Mrk8StreamWriter(out);
                    break;
                }
                case "mrc": {
                    writer = new MarcTxtWriter(out);
                    break;
                }
                case "xml": {
                    writer = new MarcXmlWriter(out, "UTF8");
                    ((MarcXmlWriter) writer).setIndent(true);
                    if (args.normalize()) {
                        ((MarcXmlWriter) writer).setUnicodeNormalization(true);
                    }
                    break;
                }
            };

            CharConverter charConverter = null;
            if ("ansel".equals(encodingInput) && "unicode".equals(encodingOutput)) {
                charConverter = new AnselToUnicode();
            } else if ("unicode".equals(encodingInput) && "ansel".equals(encodingOutput)) {
                charConverter = new UnicodeToAnsel();
            } else if ("unicode".equals(encodingInput) && "unimarc".equals(encodingOutput)) {
                charConverter = new UnicodeToUnimarc();
            } else if ("unicode".equals(encodingInput) && "iso5426".equals(encodingOutput)) {
                charConverter = new UnicodeToIso5426();
            } else if ("unicode".equals(encodingInput) && "iso6937".equals(encodingOutput)) {
                charConverter = new UnicodeToIso6937();
            } else if ("unimarc".equals(encodingInput) && "unicode".equals(encodingOutput)) {
                charConverter = new UnimarcToUnicode();
            } else if ("iso5426".equals(encodingInput) && "unicode".equals(encodingOutput)) {
                charConverter = new Iso5426ToUnicode();
            } else if ("iso6937".equals(encodingInput) && "unicode".equals(encodingOutput)) {
                charConverter = new Iso6937ToUnicode();
            }

            if (reader == null || writer == null) {
                System.err.println("Error: No reader or writer for types given!");
            } else {
                if (charConverter != null) {
                    writer.setConverter(charConverter);
                }

                int i = convert(reader, writer, maxItems);
                writer.close();

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