package kr.co.wikibook.batch.jpa.basic.domain.teacher;

import java.util.List;

public class SchoolClass {
    private final long teacherId;
    private final List<Student> students;

    public SchoolClass(long teacherId, List<Student> students) {
        this.teacherId = teacherId;
        this.students = students;
    }

    @Override
    public String toString() {
        return "SchoolClass{" +
                "teacherId=" + teacherId +
                ", students.size=" + students.size() +
                '}';
    }
}
