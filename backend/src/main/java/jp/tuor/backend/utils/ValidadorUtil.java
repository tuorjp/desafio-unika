package jp.tuor.backend.utils;

import jp.tuor.backend.model.dto.ClienteDTO;
import jp.tuor.backend.model.dto.EnderecoDTO;
import jp.tuor.backend.model.enums.TipoPessoa;
import jp.tuor.backend.service.exceptions.CampoInvalidoException;
import jp.tuor.backend.utils.annotations.Obrigatorio;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ValidadorUtil {
    public void validarCamposObrigatorios(ClienteDTO clienteDTO) {
        List<String> erros = new ArrayList<>();
        TipoPessoa tipoPessoa = clienteDTO.getTipoPessoa();
        Class<?> classe = clienteDTO.getClass();

        if (tipoPessoa == null) {
            erros.add("O campo 'tipoPessoa' é obrigatório.");
            throw new CampoInvalidoException(String.join("\n", erros));
        }

        List<String> camposFisica = Arrays.asList("cpf", "nome", "rg", "dataNascimento");
        List<String> camposJuridica = Arrays.asList("cnpj", "razaoSocial", "inscricaoEstadual", "dataCriacao");

        for (Field campo : classe.getDeclaredFields()) {
            if (campo.isAnnotationPresent(Obrigatorio.class)) {
                campo.setAccessible(true);
                Obrigatorio anotacao = campo.getAnnotation(Obrigatorio.class);
                String nomeCampo = campo.getName();

                try {
                    Object valorCampo = campo.get(clienteDTO);
                    boolean isObrigatorio = false;

                    if (anotacao.dependeDeCampo().isEmpty()) {
                        isObrigatorio = anotacao.isObrigatorio();
                    } else if (anotacao.dependeDeCampo().equals("tipoPessoa")) {
                        //validação condicional: depende do tipoPessoa
                        if (tipoPessoa == TipoPessoa.FISICA && camposFisica.contains(nomeCampo)) {
                            isObrigatorio = true;
                        } else if (tipoPessoa == TipoPessoa.JURIDICA && camposJuridica.contains(nomeCampo)) {
                            isObrigatorio = true;
                        }
                    }

                    //se for obrigatório neste contexto, valida o valor
                    if (isObrigatorio) {
                        if (valorCampo == null) {
                            erros.add("O campo '" + nomeCampo + "' é obrigatório.");
                        } else if (valorCampo instanceof String && ((String) valorCampo).trim().isEmpty()) {
                            erros.add("O campo '" + nomeCampo + "' não pode estar em branco.");
                        }
                    }

                } catch (IllegalAccessException e) {
                    System.out.println(e.getMessage());
                }
            }
        }

        List<EnderecoDTO> enderecos = clienteDTO.getEnderecos();

        if(enderecos != null && !enderecos.isEmpty()) {
            long countEnderecoPrincipal = enderecos.stream().filter(EnderecoDTO::isEnderecoPrincipal).count();

            if(countEnderecoPrincipal == 0) {
                erros.add("É obrigatório ao menos um endereço principal.");
            } else if(countEnderecoPrincipal > 1) {
                erros.add("Apenas um endereço pode ser marcado como principal");
            }

            for (int i = 0; i < enderecos.size(); i++) {
                EnderecoDTO endereco = enderecos.get(i);
                Class<?> classeEndereco = endereco.getClass();
                String prefixo = "Endereço [" + (i + 1) + "]: ";

                for (Field campoEndereco: classeEndereco.getDeclaredFields()) {
                    if(campoEndereco.isAnnotationPresent(Obrigatorio.class)) {
                        campoEndereco.setAccessible(true);

                        if(campoEndereco.getName().equals("enderecoPrincipal")) {
                            continue;
                        }

                        try {
                            Object valorCampo = campoEndereco.get(endereco);

                            if(valorCampo == null) {
                                erros.add(prefixo + "O campo '" + campoEndereco.getName() + "' é obrigatório.");
                            } else if(valorCampo instanceof String && ((String) valorCampo).trim().isEmpty()) {
                                erros.add(prefixo + "O campo '" + campoEndereco.getName() + "' não pode estar em branco.");
                            }
                        } catch (IllegalAccessException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }
            }
        }

        //se a lista de erros não estiver vazia, lança a exceção com todas as mensagens
        if (!erros.isEmpty()) {
            throw new CampoInvalidoException(String.join("; ", erros));
        }
    }
}
