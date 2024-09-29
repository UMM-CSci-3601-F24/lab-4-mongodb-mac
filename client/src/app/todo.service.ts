import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Todo, TodoOwner } from './todo-list/todo';
import { environment } from '../environments/environment';
import { map } from 'rxjs/operators';
@Injectable({
  providedIn: 'root'
})
export class TodoService {
  readonly todoUrl: string = `${environment.apiUrl}todos`;
  private readonly ownerKey = 'owner';
  private readonly statusKey = 'status';
  private readonly bodyKey = 'body';
  private readonly categoryKey = 'category';

  constructor(private httpClient: HttpClient) { }

  getTodos(filters?: { owner?: TodoOwner; status?: boolean; body?: string; category?: string}): Observable<User[]> {
    // `HttpParams` is essentially just a map used to hold key-value
    // pairs that are then encoded as "?key1=value1&key2=value2&…" in
    // the URL when we make the call to `.get()` below.
    let httpParams: HttpParams = new HttpParams();
    if (filters) {
      if (filters.owner) {
        httpParams = httpParams.set(this.ownerKey, filters.owner);
      }
      if (filters.status) {
        httpParams = httpParams.set(this.statusKey, filters.status);
      }
      if (filters.body) {
        httpParams = httpParams.set(this.bodyKey, filters.body);
      }
      if (filters.category) {
        httpParams = httpParams.set(this.categoryKey, filters.category);
    }
    // Send the HTTP GET request with the given URL and parameters.
    // That will return the desired `Observable<User[]>`.
    return this.httpClient.get<Todo[]>(this.todoUrl, {
      params: httpParams,
    });
  }
}
/**
   * Get the `User` with the specified ID.
   *
   * @param id the ID of the desired user
   * @returns an `Observable` containing the resulting user.
   */
getTodoById(id: string): Observable<Todo> {
  // The input to get could also be written as (this.userUrl + '/' + id)
  return this.httpClient.get<Todo>(`${this.todoUrl}/${id}`);
}
/**
   * A service method that filters an array of `User` using
   * the specified filters.
   *
   * Note that the filters here support partial matches. Since the
   * matching is done locally we can afford to repeatedly look for
   * partial matches instead of waiting until we have a full string
   * to match against.
   *
   * @param todos the array of `Users` that we're filtering
   * @param filters the map of key-value pairs used for the filtering
   * @returns an array of `Users` matching the given filters
   */
filterTodos(users: Todo[], filters: { owner?: string; category?: string }): Todo[] { // skip: JS-0105
  let filteredTodos = todos;

  // Filter by name
  if (filters.owner) {
    filters.owner = filters.owner.toLowerCase();
    filteredTodos = filteredTodos.filter(todo => todo.owner.toLowerCase().indexOf(filters.owner) !== -1);
  }

  // Filter by company
  if (filters.category) {
    filters.category = filters.category.toLowerCase();
    filteredTodos = filteredTodos.filter(todo => todo.category.toLowerCase().indexOf(filters.category) !== -1);
  }

  return filteredTodos;
}

addTodo(newTodo: Partial<Todo>): Observable<string> {
  // Send post request to add a new user with the user data as the body.
  return this.httpClient.post<{id: string}>(this.todoUrl, newTodo).pipe(map(res => res.id));
}
}
