export interface Endereco {
  id?: number;
  logradouro: string;
  numero: string;
  cep: string;
  bairro: string;
  cidade: string;
  estado: string;
  enderecoPrincipal: boolean;
  complemento?: string;
}
