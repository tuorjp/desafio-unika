import {AbstractControl, ValidationErrors, ValidatorFn} from "@angular/forms";

export function snOuNumberValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {

    const value = control.value;

    if (value === null || value === '' || (typeof value === 'string' && value.trim() === '')) {
      return { 'required': true };
    }

    const valueStr = String(value).trim();

    const isNumeric = /^\d+$/.test(valueStr);

    const isSN = valueStr.toLowerCase() === 's/n';

    if (isNumeric || isSN) {
      return null;
    }

    return { 'invalidNumero': 'O valor deve ser um número ou "s/n".' };
  };
}
