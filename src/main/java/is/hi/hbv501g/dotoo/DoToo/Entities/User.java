package is.hi.hbv501g.dotoo.DoToo.Entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
public class User {

    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    public String username;

    public String name;
    @NotNull
    public String password;

    @OneToMany(mappedBy = "user")
    private List<TodoList> todoLists = new ArrayList<>();

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public User(String username, String name, @NotNull String password) {
        this.username = username;
        this.name = name;
        this.password = password;
    }

    public User() {
    }
}
