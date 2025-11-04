package jp.tuor.backend.utils;

public class StringUtils {
    public static String removerMascaraDigito(String valor) {
        if(valor == null || valor.isEmpty()) {
            return valor;
        }

        return valor.replaceAll("\\D", "");
    }
}
