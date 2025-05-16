package org.example;

import javax.swing.*;
import java.awt.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class Main {
    private static JFrame mainFrame;
    private static JTable festivalTable;
    private static JButton editButton;
    private static JButton addButton;
    private static JButton deleteButton;
    private static JButton addReziserButton;
    private static JButton addZaposleniButton;

    private static final String[] columns = {"Naslov", "Datum", "Trajanje(min)", "Opis", "Režiser", "Zaposleni"};

    public static void main(String[] args) {
        mainFrame = new JFrame("Pregled");
        mainFrame.setSize(700, 400);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout());

        festivalTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(festivalTable);

        refreshFestivalTable();

        editButton = new JButton("Urejanje predstave");
        editButton.addActionListener(e -> {
            int selectedRow = festivalTable.getSelectedRow();
            if (selectedRow != -1) {
                String naslov = (String) festivalTable.getValueAt(selectedRow, 0);
                String datum = (String) festivalTable.getValueAt(selectedRow, 1);
                String trajanje = String.valueOf(festivalTable.getValueAt(selectedRow, 2));
                String opis = (String) festivalTable.getValueAt(selectedRow, 3);
                String reziser = (String) festivalTable.getValueAt(selectedRow, 4);
                String zaposleni = (String) festivalTable.getValueAt(selectedRow, 5);
                openEditWindow(naslov, datum, trajanje, opis, reziser, zaposleni);
            } else {
                JOptionPane.showMessageDialog(mainFrame, "Izberite predstavo za urejanje.");
            }
        });

        addButton = new JButton("Dodaj predstavo");
        addButton.addActionListener(e -> openAddWindow());

        deleteButton = new JButton("Uspešno izbrisano");
        deleteButton.addActionListener(e -> {
            int selectedRow = festivalTable.getSelectedRow();
            if (selectedRow != -1) {
                String naslov = (String) festivalTable.getValueAt(selectedRow, 0);
                int confirmation = JOptionPane.showConfirmDialog(mainFrame,
                        "Želite izbrisati \"" + naslov + "\"?",
                        "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirmation == JOptionPane.YES_OPTION) {
                    callDeleteFunction(naslov);
                    refreshFestivalTable();
                }
            } else {
                JOptionPane.showMessageDialog(mainFrame, "Izberite predstavo.");
            }
        });

        addReziserButton = new JButton("Dodaj režiserja");
        addReziserButton.addActionListener(e -> openReziserWindow());

        addZaposleniButton = new JButton("Dodaj zaposlenega");
        addZaposleniButton.addActionListener(e -> openZaposleniWindow());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(editButton);
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(addReziserButton);
        buttonPanel.add(addZaposleniButton);

        mainFrame.add(scrollPane, BorderLayout.CENTER);
        mainFrame.add(buttonPanel, BorderLayout.SOUTH);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

    private static void refreshFestivalTable() {
        List<String[]> data = Database.fetchPredstave();
        Object[][] tableData = new Object[data.size()][columns.length];
        for (int i = 0; i < data.size(); i++) {
            for (int j = 0; j < columns.length; j++) {
                tableData[i][j] = data.get(i)[j];
            }
        }

        DefaultTableModel model = new DefaultTableModel(tableData, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        festivalTable.setModel(model);
    }

    private static void openEditWindow(String naslov, String datum, String trajanje, String opis, String reziser, String zaposleni) {
        JFrame editFrame = new JFrame("Sprememba podatkov");
        editFrame.setSize(500, 400);
        editFrame.setLayout(new GridLayout(7, 2, 5, 5));

        JTextField titleField = new JTextField(naslov);
        JTextField dateField = new JTextField(datum);
        JTextField lengthField = new JTextField(trajanje);
        JTextField descriptionField = new JTextField(opis);

        List<String> reziserList = Database.getAllReziserNames();
        JComboBox<String> reziserCombo = new JComboBox<>(reziserList.toArray(new String[0]));
        reziserCombo.setSelectedItem(reziser);

        List<String> zaposleniList = Database.getAllZaposleniNames();
        JComboBox<String> zaposleniCombo = new JComboBox<>(zaposleniList.toArray(new String[0]));
        zaposleniCombo.setSelectedItem(zaposleni);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            String updatedNaslov = titleField.getText().trim();
            String updatedDatum = dateField.getText().trim();
            String updatedTrajanje = lengthField.getText().trim();
            String updatedOpis = descriptionField.getText().trim();
            String updatedReziser = (String) reziserCombo.getSelectedItem();
            String updatedZaposleni = (String) zaposleniCombo.getSelectedItem();

            if (updatedNaslov.isEmpty() || updatedDatum.isEmpty() || updatedTrajanje.isEmpty() ||
                    updatedReziser == null || updatedZaposleni == null) {
                JOptionPane.showMessageDialog(editFrame, "Izpolnite vsa polja.");
                return;
            }

            callSpremeniFunction(naslov, updatedNaslov, updatedDatum, updatedTrajanje, updatedOpis, updatedReziser, updatedZaposleni);
            refreshFestivalTable();
            editFrame.dispose();
        });

        editFrame.add(new JLabel("Naslov:")); editFrame.add(titleField);
        editFrame.add(new JLabel("Datum (YYYY-MM-DD HH:MM:SS):")); editFrame.add(dateField);
        editFrame.add(new JLabel("Trajanje (min):")); editFrame.add(lengthField);
        editFrame.add(new JLabel("Opis:")); editFrame.add(descriptionField);
        editFrame.add(new JLabel("Režiser:")); editFrame.add(reziserCombo);
        editFrame.add(new JLabel("Zaposleni:")); editFrame.add(zaposleniCombo);
        editFrame.add(new JLabel()); editFrame.add(saveButton);

        editFrame.setLocationRelativeTo(null);
        editFrame.setVisible(true);
    }

    // --- Separate window for adding Reziser ---
    private static void openReziserWindow() {
        JFrame reziserFrame = new JFrame("Dodaj Reziserja");
        reziserFrame.setSize(400, 200);
        reziserFrame.setLayout(new GridLayout(4, 2, 5, 5));

        JTextField imeField = new JTextField();
        JTextField priimekField = new JTextField();
        JTextField datumField = new JTextField();

        JButton save = new JButton("Shrani");
        save.addActionListener(e -> {
            String ime = imeField.getText().trim();
            String priimek = priimekField.getText().trim();
            String datum = datumField.getText().trim();

            if (ime.isEmpty() || priimek.isEmpty() || datum.isEmpty()) {
                JOptionPane.showMessageDialog(reziserFrame, "Polja so prazna.", "Napaka", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                // Parse the date string to java.sql.Date
                java.sql.Date datumRojstva = java.sql.Date.valueOf(datum); // expects format YYYY-MM-DD

                // Call database method to insert the director
                Database.insertReziser(ime, priimek, datumRojstva);

                JOptionPane.showMessageDialog(reziserFrame, "Režiser shranjen!");

                // Optionally clear fields after save
                imeField.setText("");
                priimekField.setText("");
                datumField.setText("");

            } catch (IllegalArgumentException ex) {
                // This means the date format was invalid
                JOptionPane.showMessageDialog(reziserFrame, "Datum mora biti v formatu YYYY-MM-DD.", "Napaka", JOptionPane.ERROR_MESSAGE);
            }
        });

        reziserFrame.add(new JLabel("Ime:"));
        reziserFrame.add(imeField);
        reziserFrame.add(new JLabel("Priimek:"));
        reziserFrame.add(priimekField);
        reziserFrame.add(new JLabel("Datum rojstva (YYYY-MM-DD):"));
        reziserFrame.add(datumField);
        reziserFrame.add(new JLabel());
        reziserFrame.add(save);

        reziserFrame.setLocationRelativeTo(null);
        reziserFrame.setVisible(true);
    }

    private static void openAddWindow() {
        JFrame addFrame = new JFrame("Dodaj predstavo");
        addFrame.setSize(500, 400);
        addFrame.setLayout(new GridLayout(7, 2, 5, 5));

        JTextField titleField = new JTextField();
        JTextField dateField = new JTextField();
        JTextField lengthField = new JTextField();
        JTextField descriptionField = new JTextField();

        List<String> reziserList = Database.getAllReziserNames();
        JComboBox<String> reziserCombo = new JComboBox<>(reziserList.toArray(new String[0]));

        List<String> zaposleniList = Database.getAllZaposleniNames();
        JComboBox<String> zaposleniCombo = new JComboBox<>(zaposleniList.toArray(new String[0]));

        JButton saveButton = new JButton("Add");
        saveButton.addActionListener(e -> {
            String naslov = titleField.getText().trim();
            String datum = dateField.getText().trim();
            String trajanje = lengthField.getText().trim();
            String opis = descriptionField.getText().trim();
            String reziser = (String) reziserCombo.getSelectedItem();
            String zaposleni = (String) zaposleniCombo.getSelectedItem();

            if (naslov.isEmpty() || datum.isEmpty() || trajanje.isEmpty() ||
                    reziser == null || zaposleni == null) {
                JOptionPane.showMessageDialog(addFrame, "Izpolnite vsa poljas.");
                return;
            }

            try {
                int reziserId = Database.getReziserIdByName(reziser);
                int zaposleniId = Database.getZaposleniIdByName(zaposleni);

                LocalDateTime dateTime = LocalDateTime.parse(datum, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                float trajanjeFloat = Float.parseFloat(trajanje);

                Database.dodajPredstavo(naslov, Timestamp.valueOf(dateTime), trajanjeFloat, opis, reziserId, zaposleniId);
                JOptionPane.showMessageDialog(addFrame, "Predstava uspešno dodana.");
                refreshFestivalTable();
                addFrame.dispose();

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(addFrame, "Napaka: " + ex.getMessage());
            }
        });

        addFrame.add(new JLabel("Naslov:")); addFrame.add(titleField);
        addFrame.add(new JLabel("Datum (YYYY-MM-DD HH:MM:SS):")); addFrame.add(dateField);
        addFrame.add(new JLabel("Trajanje (min):")); addFrame.add(lengthField);
        addFrame.add(new JLabel("Opis:")); addFrame.add(descriptionField);
        addFrame.add(new JLabel("Režiser:")); addFrame.add(reziserCombo);
        addFrame.add(new JLabel("Zaposleni:")); addFrame.add(zaposleniCombo);
        addFrame.add(new JLabel()); addFrame.add(saveButton);

        addFrame.setLocationRelativeTo(null);
        addFrame.setVisible(true);
    }

    private static void openZaposleniWindow() {
        JFrame zaposleniFrame = new JFrame("Dodaj zaposlenega");
        zaposleniFrame.setSize(400, 250);
        zaposleniFrame.setLayout(new GridLayout(5, 2, 5, 5));

        JTextField imeField = new JTextField();
        JTextField priimekField = new JTextField();
        JTextField datumField = new JTextField();

        // Get actual kraji from database
        java.util.List<String> krajiList = Database.getAllKrajiNames();
        JComboBox<String> krajCombo = new JComboBox<>(krajiList.toArray(new String[0]));

        JButton save = new JButton("Shrani");
        save.addActionListener(e -> {
            String ime = imeField.getText().trim();
            String priimek = priimekField.getText().trim();
            String datum = datumField.getText().trim();
            String kraj = (String) krajCombo.getSelectedItem();

            if (ime.isEmpty() || priimek.isEmpty() || datum.isEmpty() || kraj == null) {
                JOptionPane.showMessageDialog(zaposleniFrame, "Polja niso izpolnjena.", "Napaka", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                // Convert to java.sql.Date - expects YYYY-MM-DD format
                java.sql.Date datumRojstva = java.sql.Date.valueOf(datum);

                // Get kraj ID from the database
                int krajId = Database.getKrajIdByName(kraj);
                if (krajId == -1) {
                    JOptionPane.showMessageDialog(zaposleniFrame, "Kraja ni v bazi.", "Napaka", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Call method to insert zaposleni
                Database.dodajZaposlenega(ime, priimek, datumRojstva, krajId);

                JOptionPane.showMessageDialog(zaposleniFrame, "Zaposleni shranjen!");

                // Clear fields
                imeField.setText("");
                priimekField.setText("");
                datumField.setText("");
                krajCombo.setSelectedIndex(0);

            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(zaposleniFrame, "Datum mora biti v formatu YYYY-MM-DD.", "Napaka", JOptionPane.ERROR_MESSAGE);
            }
        });

        zaposleniFrame.add(new JLabel("Ime:")); zaposleniFrame.add(imeField);
        zaposleniFrame.add(new JLabel("Priimek:")); zaposleniFrame.add(priimekField);
        zaposleniFrame.add(new JLabel("Datum rojstva (YYYY-MM-DD):")); zaposleniFrame.add(datumField);
        zaposleniFrame.add(new JLabel("Kraj:")); zaposleniFrame.add(krajCombo);
        zaposleniFrame.add(new JLabel()); zaposleniFrame.add(save);

        zaposleniFrame.setLocationRelativeTo(null);
        zaposleniFrame.setVisible(true);
    }



    private static void callSpremeniFunction(String sNaslov, String nNaslov, String nDatum, String nTrajanje,
                                             String nOpis, String nReziserFull, String nZaposleniFull) {
        try {
            int reziserId = Database.getReziserIdByName(nReziserFull);
            int zaposleniId = Database.getZaposleniIdByName(nZaposleniFull);
            if (reziserId == -1 || zaposleniId == -1) {
                JOptionPane.showMessageDialog(mainFrame, "Reziser ali zaposleni ni v dazabazi.");
                return;
            }

            LocalDateTime dateTime = LocalDateTime.parse(nDatum, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            float trajanjeFloat = Float.parseFloat(nTrajanje);

            Database.spremeniPredstavo(sNaslov, reziserId, nNaslov, Timestamp.valueOf(dateTime), trajanjeFloat, nOpis, reziserId, zaposleniId);
            JOptionPane.showMessageDialog(mainFrame, "Uspešno shranjeno.");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "Prišlo je do napake: " + e.getMessage());
        }
    }

    private static void callDeleteFunction(String naslov) {
        try {
            Database.deletePredstavaByTitle(naslov);
            JOptionPane.showMessageDialog(mainFrame, "Uspešno izbrisano.");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "Prišlo je do napake: " + e.getMessage());
        }
    }
}