import {Component} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {CommonModule} from "@angular/common";
import {ClienteService} from "./services/cliente.service";
import {ClienteFiltros} from "./models/cliente-filtros.model";
import { Tooltip } from 'bootstrap';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'frontend';

  constructor(private clienteService: ClienteService) {
  }

  carregarClientes(): void {
    console.log('TESTE');

    const filters: ClienteFiltros = {
      cpfCnpj: '',
      cidade: '',
      nome: ''
    }

    this.clienteService.listarFiltrado(filters, 0, 10)
      .subscribe({
        next: (clientes) => {
          console.log('Clientes: ', clientes);
        },
        error: (erro) => {
          console.error(erro)
        },
        complete: () => {
          console.log('Requisição concluída')
        }
      })
  }

  ngOnInit(): void {
    this.carregarClientes()
  }

  ngAfterViewInit(): void {
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    tooltipTriggerList.forEach(el => new Tooltip(el));
  }
}
