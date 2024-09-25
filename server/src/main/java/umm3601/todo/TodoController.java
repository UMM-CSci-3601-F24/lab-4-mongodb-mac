package umm3601.todo;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.regex;

import static org.mockito.ArgumentMatchers.eq;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.mongojack.JacksonMongoCollection;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;



public class TodoController {
  private final JacksonMongoCollection<Todo> todoCollection;
  static final String OWNER_KEY = "age";
  static final String STATUS_KEY = "company";
  static final String BODY_KEY = "role";
  private static final String BODY_REGEX = "^(admin|editor|viewer)$";
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

}
