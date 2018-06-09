package info.diniz.pontointeligente.api.utils;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtilsTest {

	private static final String SENHA = "123456";
	
	private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
	
	@Test
	public void testSenhaNula() throws Exception {
		assertNull(PasswordUtils.geraBCrypt(null));
	}
	
	@Test
	public void testGerarHashSenha() throws Exception {
		String hash = PasswordUtils.geraBCrypt(SENHA);
		assertTrue(encoder.matches(SENHA, hash));
	}
	
}
