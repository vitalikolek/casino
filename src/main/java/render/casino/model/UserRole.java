package render.casino.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class UserRole {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  //@Column(length = 128)
  //@Size(max = 128)
  @Enumerated(EnumType.ORDINAL)
  private UserRoleType name;

  public UserRole(UserRoleType name) {
    this.name = name;
  }
}