package br.com.reservadaaldeia.portaria.util;

public class ValidadorCPF {
    
    public static boolean isCPF(String CPF) {
        // Remove caracteres especiais se houver
        CPF = CPF.replaceAll("[^0-9]", "");
        
        // Considera-se erro CPF's formados por uma sequencia de numeros iguais
        if (CPF.equals("00000000000") ||
            CPF.equals("11111111111") ||
            CPF.equals("22222222222") || CPF.equals("33333333333") ||
            CPF.equals("44444444444") || CPF.equals("55555555555") ||
            CPF.equals("66666666666") || CPF.equals("77777777777") ||
            CPF.equals("88888888888") || CPF.equals("99999999999") ||
            (CPF.length() != 11))
            return(false);

        char dig10, dig11;
        int sm, i, r, num, peso;

        try {
            // Calculo do 1o. Digito Verificador
            sm = 0;
            peso = 10;
            for (i=0; i<9; i++) {
                num = (int)(CPF.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso - 1;
            }

            r = 11 - (sm % 11);
            if ((r == 10) || (r == 11))
                dig10 = '0';
            else dig10 = (char)(r + 48);

            // Calculo do 2o. Digito Verificador
            sm = 0;
            peso = 11;
            for(i=0; i<10; i++) {
                num = (int)(CPF.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso - 1;
            }

            r = 11 - (sm % 11);
            if ((r == 10) || (r == 11))
                dig11 = '0';
            else dig11 = (char)(r + 48);

            // Verifica se os digitos calculados conferem com os digitos informados.
            if ((dig10 == CPF.charAt(9)) && (dig11 == CPF.charAt(10)))
                 return(true);
            else return(false);
        } catch (Exception erro) {
            return(false);
        }
    }

    public static String formatarEObscurecerCPF(String cpf, String tipoUsuario) {
        if (cpf == null) return "";
        
        String apenasDigitos = cpf.replaceAll("[^0-9]", "");
        
        if (apenasDigitos.length() != 11) {
            if (cpf.length() >= 5) {
                if ("SINDICO".equals(tipoUsuario)) {
                    return cpf;
                } else {
                    return cpf.substring(0, 3) + "...xxx..." + cpf.substring(cpf.length() - 2);
                }
            }
            return cpf;
        }
        
        if ("SINDICO".equals(tipoUsuario)) {
            return apenasDigitos.substring(0, 3) + "." +
                   apenasDigitos.substring(3, 6) + "." +
                   apenasDigitos.substring(6, 9) + "-" +
                   apenasDigitos.substring(9, 11);
        } else {
            return apenasDigitos.substring(0, 3) + ".xxx.xxx-" + apenasDigitos.substring(9, 11);
        }
    }
}
