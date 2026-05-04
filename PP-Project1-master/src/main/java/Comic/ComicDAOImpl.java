package Comic;

import util.DBConnectionUtility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ComicDAOImpl implements ComicDAO {

    static {
        ensureComicPricingColumns();
    }

    private static void ensureComicPricingColumns() {
        try (Connection conn = DBConnectionUtility.getConnection();
             Statement st = conn.createStatement()) {
            try {
                st.execute("ALTER TABLE Comic ADD COLUMN price REAL NOT NULL DEFAULT 0");
            } catch (SQLException ignored) {
                // duplicate column or missing Comic table
            }
            try {
                st.execute("ALTER TABLE Comic ADD COLUMN stock INTEGER NOT NULL DEFAULT 0");
            } catch (SQLException ignored) {
                // duplicate column or missing Comic table
            }
        } catch (SQLException e) {
            // no database file yet, etc.
        }
    }

    private static boolean hasColumn(ResultSet rs, String label) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        for (int i = 1; i <= md.getColumnCount(); i++) {
            if (label.equalsIgnoreCase(md.getColumnLabel(i))) {
                return true;
            }
        }
        return false;
    }

    private Comic extractComicFromResultSet(ResultSet rs) throws SQLException {
        int comicID = rs.getInt("comicID");
        String name = rs.getString("Name");
        String issue = rs.getString("Issue");
        String publisher = rs.getString("Publisher");
        double price = hasColumn(rs, "price") ? rs.getDouble("price") : 0.0;
        int stock = hasColumn(rs, "stock") ? rs.getInt("stock") : 0;
        return new Comic(comicID, name, issue, publisher, price, stock);
    }

    @Override
    public Optional<Comic> getComicById(int comicID) {
        String sql = "SELECT * FROM Comic WHERE comicID = ?";
        try (Connection conn = DBConnectionUtility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, comicID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(extractComicFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Comic> getAllComics() {
        return loadComics("SELECT * FROM Comic");
    }

    @Override
    public List<Comic> getComicsByPublisher(String publisher) {
        List<Comic> comics = new ArrayList<>();
        String sql = "SELECT * FROM Comic WHERE Publisher = ?";
        try (Connection conn = DBConnectionUtility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, publisher);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    comics.add(extractComicFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return comics;
    }

    private List<Comic> loadComics(String sql) {
        List<Comic> comics = new ArrayList<>();
        try (Connection conn = DBConnectionUtility.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                comics.add(extractComicFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return comics;
    }

    @Override
    public Optional<Comic> getComicsByName(String name) {
        String sql = "SELECT * FROM Comic WHERE Name = ?";
        try (Connection conn = DBConnectionUtility.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(extractComicFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public void deleteComicByID(int comicID) {
        String sql = "DELETE FROM Comic WHERE comicID = ?";
        try (Connection conn = DBConnectionUtility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, comicID);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void updateComic(Comic comic) {
        String sql = "UPDATE Comic SET Name = ?, Issue = ?, Publisher = ?, price = ?, stock = ? WHERE comicID = ?";
        try (Connection conn = DBConnectionUtility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, comic.getName());
            ps.setString(2, comic.getIssue());
            ps.setString(3, comic.getPublisher());
            ps.setDouble(4, comic.getPrice());
            ps.setInt(5, comic.getStock());
            ps.setInt(6, comic.getComicID());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void saveComic(Comic comic) {
        String sql = "INSERT INTO Comic (Name, Issue, Publisher, price, stock) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, comic.getName());
            ps.setString(2, comic.getIssue());
            ps.setString(3, comic.getPublisher());
            ps.setDouble(4, comic.getPrice());
            ps.setInt(5, comic.getStock());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
