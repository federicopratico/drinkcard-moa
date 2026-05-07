package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out;

public interface PasswordEncoder {
    String encode(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}
