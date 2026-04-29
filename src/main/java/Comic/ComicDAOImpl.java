package Comic;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.sql.*;
import util.DBConnectionUtility;

public class ComicDAOImpl implements ComicDAO{

    private Comic extractComicFromResultSet(ResultSet rs) throws SQLException{
        int comicID = rs.getInt("comicID");
        String name = rs.getString("Name");
        String issue = rs.getString("Issue");
        String publisher = rs.getString("Publisher");
        return new Comic(comicID, name, issue, publisher);
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
            } else {
                return Optional.empty();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Comic> getAllComics() {
        List <Comic> comics = new ArrayList<>();
        String sql = "SELECT * FROM Comic";
        return getComics(comics, sql);
    }

    @Override
    public List<Comic> getComicsByPublisher(String publisher) {
        List <Comic> comics = new ArrayList<>();
        String sql = "SELECT * FROM Comic WHERE Publisher = ?";
        return getComics(comics, sql);
    }

    private List<Comic> getComics(List<Comic> comics, String sql) {
        try (Connection conn = DBConnectionUtility.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)){
            while (rs.next()){
                comics.add(extractComicFromResultSet(rs));
            }
        }
        catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return comics;
    }

    @Override
    public Optional<Comic> getComicsByName(String name) {
        String sql = "SELECT * FROM Comic WHERE Name = ?";
        try (Connection conn = DBConnectionUtility.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()){
                return Optional.of(extractComicFromResultSet(rs));
            }
        }
        catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public void deleteComicByID(int comicID) {
        String sql = "DELETE FROM Comic WHERE comicID = ?";
        try (Connection conn = DBConnectionUtility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, comicID);
            ps.executeUpdate();
        }
        catch (SQLException e){
            System.out.println(e.getMessage());
        }

    }

    @Override
    public void updateComic(Comic comic) {
        String sql = "UPDATE Comic SET Publisher=? WHERE comicID = ?";
        try (Connection conn = DBConnectionUtility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, comic.getPublisher());
            ps.setInt(2, comic.getComicID());
            ps.executeUpdate();
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void saveComic(Comic comic) {
        String sql = "INSERT INTO Comic (Name, Issue, Publisher) VALUES (?, ?, ?)";
        try (Connection conn = DBConnectionUtility.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, comic.getName());
            ps.setString(2, comic.getIssue());
            ps.setString(3, comic.getPublisher());
            ps.executeUpdate();
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
