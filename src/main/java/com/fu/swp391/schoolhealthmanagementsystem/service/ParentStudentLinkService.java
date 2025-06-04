package com.fu.swp391.schoolhealthmanagementsystem.service;

import com.fu.swp391.schoolhealthmanagementsystem.dto.parent.LinkStudentRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.ParentStudentLink;
import com.fu.swp391.schoolhealthmanagementsystem.entity.Student;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.LinkStatus;
import com.fu.swp391.schoolhealthmanagementsystem.exception.AppException;
import com.fu.swp391.schoolhealthmanagementsystem.repository.ParentStudentLinkRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.StudentRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParentStudentLinkService {

    private final ParentStudentLinkRepository parentStudentLinkRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository; // To fetch current user

    @Transactional
    public ParentStudentLink linkParentToStudentByInvitation(LinkStudentRequestDto dto) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User parent = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "Không tìm thấy thông tin phụ huynh hiện tại."));

        log.info("Phụ huynh {} đang cố gắng liên kết với học sinh bằng mã mời: {}", parent.getEmail(), dto.getInvitationCode());

        Student student = studentRepository.findByInvitationCode(dto.getInvitationCode())
                .orElseThrow(() -> {
                    log.warn("Mã mời {} không hợp lệ hoặc không tìm thấy học sinh.", dto.getInvitationCode());
                    return new AppException(HttpStatus.NOT_FOUND, "Mã mời không hợp lệ hoặc không tìm thấy học sinh.");
                });

        // Kiểm tra xem phụ huynh này đã liên kết với học sinh này chưa
        boolean alreadyLinked = parentStudentLinkRepository.existsByParentAndStudent(parent, student);
        if (alreadyLinked) {
            log.info("Phụ huynh {} đã liên kết với học sinh {} (Mã: {}) rồi.", parent.getEmail(), student.getFullName(), student.getStudentCode());
            // You might want to return the existing link or throw an exception depending on desired behavior
            // For now, let's throw an exception to prevent duplicate active links via invitation.
            // If status could be PENDING/REJECTED and they retry, different logic might apply.
            ParentStudentLink existingLink = parentStudentLinkRepository.findByParentAndStudent(parent, student)
                    .orElseThrow(() -> new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Liên kết tồn tại nhưng không thể truy xuất."));
            if(existingLink.getStatus() == LinkStatus.ACTIVE){
                throw new AppException(HttpStatus.CONFLICT, "Bạn đã liên kết với học sinh này rồi.");
            }
            // If existing link is PENDING/REJECTED, maybe allow update or new ACTIVE link
            // For simplicity with invitation codes, we assume a new link means a fresh ACTIVE one.
            // Or, update existing if PENDING:
            // existingLink.setRelationshipType(dto.getRelationshipType());
            // existingLink.setStatus(LinkStatus.ACTIVE);
            // return parentStudentLinkRepository.save(existingLink);
        }


        ParentStudentLink link = new ParentStudentLink();
        link.setParent(parent);
        link.setStudent(student);
        link.setRelationshipType(dto.getRelationshipType());
        link.setStatus(LinkStatus.ACTIVE); // Mã mời -> ACTIVE ngay

        ParentStudentLink savedLink = parentStudentLinkRepository.save(link);
        log.info("Phụ huynh {} đã liên kết thành công với học sinh {} (Mã: {}) với vai trò {}. Trạng thái: ACTIVE",
                parent.getEmail(), student.getFullName(), student.getStudentCode(), dto.getRelationshipType());
        return savedLink;
    }
}