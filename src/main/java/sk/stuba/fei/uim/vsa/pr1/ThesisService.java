package sk.stuba.fei.uim.vsa.pr1;

import sk.stuba.fei.uim.vsa.pr1.entities.Student;
import sk.stuba.fei.uim.vsa.pr1.entities.Teacher;
import sk.stuba.fei.uim.vsa.pr1.entities.Thesis;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.*;

public class ThesisService extends AbstractThesisService<Student, Teacher, Thesis>{

    @Override
    public Student createStudent(Long aisId, String name, String email) {
        if (getStudent(aisId) != null) return null;

        Student s = new Student();
        s.setId(aisId);
        s.setName(name);
        s.setEmail(email);

        persist(s);
        return getStudent(s.getId());
    }

    @Override
    public Student getStudent(Long id) {
        if (id == null) throw new IllegalArgumentException();

        EntityManager em = emf.createEntityManager();
        TypedQuery<Student> q = em.createNamedQuery(Student.FIND_BY_ID, Student.class);
        q.setParameter("id", id);
        try {
            return q.getSingleResult();
        }
        catch(NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    @Override
    public Student updateStudent(Student student) {
        if (student == null || student.getId() == null) {
            throw new IllegalArgumentException();
        }

        EntityManager em = emf.createEntityManager();
        Student managedStudent = em.find(Student.class, student.getId());
        if (managedStudent == null) return null;

        em.getTransaction().begin();
        managedStudent = em.merge(student);
        em.getTransaction().commit();

        em.close();
        return managedStudent;
    }

    @Override
    public List<Student> getStudents() {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Student> q = em.createNamedQuery(Student.FIND_ALL, Student.class);

        List<Student> students = q.getResultList();
        em.close();
        if (students.isEmpty()) return new ArrayList<>();
        return students;
    }

    @Override
    public Student deleteStudent(Long id) {
        if (id == null) throw new IllegalArgumentException();

        EntityManager em = emf.createEntityManager();
        Student managedStudent = em.find(Student.class, id);
        if (managedStudent == null) return null;

        em.getTransaction().begin();
        if (managedStudent.getThesis() != null) {
            Thesis thesis = em.find(Thesis.class, managedStudent.getThesis().getId());
            thesis.setStudent(null);
            thesis.setStatus(ThesisStatus.AVAILABLE);
            em.merge(thesis);
        }
        em.remove(managedStudent);
        em.getTransaction().commit();
        em.close();

        return managedStudent;
    }

    @Override
    public Teacher createTeacher(Long aisId, String name, String email, String department) {
        if (getTeacher(aisId) != null) return null;

        Teacher t = new Teacher();
        t.setId(aisId);
        t.setName(name);
        t.setEmail(email);
        t.setInstitute(department);
        t.setDepartment(department);

        persist(t);
        return getTeacher(t.getId());
    }

    @Override
    public Teacher getTeacher(Long id) {
        if (id == null) throw new IllegalArgumentException();

        EntityManager em = emf.createEntityManager();
        TypedQuery<Teacher> q = em.createNamedQuery(Teacher.FIND_BY_ID, Teacher.class);
        q.setParameter("id", id);
        try {
            return q.getSingleResult();
        }
        catch(NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    @Override
    public Teacher updateTeacher(Teacher teacher) {
        if (teacher == null || teacher.getId() == null) {
            throw new IllegalArgumentException();
        }

        EntityManager em = emf.createEntityManager();
        Teacher managedTeacher = em.find(Teacher.class, teacher.getId());
        if (managedTeacher == null) return null;

        em.getTransaction().begin();
        managedTeacher = em.merge(teacher);
        em.getTransaction().commit();

        em.close();
        return managedTeacher;
    }

    @Override
    public List<Teacher> getTeachers() {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Teacher> q = em.createNamedQuery(Teacher.FIND_ALL, Teacher.class);

        List<Teacher> teachers = q.getResultList();
        em.close();
        if (teachers.isEmpty()) return new ArrayList<>();
        return teachers;
    }

    @Override
    public Teacher deleteTeacher(Long id) {
        if (id == null) throw new IllegalArgumentException();

        EntityManager em = emf.createEntityManager();
        Teacher managedTeacher = em.find(Teacher.class, id);
        if (managedTeacher == null) return null;

        for (Thesis i: getThesesByTeacher(id)) {
            deleteThesis(i.getId());
        }

        em.getTransaction().begin();
        em.remove(managedTeacher);
        em.getTransaction().commit();
        em.close();

        return managedTeacher;
    }

    @Override
    public Thesis makeThesisAssignment(Long supervisor, String title, String type, String description) {
        if (supervisor == null) throw new IllegalArgumentException();
        Teacher teacher = getTeacher(supervisor);
        if (teacher == null) return null;
        ThesisType thesisType;

        try {
            thesisType = ThesisType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return null;
        }

        Thesis t = new Thesis();
        t.setRegistrationNumber(generateRegistrationNumber());
        t.setSupervisor(teacher);
        t.setTitle(title);
        t.setDescription(description);
        t.setStatus(ThesisStatus.AVAILABLE);
        t.setType(thesisType);
        t.setInstitute(teacher.getInstitute());

        Date now = new Date();
        t.setPublicationDate(now);

        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.MONTH, 3);
        t.setDeadlineDate(cal.getTime());

        persist(t);
        return t;
    }

    @Override
    public Thesis assignThesis(Long thesisId, Long studentId) {
        if (thesisId == null || studentId == null) throw new IllegalArgumentException();

        Student student = getStudent(studentId);
        Thesis thesis = getThesis(thesisId);
        if (student == null || thesis == null) return null;

        if (student.getThesis() != null) return null;
        if (thesis.getStudent() != null) return null;

        if (hasValidDeadline(thesis.getDeadlineDate()) && thesis.getStatus() == ThesisStatus.AVAILABLE) {
            thesis.setStudent(student);
            student.setThesis(thesis);
            thesis.setStatus(ThesisStatus.WORKED_ON);
            updateStudent(student);
            thesis = updateThesis(thesis);
            return thesis;
        }
        throw new IllegalStateException();
    }

    @Override
    public Thesis submitThesis(Long thesisId) {
        if (thesisId == null) throw new IllegalArgumentException();

        Thesis thesis = getThesis(thesisId);
        if (thesis == null) return null;

        if (hasValidDeadline(thesis.getDeadlineDate()) && thesis.getStudent() != null && thesis.getStatus() == ThesisStatus.WORKED_ON) {
            thesis.setStatus(ThesisStatus.FINISHED);
            thesis = updateThesis(thesis);
            return thesis;
        }
        throw new IllegalStateException();
    }

    @Override
    public Thesis deleteThesis(Long id) {
        if (id == null) throw new IllegalArgumentException();

        EntityManager em = emf.createEntityManager();
        Thesis managedThesis = em.find(Thesis.class, id);
        if (managedThesis == null) return null;

        em.getTransaction().begin();
        // REVIEW
        if (managedThesis.getStudent() != null) {
            Student student = em.find(Student.class, managedThesis.getStudent().getId());
            student.setThesis(null);
            em.merge(student);
        }
        em.remove(managedThesis);
        em.getTransaction().commit();
        em.close();

        return managedThesis;
    }

    @Override
    public List<Thesis> getTheses() {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Thesis> q = em.createNamedQuery(Thesis.FIND_ALL, Thesis.class);

        List<Thesis> theses = q.getResultList();
        em.close();
        if (theses.isEmpty()) return new ArrayList<>();
        return theses;
    }

    @Override
    public List<Thesis> getThesesByTeacher(Long teacherId) {
        EntityManager em = emf.createEntityManager();

        Teacher teacher = getTeacher(teacherId);
        if (teacher == null) return new ArrayList<>();

        TypedQuery<Thesis> q = em.createNamedQuery(Thesis.FIND_BY_SUPERVISOR, Thesis.class);
        q.setParameter("teacher", teacher);

        List<Thesis> theses = q.getResultList();
        em.close();
        if (theses.isEmpty()) return new ArrayList<>();
        return theses;
    }

    @Override
    public Thesis getThesisByStudent(Long studentId) {
        if (studentId == null || getStudent(studentId) == null) return null;
        Student student = getStudent(studentId);

        EntityManager em = emf.createEntityManager();
        TypedQuery<Thesis> q = em.createNamedQuery(Thesis.FIND_BY_STUDENT, Thesis.class);
        q.setParameter("student", student);

        try {
            return q.getSingleResult();
        } catch(NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    @Override
    public Thesis getThesis(Long id) {
        if (id == null) throw new IllegalArgumentException();

        EntityManager em = emf.createEntityManager();
        TypedQuery<Thesis> q = em.createNamedQuery(Thesis.FIND_BY_ID, Thesis.class);
        q.setParameter("id", id);
        try {
            return q.getSingleResult();
        }
        catch(NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    @Override
    public Thesis updateThesis(Thesis thesis) {
        if (thesis == null || thesis.getId() == null) {
            throw new IllegalArgumentException();
        }

        EntityManager em = emf.createEntityManager();
        Thesis managedThesis = em.find(Thesis.class, thesis.getId());
        if (managedThesis == null) return null;

        em.getTransaction().begin();
        managedThesis = em.merge(thesis);
        em.getTransaction().commit();

        em.close();
        return managedThesis;
    }

    private String generateRegistrationNumber() {
        StringBuilder sb = new StringBuilder("FEI");
        for (int i = 0; i < 4; i++) {
            sb.append("-");
            sb.append(generateRandomDigits());
        }
        return sb.toString();
    }

    private String generateRandomDigits() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    public boolean hasValidDeadline(Date date) {
        Date now = new Date();
        return now.compareTo(date) <= 0;
    }
    public void persist(Object o) {
        if (o == null)
            return;
        EntityManager em = this.emf.createEntityManager();
        EntityTransaction et = em.getTransaction();

        et.begin();
        try {
            em.persist(o);
        } catch (IllegalStateException e) {
            et.rollback();
        } catch (Exception ignored) {
        } finally {
            try {
                et.commit();
            } catch (Exception ignored) {
            }
            em.close();
        }
    }
}
