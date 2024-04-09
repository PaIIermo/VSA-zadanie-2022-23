package sk.stuba.fei.uim.vsa.pr1.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import sk.stuba.fei.uim.vsa.pr1.ThesisStatus;
import sk.stuba.fei.uim.vsa.pr1.ThesisType;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@Entity
@NamedQuery(name = Thesis.FIND_ALL, query = "select t from Thesis t")
@NamedQuery(name = Thesis.FIND_BY_ID, query = "select t from Thesis t where t.id = :id")
@NamedQuery(name = Thesis.FIND_BY_STUDENT, query = "select t from Thesis t where t.student = :student")
@NamedQuery(name = Thesis.FIND_BY_SUPERVISOR, query = "select t from Thesis t where t.supervisor = :teacher")
public class Thesis implements Serializable {
    public static final String FIND_ALL = "Thesis.findAll";
    public static final String FIND_BY_ID = "Thesis.findById";
    public static final String FIND_BY_STUDENT = "Thesis.findByStudent";
    public static final String FIND_BY_SUPERVISOR = "Thesis.findBySupervisor";
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String registrationNumber;
    private String title;
    private String description;
    private String institute;
    @ManyToOne(optional = false)
    private Teacher supervisor;
    @OneToOne
    private Student student;
    @Enumerated(EnumType.STRING)
    private ThesisType type;
    @Enumerated(EnumType.STRING)
    private ThesisStatus status;
    @Temporal(TemporalType.DATE)
    private Date publicationDate;

    @Temporal(TemporalType.DATE)
    private Date deadlineDate;

    @Override
    public String toString() {
        return "Thesis{" +
                "id=" + id +
                ", registrationNumber='" + registrationNumber + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", institute='" + institute + '\'' +
                ", supervisor=" + supervisor +
                ", student=" + student +
                ", type=" + type +
                ", status=" + status +
                ", publicationDate=" + publicationDate +
                ", deadlineDate=" + deadlineDate +
                '}';
    }
}
