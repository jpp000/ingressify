package cesar.rv.ingressify.dominio.identidade.usuario;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

import org.apache.commons.lang3.Validate;

public final class PasswordHasher {

	private static final String SEP = ":";

	private PasswordHasher() {
	}

	public static String hash(String senhaPlana) {
		Validate.notBlank(senhaPlana, "senhaPlana");
		String salt = UUID.randomUUID().toString();
		return salt + SEP + digest(salt, senhaPlana);
	}

	public static boolean verificar(String senhaPlana, String hashArmazenado) {
		Validate.notBlank(senhaPlana, "senhaPlana");
		Validate.notBlank(hashArmazenado, "hashArmazenado");
		int sep = hashArmazenado.indexOf(SEP);
		if (sep < 1 || sep == hashArmazenado.length() - 1) {
			return false;
		}
		String salt = hashArmazenado.substring(0, sep);
		String esperado = hashArmazenado.substring(sep + 1);
		return MessageDigest.isEqual(esperado.getBytes(StandardCharsets.UTF_8),
				digest(salt, senhaPlana).getBytes(StandardCharsets.UTF_8));
	}

	private static String digest(String salt, String senhaPlana) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(salt.getBytes(StandardCharsets.UTF_8));
			byte[] out = md.digest(senhaPlana.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(out);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 não disponível", e);
		}
	}
}
