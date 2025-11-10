    package gui;

    import javax.swing.*;
    import java.awt.*;
    import java.awt.event.ActionEvent;
    import java.awt.event.ActionListener;
    import java.awt.print.*;
    import java.text.SimpleDateFormat;
    import java.util.Date;

    public class PrintBarcodeLabel {

        private JComboBox<String> printTypeSelector;
        private JTextField barcodeField;
        private JTextField barcodePrintQtyField;
        private JCheckBox includeBusinessNameCheckbox;
        private JCheckBox includeProductNameCheckbox;
        private JCheckBox includePriceCheckbox;
        private JCheckBox includePrintedDateCheckbox;

        // Sample data
        private String businessName = "My Business Store";
        private String productName = "Sample Product";
        private String price = "$29.99";

        // Paper configuration class
        class PaperConfig {

            String name;
            double width; // in points (1mm = 2.83465 points)
            double height;
            int columns;
            int rows;
            double labelWidth;
            double labelHeight;

            PaperConfig(String name, double widthMM, double heightMM, int columns, int rows) {
                this.name = name;
                this.width = mmToPoints(widthMM);
                this.height = mmToPoints(heightMM);
                this.columns = columns;
                this.rows = rows;
                this.labelWidth = this.width / columns;
                this.labelHeight = this.height / rows;
            }
        }

        private PaperConfig[] paperConfigs = {

            new PaperConfig("A4 21up 70mm x 42.4mm", 210, 297, 3, 7), // 21 labels
            new PaperConfig("A4 24up 70mm x 37mm", 210, 297, 3, 8), // 24 labels
            new PaperConfig("A4 30up 70mm x 299.7mm", 210, 297, 3, 10), // 30 labels
            new PaperConfig("A4 44up 48.5mm x 25.4mm", 210, 297, 4, 11), // 44 labels
            new PaperConfig("A4 56up 52.5mm x 21mm", 210, 297, 4, 14), // 56 labels
            new PaperConfig("A4 65up 38mm x 21mm", 210, 297, 5, 13), // 65 labels
            new PaperConfig("A4 68up 48mm x 16.6mm", 210, 297, 4, 17) // 68 labels
        };

        public PrintBarcodeLabel() {
            initializePrintingComponents();
        }

        private void initializePrintingComponents() {
            initializePaperTypes();
            initializeOtherComponents();
        }

        private void initializePaperTypes() {
            printTypeSelector = new JComboBox<>();
            printTypeSelector.addItem("Select Print Paper Type");
            for (PaperConfig config : paperConfigs) {
                printTypeSelector.addItem(config.name);
            }
        }

        private void initializeOtherComponents() {
            barcodeField = new JTextField("123456789012", 15);
            barcodePrintQtyField = new JTextField("1", 5);

            includeBusinessNameCheckbox = new JCheckBox("Include Business Name", true);
            includeProductNameCheckbox = new JCheckBox("Include Product Name", true);
            includePriceCheckbox = new JCheckBox("Include Price", true);
            includePrintedDateCheckbox = new JCheckBox("Include Printed Date", true);
        }

        private void printBarcodeLabel() {
            if (!validateInputs()) {
                return;
            }

            int totalLabels = Integer.parseInt(barcodePrintQtyField.getText());
            String selectedPaperType = (String) printTypeSelector.getSelectedItem();

            // Find the selected paper configuration
            PaperConfig selectedConfig = null;
            for (PaperConfig config : paperConfigs) {
                if (config.name.equals(selectedPaperType)) {
                    selectedConfig = config;
                    break;
                }
            }

            if (selectedConfig == null) {
                JOptionPane.showMessageDialog(null, "Invalid paper type selected");
                return;
            }

            // Create printer job
            PrinterJob printerJob = PrinterJob.getPrinterJob();

            // Create book for multiple pages
            Book book = new Book();
            PageFormat pageFormat = createPageFormat(selectedConfig);

            int labelsPerPage = selectedConfig.columns * selectedConfig.rows;
            int totalPages = (int) Math.ceil((double) totalLabels / labelsPerPage);

            // Add pages based on total labels needed
            for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
                int startLabel = pageIndex * labelsPerPage;
                int endLabel = Math.min(startLabel + labelsPerPage, totalLabels);
                book.append(new BarcodePrintable(selectedConfig, startLabel, endLabel), pageFormat);
            }

            printerJob.setPageable(book);

            try {
                printerJob.print();
                JOptionPane.showMessageDialog(null,
                        "Printed " + totalLabels + " barcode label(s) successfully!");
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(null,
                        "Printing failed: " + ex.getMessage(),
                        "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        // Inner class for printable content
        class BarcodePrintable implements Printable {

            private PaperConfig config;
            private int startLabel;
            private int endLabel;

            public BarcodePrintable(PaperConfig config, int startLabel, int endLabel) {
                this.config = config;
                this.startLabel = startLabel;
                this.endLabel = endLabel;
            }

            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
                    throws PrinterException {
                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                // Set up fonts and colors
                g2d.setColor(Color.BLACK);

                // Draw all labels for this page
                drawLabelsOnPage(g2d);

                return PAGE_EXISTS;
            }

            private void drawLabelsOnPage(Graphics2D g2d) {
                int labelsPerPage = config.columns * config.rows;
                int labelsToDraw = endLabel - startLabel;

                for (int labelIndex = 0; labelIndex < labelsToDraw; labelIndex++) {
                    int row = labelIndex / config.columns;
                    int col = labelIndex % config.columns;

                    double x = col * config.labelWidth;
                    double y = row * config.labelHeight;

                    drawSingleLabel(g2d, x, y, config.labelWidth, config.labelHeight);
                }
            }

            private void drawSingleLabel(Graphics2D g2d, double x, double y, double width, double height) {
                // Increased margins for each label - 10 points on all sides
                int margin = 8;
                int labelX = (int) x + margin;
                int labelY = (int) y + margin;
                int labelWidth = (int) width - (margin * 2); // Fixed: should be *2 for both sides
                int labelHeight = (int) height - (margin * 2); // Fixed: should be *2 for both top and bottom

                // Get data based on user selection
                String barcode = barcodeField.getText();
                String businessNameText = includeBusinessNameCheckbox.isSelected() ? businessName : "";
                String productNameText = includeProductNameCheckbox.isSelected() ? productName : "";
                String priceText = includePriceCheckbox.isSelected() ? price : "";
                String printedDate = includePrintedDateCheckbox.isSelected()
                        ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) : "";

                // Calculate starting position
                int currentY = labelY + 8;

                // Draw business name (if selected)
                if (!businessNameText.isEmpty()) {
                    Font businessFont = new Font("Arial", Font.BOLD, getFontSizeForHeight(labelHeight / 15));
                    g2d.setFont(businessFont);
                    drawCenteredString(g2d, businessNameText, labelX, currentY, labelWidth);
                    currentY += getStringHeight(g2d, businessNameText) + 2;
                }

                // Draw product name (if selected)
                if (!productNameText.isEmpty()) {
                    Font productFont = new Font("Arial", Font.BOLD, getFontSizeForHeight(labelHeight / 18));
                    g2d.setFont(productFont);
                    drawCenteredString(g2d, productNameText, labelX, currentY, labelWidth);
                    currentY += getStringHeight(g2d, productNameText) + 2;
                }

                // Draw price (if selected)
                if (!priceText.isEmpty()) {
                    Font priceFont = new Font("Arial", Font.BOLD, getFontSizeForHeight(labelHeight / 18));
                    g2d.setFont(priceFont);
                    drawCenteredString(g2d, priceText, labelX, currentY, labelWidth);
                    currentY += getStringHeight(g2d, priceText) - 2;
                }

                // Draw barcode lines - FIXED: Use consistent height calculation
                int barcodeHeight = (int) (labelHeight * 0.25); // Slightly increased but reasonable
                drawCode128Barcode(g2d, barcode, labelX, currentY, labelWidth, barcodeHeight);
                currentY += barcodeHeight + 5; // Increased spacing after barcode

                // Draw barcode number BELOW the barcode
                Font barcodeFont = new Font("Arial", Font.PLAIN, getFontSizeForHeight(labelHeight / 20));
                g2d.setFont(barcodeFont);
                drawCenteredString(g2d, barcode, labelX, currentY, labelWidth);
                currentY += getStringHeight(g2d, barcode) + 5;

                // Draw printed date (if selected)
                if (!printedDate.isEmpty()) {
                    Font dateFont = new Font("Arial", Font.PLAIN, getFontSizeForHeight(labelHeight / 22));
                    g2d.setFont(dateFont);
                    drawCenteredString(g2d, printedDate, labelX, currentY, labelWidth);
                }
            }

            private void drawCode128Barcode(Graphics2D g2d, String barcode, int x, int y, int width, int height) {
                int barcodeHeight = height;

                // 🔹 Reduce barcode width dynamically based on label width
                double widthRatio = width > 150 ? 0.85 : width > 100 ? 0.75 : 0.65;
                int barcodeWidth = (int) (width * widthRatio);
                int barcodeX = x + (width - barcodeWidth) / 2; // Centered

                // Generate barcode pattern
                String code128Pattern = generateCode128Pattern(barcode);

                // 🔹 Ensure minimum module width = 1, max = 2 px
                int moduleWidth = Math.max(1, Math.min(2, barcodeWidth / code128Pattern.length()));

                // 🔹 Ensure barcode doesn’t overflow label width
                int maxBars = barcodeWidth / moduleWidth;
                if (code128Pattern.length() > maxBars) {
                    code128Pattern = code128Pattern.substring(0, maxBars);
                }

                // Draw bars
                g2d.setColor(Color.BLACK);
                for (int i = 0; i < code128Pattern.length(); i++) {
                    if (code128Pattern.charAt(i) == '1') {
                        int barX = barcodeX + (i * moduleWidth);
                        g2d.fillRect(barX, y, moduleWidth, barcodeHeight);
                    }
                }
            }

            private void drawCenteredString(Graphics2D g2d, String text, int x, int y, int width) {
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(text);
                int textX = x + (width - textWidth) / 2;
                g2d.drawString(text, textX, y);
            }

            private int getStringHeight(Graphics2D g2d, String text) {
                FontMetrics fm = g2d.getFontMetrics();
                return fm.getHeight();
            }

            private int getFontSizeForHeight(double desiredHeight) {
                return Math.max(6, (int) (desiredHeight * 0.7)); // Reduced multiplier
            }

            private String generateCode128Pattern(String data) {
                // Code 128 character encoding (simplified version)
                String pattern = "11010010000"; // Start code B

                // Encode each character
                for (char c : data.toCharArray()) {
                    if (c >= '0' && c <= '9') {
                        pattern += getCode128CharPattern(c - '0' + 16);
                    } else if (c >= 'A' && c <= 'Z') {
                        pattern += getCode128CharPattern(c - 'A' + 17);
                    } else if (c >= 'a' && c <= 'z') {
                        pattern += getCode128CharPattern(c - 'a' + 49);
                    } else {
                        pattern += getCode128CharPattern(0); // Space
                    }
                }

                // Calculate checksum
                int checksum = 104; // Start B code value
                for (int i = 0; i < data.length(); i++) {
                    char c = data.charAt(i);
                    if (c >= '0' && c <= '9') {
                        checksum += (c - '0' + 16) * (i + 1);
                    } else if (c >= 'A' && c <= 'Z') {
                        checksum += (c - 'A' + 17) * (i + 1);
                    } else if (c >= 'a' && c <= 'z') {
                        checksum += (c - 'a' + 49) * (i + 1);
                    } else {
                        checksum += 0 * (i + 1);
                    }
                }
                checksum = checksum % 103;
                pattern += getCode128CharPattern(checksum);

                // Stop code
                pattern += "1100011101011";

                return pattern;
            }

            private String getCode128CharPattern(int value) {
                String[] patterns = {
                    "11011001100", "11001101100", "11001100110", "10010011000", "10010001100",
                    "10001001100", "10011001000", "10011000100", "10001100100", "11001001000",
                    "11001000100", "11000100100", "10110011100", "10011011100", "10011001110",
                    "10111001100", "10011101100", "10011100110", "11001110010", "11001011100",
                    "11001001110", "11011100100", "11001110100", "11101101110", "11101001100",
                    "11100101100", "11100100110", "11101100100", "11100110100", "11100110010",
                    "11011011000", "11011000110", "11000110110", "10100011000", "10001011000",
                    "10001000110", "10110001000", "10001101000", "10001100010", "11010001000",
                    "11000101000", "11000100010", "10110111000", "10110001110", "10001101110",
                    "10111011000", "10111000110", "10001110110", "11101110110", "11010001110",
                    "11000101110", "11011101000", "11011100010", "11011101110", "11101011000",
                    "11101000110", "11100010110", "11101101000", "11101100010", "11100011010",
                    "11101111010", "11001000010", "11110001010", "10100110000", "10100001100",
                    "10010110000", "10010000110", "10000101100", "10000100110", "10110010000",
                    "10110000100", "10011010000", "10011000010", "10000110100", "10000110010",
                    "11000010010", "11001010000", "11110111010", "11000010100", "10001111010",
                    "10100111100", "10010111100", "10010011110", "10111100100", "10011110100",
                    "10011110010", "11110100100", "11110010100", "11110010010", "11011011110",
                    "11011110110", "11110110110", "10101111000", "10100011110", "10001011110",
                    "10111101000", "10111100010", "11110101000", "11110100010", "10111011110",
                    "10111101110", "11101011110", "11110101110", "11010000100", "11010010000",
                    "11010011100", "11000111010"
                };

                if (value >= 0 && value < patterns.length) {
                    return patterns[value];
                }
                return patterns[0];
            }
        }

        private PageFormat createPageFormat(PaperConfig config) {
            PageFormat format = new PageFormat();
            Paper paper = new Paper();

            paper.setSize(config.width, config.height);
            paper.setImageableArea(0, 0, config.width, config.height);

            format.setPaper(paper);
            format.setOrientation(PageFormat.PORTRAIT);
            return format;
        }

        private double mmToPoints(double mm) {
            return mm * 2.83465;
        }

        private boolean validateInputs() {
            if (printTypeSelector.getSelectedIndex() == 0) {
                JOptionPane.showMessageDialog(null,
                        "Please select a paper type",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }

            if (barcodeField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "Please enter a barcode",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }

            try {
                int qty = Integer.parseInt(barcodePrintQtyField.getText());
                if (qty <= 0) {
                    JOptionPane.showMessageDialog(null,
                            "Print quantity must be greater than 0",
                            "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null,
                        "Please enter a valid print quantity",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }

            return true;
        }

        public JPanel createPrintingPanel() {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;

            // Row 0: Barcode
            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(new JLabel("Barcode:"), gbc);
            gbc.gridx = 1;
            panel.add(barcodeField, gbc);

            // Row 1: Print Quantity
            gbc.gridx = 0;
            gbc.gridy = 1;
            panel.add(new JLabel("Print Quantity:"), gbc);
            gbc.gridx = 1;
            panel.add(barcodePrintQtyField, gbc);

            // Row 2-5: Checkboxes for selection
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 2;
            panel.add(includeBusinessNameCheckbox, gbc);

            gbc.gridy = 3;
            panel.add(includeProductNameCheckbox, gbc);

            gbc.gridy = 4;
            panel.add(includePriceCheckbox, gbc);

            gbc.gridy = 5;
            panel.add(includePrintedDateCheckbox, gbc);

            // Row 6: Paper Type
            gbc.gridx = 0;
            gbc.gridy = 6;
            gbc.gridwidth = 1;
            panel.add(new JLabel("Paper Type:"), gbc);
            gbc.gridx = 1;
            panel.add(printTypeSelector, gbc);

            // Row 7: Print Button
            gbc.gridx = 0;
            gbc.gridy = 7;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.CENTER;
            JButton printButton = new JButton("Print Barcode");
            printButton.addActionListener(e -> printBarcodeLabel());
            panel.add(printButton, gbc);

            return panel;
        }

        // Getters and setters
        public void setBusinessName(String businessName) {
            this.businessName = businessName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public static void main(String[] args) {
            SwingUtilities.invokeLater(() -> {
                JFrame frame = new JFrame("Barcode Printing System");
                PrintBarcodeLabel printingSystem = new PrintBarcodeLabel();

                printingSystem.setBusinessName("Avinam PharmaX (PVT) LTD");
                printingSystem.setProductName("Anchor Full Cream Milk Powder 400g");
                printingSystem.setPrice("Rs.1250.00");

                frame.add(printingSystem.createPrintingPanel());
                frame.pack();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            });
        }
    }
