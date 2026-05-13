package com.fashionstore.util;

import org.mindrot.jbcrypt.BCrypt;

public class GenerateHashes {
    public static void main(String[] args) {
        String adminPassword = "admin123";
        String demoPassword = "demo123";
        
        String adminHash = BCrypt.hashpw(adminPassword, BCrypt.gensalt());
        String demoHash = BCrypt.hashpw(demoPassword, BCrypt.gensalt());
        
        System.out.println("=== BCrypt Hashes ===");
        System.out.println("Admin password: " + adminPassword);
        System.out.println("Admin hash: " + adminHash);
        System.out.println();
        System.out.println("Demo password: " + demoPassword);
        System.out.println("Demo hash: " + demoHash);
        System.out.println();
        System.out.println("=== Verification ===");
        System.out.println("Verify admin hash: " + BCrypt.checkpw(adminPassword, adminHash));
        System.out.println("Verify demo hash: " + BCrypt.checkpw(demoPassword, demoHash));
    }
}
