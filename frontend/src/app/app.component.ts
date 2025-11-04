import {Component} from '@angular/core';
import {CommonModule, DatePipe} from "@angular/common";
import {ClienteService} from "./services/cliente.service";
import {ClienteFiltros} from "./models/cliente-filtros.model";
import {Tooltip} from 'bootstrap';
import {FormsModule} from "@angular/forms";
import {Cliente} from "./models/cliente.model";
import {EnderecoDto} from "./dto/endereco.dto";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
  ],
  providers: [DatePipe],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {

  //estado do componente
  listaClientes: Cliente[] = [];
  filtros: ClienteFiltros = {nome: '', cpfCnpj: '', cidade: ''};

  paginaAtual: number = 0;
  tamanhoPagina: number = 10;
  totalPaginas: number = 0;
  totalElementos: number = 0;

  isLoading: boolean = false;

  //construtor e dependências
  constructor(
    private clienteService: ClienteService,
    private datePipe: DatePipe
  ) {
  }

  //inicializações
  ngOnInit(): void {
    this.carregarClientesFiltrados()
  }

  ngAfterViewInit(): void {
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    tooltipTriggerList.forEach(el => new Tooltip(el));
  }

  //funções http
  carregarClientesFiltrados(): void {
    this.isLoading = true;
    this.listaClientes = [];

    this.clienteService.listarFiltrado(this.filtros, this.paginaAtual, this.tamanhoPagina)
      .subscribe({
        next: (pagina) => {
          this.listaClientes = pagina.content;
          this.totalPaginas = pagina.totalPages;
          this.totalElementos = pagina.totalElements;
        },
        error: (err) => {
          console.error("Erro na listagem inicial: ", err)
        },
        complete: () => {
          this.isLoading = false
        }
      });
  }

  //chamadas da UI
  onFiltrarClick(): void {
    this.paginaAtual = 0;
    this.carregarClientesFiltrados()
  }

  onLimparClick(): void {
    this.filtros = {nome: '', cidade: '', cpfCnpj: ''};
    this.paginaAtual = 0;
    this.carregarClientesFiltrados();
  }

  proximaPagina(): void {
    if (this.paginaAtual < this.totalPaginas - 1) {
      this.paginaAtual++;
      this.carregarClientesFiltrados();
    }
  }

  paginaAnterior(): void {
     if(this.paginaAtual > 0) {
       this.paginaAtual--;
       this.carregarClientesFiltrados();
     }
  }

  //auxiliares
  getNomeCliente(cliente: Cliente): string {
    const nome = cliente.tipoPessoa == 'FISICA' ? cliente.nome : cliente.razaoSocial;

    return nome ? nome : ''
  }

  getDataCliente(cliente: Cliente): string {
    const data = cliente.tipoPessoa === 'FISICA' ? cliente.dataNascimento : cliente.dataCriacao;
    return this.datePipe.transform(data, 'dd/MM/yyyy') || 'N/A';
  }

  getEnderecoPrincipal(enderecos: EnderecoDto[] | undefined): string {
    if (!enderecos || enderecos.length === 0) {
      return 'N/A';
    }
    const principal = enderecos.find(e => e.enderecoPrincipal);
    return principal ? `${this.formatarCep(principal.cep)}; ${principal.logradouro}, ${principal.numero}, ${principal.cidade}-${principal.estado}` : 'N/A';
  }

  private formatarCep(cep: string | null | undefined): string {
    if (!cep) {
      return '';
    }

    const cepLimpo = cep.replace(/\D/g, '');

    if (cepLimpo.length === 8) {
      return cepLimpo.replace(/(\d{5})(\d{3})/, '$1-$2');
    }

    return cep;
  }

  getDocumentoCliente(cliente: Cliente): string {
    const doc = cliente.tipoPessoa === 'FISICA' ? cliente.cpf : cliente.cnpj;

    if (!doc) {
      return 'N/A';
    }

    if (cliente.tipoPessoa === 'FISICA' && doc.length === 11) {
      return doc.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
    }

    if (cliente.tipoPessoa === 'JURIDICA' && doc.length === 14) {
      return doc.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, '$1.$2.$3/$4-$5');
    }

    return doc;
  }
}
