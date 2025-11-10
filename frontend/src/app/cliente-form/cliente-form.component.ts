import {Component, EventEmitter, inject, Output} from '@angular/core';
import {FormArray, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {DatePipe, NgClass, NgForOf, NgIf} from "@angular/common";
import {NgxMaskDirective} from "ngx-mask";
import {Modal} from "bootstrap";
import {NotificationService} from "../shared/notification/notification.service";
import {ClienteService} from "../services/cliente.service";
import {HttpClient} from "@angular/common/http";
import {EnderecoDto} from "../dto/endereco.dto";
import {snOuNumberValidator} from "../shared/validators/custom-validators";
import {Cliente} from "../models/cliente.model";
import {ClienteDTO} from "../dto/cliente.dto";

@Component({
  selector: 'app-cliente-form',
  standalone: true,
  imports: [
    FormsModule,
    NgForOf,
    NgIf,
    NgxMaskDirective,
    ReactiveFormsModule,
    NgClass
  ],
  templateUrl: './cliente-form.component.html',
  styleUrl: './cliente-form.component.css'
})
export class ClienteFormComponent {
  private readonly notification = inject(NotificationService);
  private readonly clienteService = inject(ClienteService);
  private readonly datePipe = inject(DatePipe);
  private readonly fb = inject(FormBuilder);
  private readonly http = inject(HttpClient);

  clienteForm!: FormGroup; // '!' inicializar no ngOnInit
  modalInstance: Modal | null = null;
  isEditMode: boolean = false;
  isLoading: boolean = false;

  @Output() onSaveSuccess = new EventEmitter<void>();

  constructor() {
    this.inicializarFormulario();
    this.setUpConditionalValidators();
  }

  ngAfterViewInit(): void {
    const modalEl = document.getElementById("clienteModal");
    if (modalEl) {
      this.modalInstance = new Modal(modalEl);
    }
  }

  ngOnDestroy(): void {
    this.modalInstance?.dispose();
  }

  //--------------------------------------------------------------------------------------------------------
  //formulário do cliente e validadores
  inicializarFormulario(): void {
    this.clienteForm = this.fb.group({
      id: [null],
      tipoPessoa: ['FISICA', Validators.required],
      email: ['', [Validators.email]],
      ativo: [true, Validators.required],

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

  //get para controls do formulário
  get f() {
    return this.clienteForm.controls;
  }

  //set validadores dinâmicos com base em tipoPessoa
  setUpConditionalValidators(): void {
    this.clienteForm.get('tipoPessoa')?.valueChanges.subscribe(tipo => {
      this.updateValidators(tipo);
    });
  }

  //validadores dinâmicos
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

  //get para endereços do formulário
  get enderecosFormArray(): FormArray {
    return this.clienteForm.get('enderecos') as FormArray;
  }

  //cria um FormGroup com os campos de endereço e os validadores
  criarEnderecoFormGroup(endereco?: EnderecoDto): FormGroup {
    return this.fb.group({
      id: [endereco?.id || null],
      logradouro: [endereco?.logradouro || '', Validators.required],
      numero: [endereco?.numero || '', [snOuNumberValidator()]],
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

  //--------------------------------------------------------------------------------------------------------
  //modal
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
    this.isEditMode = true;
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

  fecharModalFormulario() {
    this.modalInstance?.hide();
  }

  //criar/editar cliente
  salvarCliente() {
    if (this.clienteForm.invalid) {
      this.clienteForm.markAllAsTouched();
      this.enderecosFormArray.controls.forEach(control => {
        (control as FormGroup).markAllAsTouched();
      });
      this.notification.showWarning('Preencha os campos obrigatórios e adicione ao menos um endereço.');
      return;
    }

    // const clienteDto: ClienteDTO = this.clienteForm.value;
    const clienteDto: ClienteDTO = this.getCleanFormValue();
    const saveObservable = this.isEditMode
      ? this.clienteService.editar(clienteDto)
      : this.clienteService.criar(clienteDto);

    saveObservable.subscribe({
      next: (res) => {
        const successMessage = this.isEditMode ? "Cliente editado com sucesso!" : "Cliente criado com sucesso!";
        this.notification.showSuccess(successMessage);
        this.fecharModalFormulario();
        this.onSaveSuccess.emit();//emissor para o componente pai
      },
      error: (err) => {
        console.error("Erro ao salvar cliente", err);
        this.notification.onApiError(err);
      }
    })
  }

  private getCleanFormValue(): ClienteDTO {
    const formValue = {...this.clienteForm.value} as ClienteDTO;

    const cleanDTO: any = {
      id: formValue.id,
      tipoPessoa: formValue.tipoPessoa,
      email: formValue.email,
      ativo: formValue.ativo,
    }

    if (formValue.tipoPessoa === 'FISICA') {
      cleanDTO.nome = formValue.nome;
      cleanDTO.dataNascimento = formValue.dataNascimento;
      cleanDTO.cpf = this.cleanString(formValue.cpf);
      cleanDTO.rg = this.cleanString(formValue.rg);

      cleanDTO.razaoSocial = null;
      cleanDTO.cnpj = null;
      cleanDTO.inscricaoEstadual = null;
      cleanDTO.dataCriacao = null;
    } else if (formValue.tipoPessoa === 'JURIDICA') {
      cleanDTO.razaoSocial = formValue.razaoSocial;
      cleanDTO.dataCriacao = formValue.dataCriacao;
      cleanDTO.cnpj = this.cleanString(formValue.cnpj);
      cleanDTO.inscricaoEstadual = this.cleanString(formValue.inscricaoEstadual);

      cleanDTO.nome = null;
      cleanDTO.dataNascimento = null;
      cleanDTO.cpf = null;
      cleanDTO.rg = null;
    }

    if (formValue.enderecos && formValue.enderecos.length > 0) {
      cleanDTO.enderecos = formValue.enderecos.map(end => {
        const cleanEnd = {...end};
        cleanEnd.cep = this.cleanString(cleanEnd.cep);
        return cleanEnd;
      });
    } else {
      cleanDTO.enderecos = [];
    }

    return cleanDTO as ClienteDTO;
  }

  private cleanString(value: string | null | undefined): string {
    if (!value) {
      return '';
    }

    return value.replace(/\D/g, '');
  }

  //--------------------------------------------------------------------------------------------------------
  //auxiliares API
  buscarCep(index: number): void {
    this.isLoading = true;
    const enderecoGroup = this.enderecosFormArray.at(index) as FormGroup;

    const cep = this.cleanString(enderecoGroup.get("cep")?.value);

    if (!cep || cep.length !== 8) {
      this.isLoading = false;
      this.notification.showWarning("Digite um CEP válido");
      return;
    }

    this.http.get<any>(`https://viacep.com.br/ws/${cep}/json`).subscribe({
      next: (res) => {
        this.isLoading = false;
        if(res.erro) {
          this.notification.showError('CEP não encontrado.');
          return;
        }

        enderecoGroup.patchValue({
          logradouro: res.logradouro,
          bairro: res.bairro,
          cidade: res.localidade,
          estado: res.uf,
          complemento: res.complemento
        });

        this.notification.showSuccess("Endereço preenchido.");
      },
      error: (err) => {
        this.isLoading = false;
        this.notification.showError("Erro ao consultar o CEP. Verifique a conexão.");
        console.error(err);
      }
    });
  }
}
