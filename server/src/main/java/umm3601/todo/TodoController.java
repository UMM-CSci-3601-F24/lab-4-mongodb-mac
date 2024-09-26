package umm3601.todo;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.mongojack.JacksonMongoCollection;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.DeleteResult;

import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import umm3601.user.UserByCompany;
import umm3601.Controller;


public class TodoController implements Controller {

  private final JacksonMongoCollection<Todo> todoCollection;
  static final String OWNER_KEY = "age";
  static final String STATUS_KEY = "company";
  static final String BODY_KEY = "role";

  private static final String API_TODOS = "/api/todos";
  private static final String API_TODO_BY_ID = "/api/todo/{id}";
  private static final String BODY_REGEX = "^(admin|editor|viewer)$";
  public static final String CATEGORY_REGEX = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
 /**
   * Construct a controller for Todos.
   *
   * @param database the database containing todo data
   */
  public TodoController(MongoDatabase database) {
    todoCollection = JacksonMongoCollection.builder().build(
        database,
        "todos",
        Todo.class,
        UuidRepresentation.STANDARD);
  }

  public void getTodo(Context ctx) {
    String id = ctx.pathParam("id");
    Todo todo;

    try {
      todo = todoCollection.find(eq("_id", new ObjectId(id))).first();
    } catch (IllegalArgumentException e) {
      throw new BadRequestResponse("The requested todo id wasn't a legal Mongo Object ID.");
    }
    if (todo == null) {
      throw new NotFoundResponse("The requested todo was not found");
    } else {
      ctx.json(todo);
      ctx.status(HttpStatus.OK);
    }
  }

  /**
   * Set the JSON body of the response to be a list of all the users returned from
   * the databasestatic final String AGE_KEY = "age";
  static final String COMPANY_KEY = "company";
  static final String ROLE_KEY = "role";
  static final String SORT_ORDER_KEY = "sortorder";
   * that match any requested filters and ordering
   *
   * @param ctx a Javalin HTTP context
   */
  public void getTodos(Context ctx) {
    Bson combinedFilter = constructFilter(ctx);
    Bson sortingOrder = constructSortingOrder(ctx);


ArrayList<Todo> matchingTodos = todoCollection
        .find(combinedFilter)
        .sort(sortingOrder)
        .into(new ArrayList<>());
        ctx.json(matchingTodos);

        ctx.status(HttpStatus.OK);

  }
  /**
   * Construct a Bson filter document to use in the `find` method based on the
   * query parameters from the context.
   *
   * This checks for the presence of the `age`, `company`, and `role` query
   * parameters and constructs a filter document that will match users with
   * the specified values for those fields.
   *
   * @param ctx a Javalin HTTP context, which contains the query parameters
   *            used to construct the filter
   * @return a Bson filter document that can be used in the `find` method
   *         to filter the database collection of users
   */
  private Bson constructFilter(Context ctx) {
    List<Bson> filters = new ArrayList<>(); // start with an empty list of filters

    if (ctx.queryParamMap().containsKey(OWNER_KEY)) {
      Pattern pattern = Pattern.compile(Pattern.quote(ctx.queryParam(OWNER_KEY)), Pattern.CASE_INSENSITIVE);
      filters.add(regex(OWNER_KEY, pattern));
    }
    if (ctx.queryParamMap().containsKey(STATUS_KEY)) {
      Pattern pattern = Pattern.compile(Pattern.quote(ctx.queryParam(STATUS_KEY)), Pattern.CASE_INSENSITIVE);
      filters.add(regex(STATUS_KEY, pattern));
    }
    if (ctx.queryParamMap().containsKey(BODY_KEY)) {
      String role = ctx.queryParamAsClass(BODY_KEY, String.class)
          .check(it -> it.matches(BODY_REGEX), "todo must have a legal todo body")
          .get();
      filters.add(eq(BODY_KEY, role));
    }

    // Combine the list of filters into a single filtering document.
    Bson combinedFilter = filters.isEmpty() ? new Document() : and(filters);

    return combinedFilter;
  }

  /**
   * Construct a Bson sorting document to use in the `sort` method based on the
   * query parameters from the context.
   *
   * This checks for the presence of the `sortby` and `sortorder` query
   * parameters and constructs a sorting document that will sort users by
   * the specified field in the specified order. If the `sortby` query
   * parameter is not present, it defaults to "name". If the `sortorder`
   * query parameter is not present, it defaults to "asc".
   *
   * @param ctx a Javalin HTTP context, which contains the query parameters
   *            used to construct the sorting order
   * @return a Bson sorting document that can be used in the `sort` method
   *         to sort the database collection of users
   */
  private Bson constructSortingOrder(Context ctx) {
    // Sort the results. Use the `sortby` query param (default "name")
    // as the field to sort by, and the query param `sortorder` (default
    // "asc") to specify the sort order.
    String sortBy = Objects.requireNonNullElse(ctx.queryParam("sortby"), "name");
    String sortOrder = Objects.requireNonNullElse(ctx.queryParam("sortorder"), "asc");
    Bson sortingOrder = sortOrder.equals("desc") ? Sorts.descending(sortBy) : Sorts.ascending(sortBy);
    return sortingOrder;
  }

  /**
   * Add a new user using information from the context
   * (as long as the information gives "legal" values to User fields)
   *
   * @param ctx a Javalin HTTP context that provides the user info
   *            in the JSON body of the request
   */
  public void addNewTodo(Context ctx) {

    String body = ctx.body();
    Todo newTodo = ctx.bodyValidator(Todo.class)
        .check(usr -> usr.owner != null && usr.owner.length() > 0,
            "User must have a non-empty owner; body was " + body)
        .check(usr -> usr.category.matches(CATEGORY_REGEX),
            "User must have a legal category; body was " + body)
        .check(usr -> usr.body.matches(BODY_REGEX),
            "User must have a legal body; body was " + body)
        .get();

    // Generate a user avatar (you won't need this part for todos)


    // Insert the new user into the database
    todoCollection.insertOne(newTodo);

    // Set the JSON response to be the `_id` of the newly created user.
    // This gives the client the opportunity to know the ID of the new user,
    // which it can use to perform further operations (e.g., display the user).
    ctx.json(Map.of("id", newTodo._id));
    // 201 (`HttpStatus.CREATED`) is the HTTP code for when we successfully
    // create a new resource (a user in this case).
    // See, e.g., https://developer.mozilla.org/en-US/docs/Web/HTTP/Status
    // for a description of the various response codes.
    ctx.status(HttpStatus.CREATED);
  }
    /**
   * Delete the user specified by the `id` parameter in the request.
   *
   * @param ctx a Javalin HTTP context
   */
  public void deleteUser(Context ctx) {
    String id = ctx.pathParam("id");
    DeleteResult deleteResult = todoCollection.deleteOne(eq("_id", new ObjectId(id)));
    // We should have deleted 1 or 0 users, depending on whether `id` is a valid
    // user ID.
    if (deleteResult.getDeletedCount() != 1) {
      ctx.status(HttpStatus.NOT_FOUND);
      throw new NotFoundResponse(
          "Was unable to delete ID "
              + id
              + "; perhaps illegal ID or an ID for an item not in the system?");
    }
    ctx.status(HttpStatus.OK);
  }
  /**
   * Setup routes for the `user` collection endpoints.
   *
   * These endpoints are:
   * - `GET /api/users/:id`
   * - Get the specified user
   * - `GET /api/users?age=NUMBER&company=STRING&name=STRING`
   * - List users, filtered using query parameters
   * - `age`, `company`, and `name` are optional query parameters
   * - `GET /api/usersByCompany`
   * - Get user names and IDs, possibly filtered, grouped by company
   * - `DELETE /api/users/:id`
   * - Delete the specified user
   * - `POST /api/users`
   * - Create a new user
   * - The user info is in the JSON body of the HTTP request
   *
   * GROUPS SHOULD CREATE THEIR OWN CONTROLLERS THAT IMPLEMENT THE
   * `Controller` INTERFACE FOR WHATEVER DATA THEY'RE WORKING WITH.
   * You'll then implement the `addRoutes` method for that controller,
   * which will set up the routes for that data. The `Server#setupRoutes`
   * method will then call `addRoutes` for each controller, which will
   * add the routes for that controller's data.
   *
   * @param server         The Javalin server instance
   * @param Controller The controller that handles the user endpoints
   */
  public void addRoutes(Javalin server) {
    // Get the specified user
    server.get(API_TODO_BY_ID, this::getTodo);

    // List users, filtered using query parameters
    server.get(API_TODOS, this::getTodos);

    // Delete the specified user
    // server.delete(API_TODO_BY_ID, this::deleteTodo);

    // Add new user with the user info being in the JSON body
    // of the HTTP request
    server.post(API_TODOS, this::addNewTodo);
  }
}
