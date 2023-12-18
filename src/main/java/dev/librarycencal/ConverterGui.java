package dev.librarycencal;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConverterGui {
    public static void start() {
        try {
            FlatDarkLaf.setup();
        } catch (Exception ignored) {
        }

        JFrame frame = new JFrame("LCC Marc");
        frame.setResizable(true);
        frame.setMinimumSize(new Dimension(500, 0));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JLabel inTypeLabel = new JLabel("Input Type: ");
        JComboBox<String> inTypeDropdown = new JComboBox<>(new String[] { "JSON", "ISO", "Mrk8" });

        JLabel outTypeLabel = new JLabel("Output Type: ");
        JComboBox<String> outTypeDropdown = new JComboBox<>(new String[] { "JSON", "ISO", "Marc", "Mrk8" });

        JLabel inPathLabel = new JLabel("Input Path: ");
        JTextField inPathField = new JTextField();
        JButton inPathBtn = new JButton();
        inPathBtn.setIcon(UIManager.getIcon("FileView.directoryIcon"));
        inPathBtn.addActionListener(event -> {
            JFileChooser chooser = new JFileChooser();
            chooser.addChoosableFileFilter(new FileNameExtensionFilter("json files (*.json)", "json"));
            chooser.addChoosableFileFilter(new FileNameExtensionFilter("marc files (*.mrc)", "mrc"));
            chooser.addChoosableFileFilter(new FileNameExtensionFilter("mrk8 files (*.mrk)", "mrk"));
            chooser.addChoosableFileFilter(new FileNameExtensionFilter("iso files (*.iso)", "iso"));
            int r = chooser.showOpenDialog(frame);
            if (r == JFileChooser.APPROVE_OPTION) {
                inPathField.setText(chooser.getSelectedFile().toPath().toAbsolutePath().toString());
            }
        });

        JLabel outPathLabel = new JLabel("Output Path: ");
        JTextField outPathField = new JTextField();
        JButton outPathBtn = new JButton();
        outPathBtn.setIcon(UIManager.getIcon("FileView.directoryIcon"));
        outPathBtn.addActionListener(event -> {
            JFileChooser chooser = new JFileChooser();
            int r = chooser.showSaveDialog(frame);
            if (r == JFileChooser.APPROVE_OPTION) {
                outPathField.setText(chooser.getSelectedFile().toPath().toAbsolutePath().toString());
            }
        });

        JLabel encodeInputLabel= new JLabel("Input Encoding: ");
        JComboBox<String> encodeInputCombox = new JComboBox<>(new String[]{ "", "Ansel", "Unimarc", "Unicode", "Iso6937", "Iso5426" });
        JLabel encodeOutputLabel= new JLabel("Output Encoding: ");
        JComboBox<String> encodeOutputCombox = new JComboBox<>(new String[]{ "", "Ansel", "Unimarc", "Unicode", "Iso6937", "Iso5426" });

        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        frame.setContentPane(panel);

        JButton convertButton = new JButton();
        convertButton.setText("Convert");
        convertButton.addActionListener(event -> {
            String inType = (String) inTypeDropdown.getSelectedItem();
            String outType = (String) outTypeDropdown.getSelectedItem();

            if (inPathField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Input path cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (inType != null && inType.equals(outType)) {
                JOptionPane.showMessageDialog(frame, "Input and output cannot be of the same format!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Path inPath = Paths.get(inPathField.getText());

            if (!Files.exists(inPath)) {
                JOptionPane.showMessageDialog(frame, "Input path does not exist!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Path outPath;
            if (outPathField.getText().isEmpty()) {
                outPath = Paths.get(inPath.getParent().resolve(inPath.getFileName().toString().substring(0, inPath.getFileName().toString().lastIndexOf("."))).toString());
            } else {
                outPath = Paths.get(outPathField.getText());
            }

            String encodeInput = (String) encodeInputCombox.getSelectedItem();
            if (encodeInput.trim().length() == 0) {
                encodeInput = "null";
            }

            String encodeOutput = (String) encodeOutputCombox.getSelectedItem();
            if (encodeOutput.trim().length() == 0) {
                encodeOutput = "null";
            }

            ConvertArgs convertArgs = new ConvertArgs(inType.toLowerCase(), outType.toLowerCase(), inPath, outPath, encodeInput, encodeOutput, 9999999);
            Main.convert(convertArgs);

            JOptionPane.showMessageDialog(frame, "Conversion was a success!", "Successful!", JOptionPane.INFORMATION_MESSAGE);
        });

        GroupLayout layout = new GroupLayout(frame.getContentPane());
        frame.getContentPane().setLayout(layout);

        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(
                                layout.createSequentialGroup()
                                        .addGroup(
                                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(inTypeLabel)
                                                        .addComponent(inTypeDropdown)
                                        )
                                        .addGap(15)
                                        .addGroup(
                                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(outTypeLabel)
                                                        .addComponent(outTypeDropdown)
                                        )
                                        .addGap(15)
                                        .addGroup(
                                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(inPathLabel)
                                                        .addComponent(inPathField)
                                                        .addComponent(inPathBtn)
                                        )
                                        .addGap(15)
                                        .addGroup(
                                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(outPathLabel)
                                                        .addComponent(outPathField)
                                                        .addComponent(outPathBtn)
                                        )
                                        .addGap(15)
                                        .addGroup(
                                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(encodeInputLabel)
                                                        .addComponent(encodeInputCombox)
                                        )
                                        .addGap(15)
                                        .addGroup(
                                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(encodeOutputLabel)
                                                        .addComponent(encodeOutputCombox)
                                        )
                                        .addGap(15)
                                        .addComponent(convertButton)
                        )
        );

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(
                                layout.createSequentialGroup()
                                        .addGroup(
                                                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addGroup(
                                                                layout.createSequentialGroup()
                                                                        .addGroup(
                                                                                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(inTypeLabel)
                                                                        )
                                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                                        .addGroup(
                                                                                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(inTypeDropdown)
                                                                        )
                                                        )
                                                        .addGroup(
                                                                layout.createSequentialGroup()
                                                                        .addGroup(
                                                                                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(outTypeLabel)
                                                                        )
                                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                                        .addGroup(
                                                                                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(outTypeDropdown)
                                                                        )
                                                        )
                                                        .addGroup(
                                                                layout.createSequentialGroup()
                                                                        .addGroup(
                                                                                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(inPathLabel)
                                                                        )
                                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                                        .addGroup(
                                                                                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(inPathField)
                                                                        )
                                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                                        .addGroup(
                                                                                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(inPathBtn)
                                                                        )
                                                        )
                                                        .addGroup(
                                                                layout.createSequentialGroup()
                                                                        .addGroup(
                                                                                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(outPathLabel)
                                                                        )
                                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                                        .addGroup(
                                                                                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(outPathField)
                                                                        )
                                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                                        .addGroup(
                                                                                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(outPathBtn)
                                                                        )
                                                        )
                                                        .addGroup(
                                                                layout.createSequentialGroup()
                                                                        .addGroup(
                                                                                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(encodeInputLabel)
                                                                        )
                                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                                        .addGroup(
                                                                                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(encodeInputCombox)
                                                                        )
                                                        )
                                                        .addGroup(
                                                                layout.createSequentialGroup()
                                                                        .addGroup(
                                                                                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(encodeOutputLabel)
                                                                        )
                                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                                        .addGroup(
                                                                                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(encodeOutputCombox)
                                                                        )
                                                        )
                                                        .addComponent(convertButton)
                                        )
                        )
        );

        frame.pack();
        frame.setLocationRelativeTo(null);

        frame.setVisible(true);
    }
}
