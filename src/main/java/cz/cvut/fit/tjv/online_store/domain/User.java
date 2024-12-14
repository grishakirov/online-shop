package cz.cvut.fit.tjv.online_store.domain;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_account")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder.Default
    private Boolean isActive = true;

    public User(Long l, String name, String surname, String mail, String password, LocalDate birthDate) {
        this.id = l;
        this.name = name;
        this.surname = surname;
        this.email = mail;
        this.password = password;
        this.birthDate = birthDate;
    }

    @PrePersist
    public void setDefaults() {
        if (isActive == null) {
            isActive = true;
        }
    }
}