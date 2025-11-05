import {inject, Injectable} from '@angular/core';
import {ToastrService} from "ngx-toastr";

type ToastOptions = Partial<import('ngx-toastr').ToastrConfig>;

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private toastr = inject(ToastrService);

  public showSuccess(message: string, title: string = "Sucesso!" ) {
    this.toastr.success(message, title);
  }

  public showError(message: string, title: string = 'Ocorreu um erro.') {
    this.toastr.error(message);
  }

  public showInfo(message: string) {
    this.toastr.info(message);
  }

  public showWarning(message: string) {
    this.toastr.info(message);
  }

  public onApiError(error: any) {
    let errorMessage;

    if(error?.error?.message || error?.message) {
      errorMessage = error?.error?.message || error?.message || 'Não foi possível completar a operação.';
    }

    if(error?.error && error?.error?.erros && error?.error?.erros?.length > 0) {
      errorMessage = error?.error?.erros.join("\n");
    }

    this.showError(errorMessage);
  }

  constructor() { }
}
