package org.example;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {

    public static List<String[]> fetchPredstave() {
        List<String[]> result = new ArrayList<>();
        String sql = """
            SELECT p.naslov, p.datum, p.trajanje, p.opis,
                   r.ime || ' ' || r.priimek AS reziser_ime,
                   z.ime || ' ' || z.priimek AS zaposleni_ime
            FROM predstave p
            JOIN reziserji r ON p.reziser_id = r."ID"
            JOIN zaposleni z ON p.zaposleni_id = z."ID"
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String naslov = rs.getString("naslov");
                String datum = rs.getString("datum");
                String trajanje = rs.getString("trajanje");
                String opis = rs.getString("opis");
                String reziserIme = rs.getString("reziser_ime");
                String zaposleniIme = rs.getString("zaposleni_ime");
                result.add(new String[]{naslov, datum, trajanje, opis, reziserIme, zaposleniIme});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static void deletePredstavaByTitle(String naslov) {
        String sql = "SELECT izbrisi_predstavo_po_naslovu(?);";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, naslov);
            stmt.executeQuery();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Napaka pri brisanju predstave: " + e.getMessage());
        }
    }

    public static int getZaposleniIdByName(String fullName) {
        String[] parts = fullName.split(" ");
        if (parts.length < 2) {
            return -1;
        }

        String firstName = parts[0];
        String lastName = parts[1];

        // 🔧 FIXED: use "ID" instead of id
        String sql = "SELECT \"ID\" FROM zaposleni WHERE ime = ? AND priimek = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("ID"); // 🔧 FIXED: also match "ID" in getInt
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static int getReziserIdByName(String fullName) {
        String[] parts = fullName.split(" ");
        if (parts.length < 2) {
            return -1;
        }

        String firstName = parts[0];
        String lastName = parts[1];

        String sql = "SELECT \"ID\" FROM reziserji WHERE ime = ? AND priimek = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("ID");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Napaka pri pridobivanju reziserja: " + e.getMessage());
        }

        return -1;
    }


    public static void spremeniPredstavo(
            String sNaslov,
            int sReziserId,
            String nNaslov,
            Timestamp nDatum,
            float nTrajanje,
            String nOpis,
            int nReziserId,
            int nZaposleniId
    ) {
        String sql = "SELECT spremeni_predstava(?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sNaslov);
            stmt.setInt(2, sReziserId);
            stmt.setString(3, nNaslov);
            stmt.setTimestamp(4, nDatum);
            stmt.setFloat(5, nTrajanje);
            stmt.setString(6, nOpis);
            stmt.setInt(7, nReziserId);
            stmt.setInt(8, nZaposleniId);

            stmt.execute();
            System.out.println("Predstava updated successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getAllReziserNames() {
        List<String> names = new ArrayList<>();
        String sql = "SELECT ime, priimek FROM reziserji ORDER BY priimek, ime";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                names.add(rs.getString("ime") + " " + rs.getString("priimek"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return names;
    }

    public static List<String> getAllZaposleniNames() {
        List<String> zaposleniList = new ArrayList<>();
        String sql = "SELECT full_name FROM get_all_zaposleni_names()";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                zaposleniList.add(rs.getString("full_name"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return zaposleniList;
    }

    public static void insertReziser(String ime, String priimek, java.sql.Date datumRojstva) {
        String sql = "SELECT dodaj_reziserja(?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ime);
            stmt.setString(2, priimek);
            stmt.setDate(3, datumRojstva);

            stmt.execute();
            JOptionPane.showMessageDialog(null, "Reziser uspešno dodan!");

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Napaka pri dodajanju reziserja: " + e.getMessage());
        }
    }

    public static List<String> getAllKrajiNames() {
        List<String> krajiList = new ArrayList<>();
        String sql = "SELECT ime FROM kraji ORDER BY ime";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                krajiList.add(rs.getString("ime"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return krajiList;
    }

    public static int getKrajIdByName(String ime) {
        String sql = "SELECT \"ID\" FROM kraji WHERE ime = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ime);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("ID");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1; // not found
    }

    public static void dodajZaposlenega(String ime, String priimek, Date datumRojstva, int krajId) {
        String sql = "SELECT dodaj_zaposlenega(?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ime);
            stmt.setString(2, priimek);
            stmt.setDate(3, datumRojstva);
            stmt.setInt(4, krajId);

            stmt.execute();
            System.out.println("Zaposleni shranjen.");

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Napaka pri shranjevanju zaposlenega: " + e.getMessage());
        }
    }
    public static void dodajPredstavo(String naslov, Timestamp datum, float trajanje, String opis, int reziserId, int zaposleniId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO predstave (naslov, datum, trajanje, opis, reziser_id, zaposleni_id) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, naslov);
                stmt.setTimestamp(2, datum);
                stmt.setFloat(3, trajanje);
                stmt.setString(4, opis);
                stmt.setInt(5, reziserId);
                stmt.setInt(6, zaposleniId);

                int rowsInserted = stmt.executeUpdate();
                System.out.println("Rows inserted: " + rowsInserted);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}