import {Component, ElementRef, inject, ViewChild} from '@angular/core';
import {CommonModule, DatePipe} from "@angular/common";
import {ClienteService} from "./services/cliente.service";
import {ClienteFiltros} from "./models/cliente-filtros.model";
import {Modal, Tooltip} from 'bootstrap';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {Cliente} from "./models/cliente.model";
import {EnderecoDto} from "./dto/endereco.dto";
import {NotificationService} from "./shared/notification/notification.service";
import {NgxMaskDirective} from "ngx-mask";
import {ClienteFormComponent} from "./cliente-form/cliente-form.component";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    ClienteFormComponent,
    NgxMaskDirective,
  ],
  providers: [DatePipe],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  //notificação
  private readonly notification = inject(NotificationService);

  //estado do componente
  listaClientes: Cliente[] = [];
  filtros: ClienteFiltros = {nome: '', cpfCnpj: '', cidade: ''};
  paginaAtual: number = 0;
  tamanhoPagina: number = 5;
  totalPaginas: number = 0;
  totalElementos: number = 0;
  isLoading: boolean = false;
  importErrors: String[] = [];

  //modal de deleção
  deleteModalInstance: Modal | null = null;
  clienteParaDeletarId: number | null = null;
  clienteParaDeletarNome: string | null = null;

  //elemento para seleção de arquivos no front
  @ViewChild('fileInput') fileInput!: ElementRef

  //componente do formulário
  @ViewChild(ClienteFormComponent) clienteFormModal!: ClienteFormComponent;

  //construtor e dependências
  constructor(
    private clienteService: ClienteService,
    private datePipe: DatePipe,
  ) {
  }

  //--------------------------------------------------------------------------------------------------------
  //inicializações
  ngOnInit(): void {
    this.carregarClientesFiltrados();
  }

  ngAfterViewInit(): void {
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    tooltipTriggerList.forEach(el => new Tooltip(el));

    //modal de deleção
    const deleteModalEl = document.getElementById("confirmDeleteModal");
    if (deleteModalEl) {
      this.deleteModalInstance = new Modal(deleteModalEl);
    }
  }

  //--------------------------------------------------------------------------------------------------------
  //chamadas ao modal de formulário
  modalNovo() {
    this.clienteFormModal.abrirModalNovo();
  }

  modalEditar(cliente: Cliente) {
    this.clienteFormModal.abrirModalEditar(cliente);
  }

  onFormularioSalvo() {
    this.carregarClientesFiltrados();
  }

  //--------------------------------------------------------------------------------------------------------
  //chamadas ao modal delete

  abrirModalDelete(id: number | undefined, nomeCliente: string): void {
    if (id) {
      this.clienteParaDeletarId = id;
      this.clienteParaDeletarNome = nomeCliente;
      this.deleteModalInstance?.show();
    }
  }

  fecharModalDelete() {
    this.deleteModalInstance?.hide();
    this.clienteParaDeletarId = null;
    this.clienteParaDeletarNome = null;
  }

  //--------------------------------------------------------------------------------------------------------
  //funções http, chamadas ao service

  //deletar
  confirmarDelete(): void {
    if (!this.clienteParaDeletarId) return;

    this.clienteService.deletarCliente(this.clienteParaDeletarId).subscribe({
      next: () => {
        this.notification.showSuccess('Cliente excluído com sucesso.');
        this.carregarClientesFiltrados();
        this.fecharModalDelete();
      },
      error: (err) => {
        this.notification.onApiError(err);
        this.fecharModalDelete()
      }
    });
  }

  //listarClientes
  carregarClientesFiltrados(): void {
    this.isLoading = true;
    this.listaClientes = [];

    const filtrosParaApi = { ...this.filtros };

    if(filtrosParaApi.cpfCnpj) {
      filtrosParaApi.cpfCnpj = filtrosParaApi.cpfCnpj.replace(/\D/g, '');
    }

    this.clienteService.listarFiltrado(filtrosParaApi, this.paginaAtual, this.tamanhoPagina)
      .subscribe({
        next: (pagina) => {
          this.listaClientes = pagina.content;
          this.totalPaginas = pagina.totalPages;
          this.totalElementos = pagina.totalElements;
        },
        error: (err) => {
          this.notification.showError(err?.message)
        },
        complete: () => {
          this.isLoading = false
        }
      });
  }

  //cria excel com clientes do banco
  exportarClientes() {
    this.isLoading = true;
    this.clienteService.exportarExcel().subscribe({
      next: (blobData) => {
        const a = document.createElement('a');
        const objectUrl = window.URL.createObjectURL(blobData);
        a.href = objectUrl;

        a.download = 'relatorio_clientes.xlsx';
        document.body.appendChild(a);
        a.click();

        window.URL.revokeObjectURL(objectUrl);
        document.body.removeChild(a);

        this.isLoading = false;
        this.notification.showSuccess("Relatório exportado com sucesso");
      },
      error: (err) => {
        this.isLoading = false;
        this.notification.onApiError(err);
      }
    });
  }

  importarClientes() {
    this.fileInput.nativeElement.value = null;
    this.fileInput.nativeElement.click()
  }

  arquivoSelecionadoImportacaoXLSX(event: any) {
    const file: File | null = event.target.files?.[0] || null;

    if (!file) {
      return;
    }

    const allowedTypes = [
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', //.xlsx
      'application/vnd.ms-excel' //.xls
    ];

    if (!allowedTypes.includes(file.type)) {
      this.notification.showWarning('Por favor, selecione um arquivo Excel (.xlsx ou .xls).');
      return;
    }

    this.isLoading = true;
    this.notification.showInfo('Iniciando importação...');

    this.clienteService.importarExcel(file).subscribe({
      next: (_response) => {
          this.notification.showSuccess('Arquivo importado com sucesso');
          this.carregarClientesFiltrados();
          this.importErrors = [];
      },
      error: (err) => {
        this.isLoading = false;
        this.notification.showError("Ocorreu um erro ao importar arquivo.");

        if (err?.error && err?.error?.erros && err?.error?.erros?.length > 0) {
          this.importErrors = err?.error?.erros;
        }
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }

  exportarClientesPDF(id: number | null = null) {
    this.isLoading = true;
    this.clienteService.exportarPDF(id).subscribe({
      next: (res) => {
        const a = document.createElement('a');
        const objectUrl = window.URL.createObjectURL(res);
        a.href = objectUrl;

        a.download = 'relatorio_clientes.pdf';
        document.body.appendChild(a);
        a.click();

        window.URL.revokeObjectURL(objectUrl);
        document.body.removeChild(a);

        this.isLoading = false;
        this.notification.showSuccess("Relatório PDF exportado com sucesso");
      },
      error: (err) => {
        this.isLoading = false;
        this.notification.onApiError(err);
      }
    });
  }

  //--------------------------------------------------------------------------------------------------------
  //chamadas da UI filtros e tabela
  filtrar(): void {
    this.paginaAtual = 0;
    this.carregarClientesFiltrados()
  }

  limparFiltro(): void {
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
    if (this.paginaAtual > 0) {
      this.paginaAtual--;
      this.carregarClientesFiltrados();
    }
  }

  //--------------------------------------------------------------------------------------------------------
  //auxiliares
  getNomeCliente(cliente: Cliente): string {
    const nome = cliente.tipoPessoa == 'FISICA' ? cliente.nome : cliente.razaoSocial;

    return nome ? nome : ''
  }

  getDataCliente(cliente: Cliente): string {
    const data = cliente.tipoPessoa === 'FISICA' ? cliente.dataNascimento : cliente.dataCriacao;
    return this.datePipe.transform(data, 'dd/MM/yyyy', 'UTC') || 'N/A';
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
