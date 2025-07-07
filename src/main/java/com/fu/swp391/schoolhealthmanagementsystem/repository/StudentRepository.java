package com.fu.swp391.schoolhealthmanagementsystem.repository;

import com.fu.swp391.schoolhealthmanagementsystem.entity.Student;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.Class;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ClassGroup;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {

    List<Student> findByClassGroup(ClassGroup classGroup);

    List<Student> findByClassValue(Class classValue);

    List<Student> findByClassGroupAndClassValue(ClassGroup classGroup, Class classValue);

    List<Student> findByClassGroupIn(Collection<ClassGroup> classGroups);

    List<Student> findByClassValueIn(Collection<Class> classValues);

    List<Student> findByClassGroupAndStatus(ClassGroup classGroup, StudentStatus status);

    List<Student> findByClassGroupInAndStatus(Collection<ClassGroup> classGroups, StudentStatus status);

    List<Student> findByDateOfBirthBetween(LocalDate minDate, LocalDate maxDate);

    List<Student> findByStatus(StudentStatus status);

    Optional<Student> findByInvitationCode(String invitationCode);

    Optional<Student> findByFullNameAndDateOfBirth(String fullName, LocalDate dateOfBirth);
}
