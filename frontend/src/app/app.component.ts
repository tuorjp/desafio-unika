import {Component, inject} from '@angular/core';
import {CommonModule, DatePipe} from "@angular/common";
import {ClienteService} from "./services/cliente.service";
import {ClienteFiltros} from "./models/cliente-filtros.model";
import {Modal, Tooltip} from 'bootstrap';
import {FormArray, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {Cliente} from "./models/cliente.model";
import {EnderecoDto} from "./dto/endereco.dto";
import {ClienteDTO} from "./dto/cliente.dto";
import {NotificationService} from "./shared/notification/notification.service";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule
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
  tamanhoPagina: number = 10;
  totalPaginas: number = 0;
  totalElementos: number = 0;
  isLoading: boolean = false;

  clienteForm!: FormGroup; // '!' inicializar no ngOnInit
  modalInstance: Modal | null = null;
  isEditMode: boolean = false;

  //construtor e dependências
  constructor(
    private clienteService: ClienteService,
    private datePipe: DatePipe,
    private fb: FormBuilder
  ) {
  }

  //inicializações
  ngOnInit(): void {
    this.carregarClientesFiltrados();
    this.inicializarFormulario();
    this.setUpConditionalValidators();
  }

  ngAfterViewInit(): void {
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    tooltipTriggerList.forEach(el => new Tooltip(el));

    const modalEl = document.getElementById("clienteModal");
    if (modalEl) {
      this.modalInstance = new Modal(modalEl);
    }
  }

  //get para controls dop formulário
  get f() {
    return this.clienteForm.controls;
  }

  //formulário
  inicializarFormulario(): void {
    this.clienteForm = this.fb.group({
      id: [null],
      tipoPessoa: ['FISICA', Validators.required],
      email: ['', [Validators.email]],
      ativo: [true],

      //PF
      cpf: [''],
      nome: [''],
      rg: [''],
      dataNascimento: [''],

      //PJ
      cnpj: [''],
      razaoSocial: [''],
      inscricaoEstadual: [''],
      dataCriacao: [''],

      //endereços
      enderecos: this.fb.array([], Validators.minLength(1))
    });

    this.updateValidators(this.clienteForm.get('tipoPessoa')?.value)
  }

  setUpConditionalValidators(): void {
    this.clienteForm.get('tipoPessoa')?.valueChanges.subscribe(tipo => {
      this.updateValidators(tipo);
    });
  }

  updateValidators(tipo: string): void {
    //campos
    const cpf = this.clienteForm.get('cpf');
    const nome = this.clienteForm.get('nome');
    const cnpj = this.clienteForm.get('cnpj');
    const razaoSocial = this.clienteForm.get('razaoSocial');
    const dataNascimento = this.clienteForm.get('dataNascimento');
    const dataCriacao = this.clienteForm.get('dataCriacao');
    const rg = this.clienteForm.get('rg');
    const inscricaoEstadual = this.clienteForm.get('inscricaoEstadual');

    //limpar validações
    cpf?.clearValidators();
    nome?.clearValidators();
    cnpj?.clearValidators();
    razaoSocial?.clearValidators();
    dataCriacao?.clearValidators();
    dataNascimento?.clearValidators();
    rg?.clearValidators();
    inscricaoEstadual?.clearValidators();

    //setar validações
    if (tipo === 'FISICA') {
      cpf?.setValidators([Validators.required]);
      nome?.setValidators([Validators.required]);
      dataNascimento?.setValidators([Validators.required]);
      rg?.setValidators([Validators.required]);
    } else if (tipo === 'JURIDICA') {
      cnpj?.setValidators([Validators.required]);
      razaoSocial?.setValidators([Validators.required]);
      dataCriacao?.setValidators([Validators.required]);
      inscricaoEstadual?.setValidators([Validators.required]);
    }

    //atualizar e validar
    cpf?.updateValueAndValidity();
    nome?.updateValueAndValidity();
    cnpj?.updateValueAndValidity();
    razaoSocial?.updateValueAndValidity();
    dataNascimento?.updateValueAndValidity();
    dataCriacao?.updateValueAndValidity();
    rg?.updateValueAndValidity();
    inscricaoEstadual?.updateValueAndValidity();
  }

  get enderecosFormArray(): FormArray {
    return this.clienteForm.get('enderecos') as FormArray;
  }

  criarEnderecoFormGroup(endereco?: EnderecoDto): FormGroup {
    return this.fb.group({
      id: [endereco?.id || null],
      logradouro: [endereco?.logradouro || '', Validators.required],
      numero: [endereco?.numero || '', Validators.required],
      cep: [endereco?.cep || '', Validators.required],
      bairro: [endereco?.bairro || '', Validators.required],
      cidade: [endereco?.cidade || '', Validators.required],
      estado: [endereco?.estado || '', Validators.required],
      enderecoPrincipal: [endereco?.enderecoPrincipal || false],
      complemento: [endereco?.complemento || '']
    })
  }

  adicionarEndereco(): void {
    const novoEndereco = this.criarEnderecoFormGroup();
    this.enderecosFormArray.push(novoEndereco);
  }

  removerEndereco(index: number) {
    this.enderecosFormArray.removeAt(index)
  }

  abrirModalNovo(): void {
    this.isEditMode = false;
    this.clienteForm.reset({
      tipoPessoa: 'FISICA',
      ativo: true
    });

    this.enderecosFormArray.clear();
    this.adicionarEndereco();
    this.modalInstance?.show();
  }

  abrirModalEditar(cliente: Cliente) {
    this.isEditMode = false;
    this.clienteForm.reset();
    this.enderecosFormArray.clear();

    const dataNascFormatada = this.datePipe.transform(cliente.dataNascimento, 'yyyy-MM-dd');
    const dataCriacaoFormatada = this.datePipe.transform(cliente.dataCriacao, 'yyyy-MM-dd');

    this.clienteForm.patchValue({
      ...cliente,
      dataNascimento: dataNascFormatada,
      dataCriacao: dataCriacaoFormatada
    });

    if (cliente.enderecos && cliente.enderecos.length > 0) {
      cliente.enderecos.forEach((element) => {
        this.enderecosFormArray.push(this.criarEnderecoFormGroup(element as EnderecoDto));
      });
    } else {
      this.adicionarEndereco();
    }

    this.modalInstance?.show();
  }

  fecharModal() {
    this.modalInstance?.hide();
  }

  //funções http, chamadas ao service
  //criar/editar cliente
  onSalvar() {
    if (this.clienteForm.invalid) {
      this.clienteForm.markAllAsTouched();
      this.enderecosFormArray.controls.forEach(control => {
        (control as FormGroup).markAllAsTouched();
      });
      this.notification.showWarning('Preencha os campos obrigatórios e adicione ao menos um endereço.');
      return;
    }

    const clienteDto: ClienteDTO = this.clienteForm.value;
    if (this.isEditMode) {
      this.clienteService.editar(clienteDto)
        .subscribe({
          next: (res) => {
            console.log("Cliente editado")
            this.fecharModal();
            this.carregarClientesFiltrados();
          },
          error: (err) => {
            console.error("Erro ao editar cliente", err)
          }
        });
    } else {
      this.clienteService.criar(clienteDto)
        .subscribe({
          next: (res) => {
            this.fecharModal();
            this.carregarClientesFiltrados();
            this.notification.showSuccess("Cliente criado com sucesso!")
          },
          error: (err) => {
            console.error("Erro ao criar cliente ", err?.message);
            this.notification.onApiError(err);
          }
        });
    }
  }

  //listarClientes
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
          this.notification.showError(err?.message)
        },
        complete: () => {
          this.isLoading = false
        }
      });
  }

  //chamadas da UI filtros e tabela
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
    if (this.paginaAtual > 0) {
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
