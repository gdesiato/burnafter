import { Routes } from '@angular/router';
import { CreateComponent } from './pages/create/create';
import { ViewComponent } from './pages/view/view';

export const routes: Routes = [
  { path: '', component: CreateComponent },
  { path: 'p/:id', component: ViewComponent },
  { path: '**', redirectTo: '' }
];
