package portaria.util;

import portaria.exception.DAOException;
import java.util.regex.Pattern;

public class ValidadorEntrada {

    // Permite letras (incluindo acentos), espaços, apóstrofos e hífens. Não permite números nem emojis.
    private static final Pattern NOME_PATTERN = Pattern.compile("^[\\p{L} \\.'\\-]+$");
    
    // Permite letras (sem acento), números e underlines. Sem espaços ou emojis.
    private static final Pattern LOGIN_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");

    public static void validarNome(String nome, String campo) throws DAOException {
        if (nome == null || nome.trim().isEmpty()) {
            throw new DAOException("Erro: O campo " + campo + " não pode estar vazio.");
        }
        if (!NOME_PATTERN.matcher(nome).matches()) {
            throw new DAOException("Erro: O campo " + campo + " contém caracteres inválidos. Use apenas letras (sem números ou emojis).");
        }
    }

    public static void validarLogin(String login) throws DAOException {
        if (login == null || login.trim().isEmpty()) {
            throw new DAOException("Erro: O login não pode estar vazio.");
        }
        if (!LOGIN_PATTERN.matcher(login).matches()) {
            throw new DAOException("Erro: O login deve conter apenas letras, números e underline (_), sem espaços ou emojis.");
        }
    }

    public static void validarApartamento(int torre, int bloco, int andar, int numero) throws DAOException {
        if (torre <= 0 || bloco <= 0 || andar < 0 || numero <= 0) {
            throw new DAOException("Erro: Os números do apartamento não podem ser negativos ou zeros (exceto andar térreo = 0).");
        }
    }
}

