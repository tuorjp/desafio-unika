import {TipoPessoa} from "../enum/tipo-pessoa.enum";
import {EnderecoDto} from "../dto/endereco.dto";

export interface Cliente {
  id?: number;
  tipoPessoa: TipoPessoa;
  cpf?: string;
  cnpj?: string;
  nome?: string;
  rg?: string;
  dataNascimento?: Date;
  dataCriacao?: Date;
  razaoSocial?: string;
  inscricaoEstadual?: string;
  email?: string;
  ativo?: boolean;
  enderecos?: EnderecoDto[];
}
