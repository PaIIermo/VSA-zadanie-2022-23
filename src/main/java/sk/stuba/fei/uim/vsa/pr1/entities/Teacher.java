package sk.stuba.fei.uim.vsa.pr1.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@NamedQuery(name = Teacher.FIND_ALL, query = "select t from Teacher t")
@NamedQuery(name = Teacher.FIND_BY_ID, query = "select t from Teacher t where t.id = :id")
public class Teacher implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique=true)
    private String email;

    private String name;

    public static final String FIND_ALL = "Teacher.findAll";
    public static final String FIND_BY_ID = "Teacher.findById";
    private String institute;
    private String department;

    @OneToMany(mappedBy = "supervisor")
    private List<Thesis> theses;

    @Override
    public String toString() {
        return "Teacher{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", institute='" + institute + '\'' +
                ", department='" + department + '\'' +
                '}';
    }
}
