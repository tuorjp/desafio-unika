import {Endereco} from "./endereco.model";

export interface Cliente {
  id: number;
  nome: string;
  razaoSocial: string;
  cpf: string;
  cnpj: string;
  dataNascimento: Date;
  dataCriacao: Date;
  ativo: boolean;
  enderecos: Endereco[];
}
