package sk.stuba.fei.uim.vsa.pr1.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@Entity
@NamedQuery(name = Student.FIND_ALL, query = "select s from Student s")
@NamedQuery(name = Student.FIND_BY_ID, query = "select s from Student s where s.id = :id")
public class Student implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique=true)
    private String email;

    private String name;
    public static final String FIND_ALL = "Student.findAll";
    public static final String FIND_BY_ID = "Student.findById";
    private Integer year;
    private Integer semester;
    private String program;

    @OneToOne(mappedBy = "student")
    private Thesis thesis;

    @Override
    public String toString() {
        String result =  "Student{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", year=" + year +
                ", semester=" + semester +
                ", program='" + program + '\'';
        if (thesis != null) {
            result += ", thesis=" + thesis.getTitle();
        }
        result += '}';

        return result;
    }
}
