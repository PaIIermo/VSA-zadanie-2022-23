package sk.stuba.fei.uim.vsa.pr1;

public class Project1 {

    public static void main(String[] args) {
        ThesisService ts = new ThesisService();
        ts.createStudent(1L, "Ferko", "asdasd");
        ts.createTeacher(2L, "Julius", "gfs", "afsd");
    }
}
