package umm3601.todo;
import org.mongojack.Id;
import org.mongojack.ObjectId;

@SuppressWarnings({"VisibilityModifier"})
public class Todo {

  @ObjectId @Id
  @SuppressWarnings({"MemberName"})
  public String _id;

  public String owner;
  public Boolean status;
  public String body;
  public String category;

  @Override
  public int hashCode() {
    return _id.hashCode();
  }
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Todo)) {
      return false;
    }
    Todo other = (Todo) obj;
    return _id.equals(other._id);
  }
}
