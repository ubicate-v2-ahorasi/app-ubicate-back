package app_jwt.auth_service.shared.util;

public final class CredentialsUtil {

    private CredentialsUtil() {}

    public static String generateTempPasswordFromDni(String dni) {
        if (dni == null || dni.length() < 8) {
            return "Ch" + (dni == null ? "00000000" : dni) + "#2025";
        }
        String first4 = dni.substring(0, 4);
        String last4  = dni.substring(dni.length() - 4);
        return "Ch" + first4 + "#" + last4;
    }

    public static String usernameFromEmail(String email) {
        return email;
    }
}
