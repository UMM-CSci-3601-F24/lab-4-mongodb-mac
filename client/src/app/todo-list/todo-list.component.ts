import { Component, inject, signal, computed} from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TodoService } from '../todo.service';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { catchError, combineLatest, of, switchMap, tap } from 'rxjs';
import { Todo } from './todo';

@Component({
  selector: 'app-todo-list',
  standalone: true,
  imports: [],
  templateUrl: './todo-list.component.html',
  styleUrl: './todo-list.component.scss',
})
export class TodoListComponent {
  private todoService = inject(TodoService);
  private snackBar = inject(MatSnackBar);

  todoOwner = signal<string | undefined>(undefined);
  todoStatus = signal<boolean | undefined>(undefined);
  todoBody = signal<string | undefined>(undefined);
  todoCategory = signal<string | undefined>(undefined);

  viewType = signal<'card' | 'list'>('card');

  errMsg = signal<string | undefined>(undefined);

  private todoOwner$ = toObservable(this.todoOwner);
  private todoCategory$ = toObservable(this.todoCategory);

  serverFilteredTodos =

  toSignal(
    combineLatest([this.todoOwner$, this.todoCategory$]).pipe(
      // `switchMap` maps from one observable to another. In this case, we're taking `role` and `age` and passing
      // them as arguments to `userService.getUsers()`, which then returns a new observable that contains the
      // results.
      switchMap(([owner, category]) =>
        this.todoService.getTodos({
          owner,
          category,
        })
      ),
      catchError((err) => {
        if (err.error instanceof ErrorEvent) {
          this.errMsg.set(
            `Problem in the client – Error: ${err.error.message}`
          );
        } else {
          this.errMsg.set(
            `Problem contacting the server – Error Code: ${err.status}\nMessage: ${err.message}`
          );
        }
        this.snackBar.open(this.errMsg(), 'OK', { duration: 6000 });
        // `catchError` needs to return the same type. `of` makes an observable of the same type, and makes the array still empty
        return of<Todo[]>([]);
      }),
      // Tap allows you to perform side effects if necessary
      tap(() => {
        // A common side effect is printing to the console.
        // You don't want to leave code like this in the
        // production system, but it can be useful in debugging.
        // console.log('Users were filtered on the server')
      })
    )
    filteredTodos = computed(() => {
      const serverFilteredTodos = this.serverFilteredTodos();
      return this.todoService.filterTodos(serverFilteredTodos, {
        owner: this.todoOwner(),
        category: this.todoCategory(),
      });
    });
}
