package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out;

public interface TokenService {
    String generateToken(String email);
    String getEmailFromToken(String token);
    boolean validateToken(String token);
}
