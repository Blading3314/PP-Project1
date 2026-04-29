package test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import util.DBConnectionUtility;
import java.sql.*;

public class DatabaseTableTest {
    @Test
    void testTable(){
        try (Connection conn = DBConnectionUtility.getConnection()){
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Employee");
            while (rs.next()){
                System.out.println(rs.getString("firstName"));
            }
        }
        catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }
}
