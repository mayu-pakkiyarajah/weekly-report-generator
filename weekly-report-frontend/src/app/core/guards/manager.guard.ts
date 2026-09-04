import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/** Client-side convenience only - every manager endpoint is also enforced server-side. */
export const managerGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.isAuthenticated() && auth.isManager()) return true;

  router.navigate(auth.isAuthenticated() ? ['/reports'] : ['/login']);
  return false;
};
