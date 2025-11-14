import {inject, Injectable} from '@angular/core';
import {ToastrService} from "ngx-toastr";

type ToastOptions = Partial<import('ngx-toastr').IndividualConfig>;

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private toastr = inject(ToastrService);

  public showSuccess(message: string, title: string = "Sucesso!" ) {
    this.toastr.success(message, title);
  }

  public showError(message: string, title: string = 'Ocorreu um erro.') {
    const options: ToastOptions = {
      timeOut: 15000,
      closeButton: true,
      tapToDismiss: false,
      toastClass: 'ngx-toastr wide-toast'
    }

    this.toastr.error(message, title, options);
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

    this.showError(errorMessage);
  }

  constructor() { }
}
