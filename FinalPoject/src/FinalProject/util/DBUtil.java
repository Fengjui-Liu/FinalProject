package FinalProject.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {
    private static final String URL = "jdbc:mysql://140.119.19.73:3315/TG03?useSSL=false&serverTimezone=Asia/Taipei";
    private static final String USER = "TG03";
    private static final String PASSWORD = "VN8Q7j";

    // 提供取得連線的方法
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("❌ 無法連線到資料庫：" + e.getMessage());
            return null;
        }
    }
}
