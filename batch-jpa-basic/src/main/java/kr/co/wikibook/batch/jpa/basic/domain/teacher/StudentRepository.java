package kr.co.wikibook.batch.jpa.basic.domain.teacher;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
}
