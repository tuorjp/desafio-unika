import {Injectable} from "@angular/core";
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from "rxjs";
import {Cliente} from "../models/cliente.model";
import {Page} from "../models/page.model";
import {ClienteDTO} from "../dto/cliente.dto";
import {ClienteFiltros} from "../models/cliente-filtros.model";
import {ResumoClientesPorCidade} from "../models/resumo-clientes-por-cidade.model";
import {TipoPessoa} from "../enum/tipo-pessoa.enum";
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class ClienteService {

  private readonly apiUrl = this.getApiUrl();

  constructor(private http: HttpClient) {
  }

  private getApiUrl(): string {
    if (window.location.host.includes('10.0.2.2')) {
      return environment.apiUrlMobile;
    }

    return environment.apiUrlWeb;
  }

  listarTodos(): Observable<Cliente[]> {
    return this.http.get<Cliente[]>(`${this.apiUrl}/list-all`);
  }

  listarPaginado(page: number, size: number): Observable<Page<Cliente>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Page<Cliente>>(`${this.apiUrl}/list-page`, {params});
  }

  listarFiltrado({nome, cpfCnpj, cidade}: ClienteFiltros, page: number, size: number): Observable<Page<Cliente>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (nome) {
      params = params.set('nome', nome);
    }

    if (cpfCnpj) {
      params = params.set('cpfCnpj', cpfCnpj)
    }

    if (cidade) {
      params = params.set('cidade', cidade)
    }

    return this.http.get<Page<Cliente>>(`${this.apiUrl}/list-filter`, {params});
  }

  buscarResumoPorCidade(): Observable<ResumoClientesPorCidade[]> {
    return this.http.get<ResumoClientesPorCidade[]>(`${this.apiUrl}/summary-city`);
  }

  buscaPorTipoEEstado(tipo: TipoPessoa, estado: string): Observable<Cliente[]> {
    const params = new HttpParams()
      .set('tipo', tipo)
      .set('estado', estado);

    return this.http.get<Cliente[]>(`${this.apiUrl}/search-by-state`, {params});
  }

  criar(cliente: ClienteDTO): Observable<any> {
    return this.http.post(`${this.apiUrl}/create`, cliente);
  }

  editar(cliente: ClienteDTO): Observable<any> {
    return this.http.put(`${this.apiUrl}/edit`, cliente);
  }

  importarExcel(file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file, file.name);

    return this.http.post(`${this.apiUrl}/import/excel`, formData);
  }

  exportarExcel(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/export-excel`, {
      responseType: 'blob'
    })
  }

  exportarPDF(id: number | null = null): Observable<Blob> {
    if(id != null) {
      return this.http.get(`${this.apiUrl}/report/pdf/${id}`, {
        responseType: "blob"
      });
    }

    return this.http.get(`${this.apiUrl}/report/pdf/geral`, {
      responseType: "blob"
    });
  }

  deletarCliente(id: number): Observable<any> {
    const params: HttpParams = new HttpParams().set('id', id);
    return this.http.delete(`${this.apiUrl}/delete-client`, {params});
  }
}
