import { TestBed, waitForAsync} from '@angular/core/testing';
import { HttpClient, HttpParams, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Todo } from './todo';
import { of } from 'rxjs';
import { TodoService } from '../todo.service';

describe('TodoService', () => {
  const testTodos: Todo[] = [
     {
    "_id": {
      "$oid": "58af3a600343927e48e8720f"
    },
    "owner": "Blanche",
    "status": false,
    "body": "In sunt ex non tempor cillum commodo amet incididunt anim qui commodo quis. Cillum non labore ex sint esse.",
    "category": "software design"
  },
  {
    "_id": {
      "$oid": "58af3a600343927e48e87210"
    },
    "owner": "Fry",
    "status": false,
    "body": "Ipsum esse est ullamco magna tempor anim laborum non officia deserunt veniam commodo. Aute minim incididunt ex commodo.",
    "category": "video games"
  },
  {
    "_id": {
      "$oid": "58af3a600343927e48e87211"
    },
    "owner": "Fry",
    "status": true,
    "body": "Ullamco irure laborum magna dolor non. Anim occaecat adipisicing cillum eu magna in.",
    "category": "homework"
  },
  ];
  let service: TodoService;

  let httpClient: HttpClient;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TodoService);
    imports: [],
    providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()]
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
