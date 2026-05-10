import org.mindrot.jbcrypt.BCrypt;

public class TestPassword {
    public static void main(String[] args) {
        String password = "demo123";
        String newHash = BCrypt.hashpw(password, BCrypt.gensalt());
        
        System.out.println("New hash for " + password + ": " + newHash);
        System.out.println("Verification: " + BCrypt.checkpw(password, newHash));
    }
}
