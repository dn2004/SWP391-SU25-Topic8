package com.fu.swp391.schoolhealthmanagementsystem.exception;

import com.fu.swp391.schoolhealthmanagementsystem.dto.ErrorResponseDto;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponseDto> handleAppException(AppException ex, HttpServletRequest request) {
        log.error("Lỗi nghiệp vụ (AppException): {} - {}", ex.getStatus(), ex.getMessage(), ex);
        ErrorResponseDto errorResponse = ErrorResponseDto.of(
                ex.getStatus(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, List<String>> errors = new HashMap<>();

        // Xử lý lỗi cấp độ field
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            errors.computeIfAbsent(fieldName, k -> new ArrayList<>()).add(errorMessage);
        });
        // Xử lý lỗi cấp độ class/object
        ex.getBindingResult().getGlobalErrors().forEach(error -> {
            String objectName = "dto"; // hoặc có thể dùng error.getObjectName()
            String errorMessage = error.getDefaultMessage();
            errors.computeIfAbsent(objectName, k -> new ArrayList<>()).add(errorMessage);
        });
        log.warn("Lỗi xác thực đầu vào (MethodArgumentNotValidException): {}", errors);
        ErrorResponseDto errorResponse = ErrorResponseDto.of(
                HttpStatus.BAD_REQUEST,
                "Dữ liệu đầu vào không hợp lệ",
                request.getRequestURI(),
                errors
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, List<String>> errors = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String propertyPath = violation.getPropertyPath().toString();
            // Clean up property path (e.g., "methodName.argName.fieldName" -> "fieldName")
            String fieldName = propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
            String message = violation.getMessage();
            errors.computeIfAbsent(fieldName, k -> new ArrayList<>()).add(message);
        }
        log.warn("Lỗi ràng buộc (ConstraintViolationException): {}", errors);
        ErrorResponseDto errorResponse = ErrorResponseDto.of(
                HttpStatus.BAD_REQUEST,
                "Dữ liệu đầu vào không hợp lệ",
                request.getRequestURI(),
                errors
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Lỗi phân tích cú pháp yêu cầu (HttpMessageNotReadableException) cho {}: {}", request.getRequestURI(), ex.getMessage());

        String errorMessage;

        // Check for the specific "missing body" case first
        if (ex.getMessage() != null && ex.getMessage().contains("Required request body is missing")) {
            errorMessage = "Nội dung yêu cầu (request body) không được để trống.";
        } else {
            Throwable cause = ex.getCause();
            if (cause instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException) {
                com.fasterxml.jackson.databind.exc.InvalidFormatException ife = (com.fasterxml.jackson.databind.exc.InvalidFormatException) cause;
                String fieldName = ife.getPath().stream()
                        .map(com.fasterxml.jackson.databind.JsonMappingException.Reference::getFieldName)
                        .collect(java.util.stream.Collectors.joining("."));
                errorMessage = String.format("Giá trị '%s' không hợp lệ cho trường '%s'. Vui lòng kiểm tra lại các giá trị được chấp nhận.", ife.getValue(), fieldName);
            } else if (cause instanceof com.fasterxml.jackson.databind.exc.MismatchedInputException) {
                com.fasterxml.jackson.databind.exc.MismatchedInputException mie = (com.fasterxml.jackson.databind.exc.MismatchedInputException) cause;
                String fieldName = mie.getPath().stream()
                        .map(com.fasterxml.jackson.databind.JsonMappingException.Reference::getFieldName)
                        .collect(java.util.stream.Collectors.joining("."));
                errorMessage = String.format("Định dạng dữ liệu không đúng cho trường '%s'.", fieldName);
            } else {
                errorMessage = "Yêu cầu không hợp lệ. Vui lòng kiểm tra lại định dạng JSON.";
            }
        }

        ErrorResponseDto errorResponse = ErrorResponseDto.of(
                HttpStatus.BAD_REQUEST,
                errorMessage,
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Từ chối truy cập (AccessDeniedException): Người dùng không có quyền truy cập tài nguyên '{}'. Lý do: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponseDto errorResponse = ErrorResponseDto.of(
                HttpStatus.FORBIDDEN,
                "Bạn không có quyền truy cập tài nguyên này.",
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationException.class) // Bao gồm BadCredentialsException
    public ResponseEntity<ErrorResponseDto> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        log.warn("Lỗi xác thực (AuthenticationException) cho đường dẫn '{}': {}", request.getRequestURI(), ex.getMessage());
        String message = "Thông tin đăng nhập không chính xác hoặc tài khoản không hợp lệ.";
        if (ex instanceof BadCredentialsException) {
            message = "Email hoặc mật khẩu không chính xác.";
        }
        ErrorResponseDto errorResponse = ErrorResponseDto.of(
                HttpStatus.UNAUTHORIZED,
                message,
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }


    // Xử lý lỗi gửi mail
    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<ErrorResponseDto> handleMessagingException(jakarta.mail.MessagingException ex, HttpServletRequest request) {
        log.error("Lỗi gửi email: {}", ex.getMessage(), ex);
        ErrorResponseDto errorResponse = ErrorResponseDto.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Không thể gửi email. Vui lòng thử lại sau hoặc liên hệ quản trị viên.",
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Lỗi không xác định (Exception): {}", ex.getMessage(), ex);
        ErrorResponseDto errorResponse = ErrorResponseDto.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Đã xảy ra lỗi không mong muốn. Vui lòng thử lại sau.",
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ErrorResponseDto> handleFileStorageException(FileStorageException ex, HttpServletRequest request) {
        log.error("Lỗi xử lý file: {}", ex.getMessage(), ex);
        ErrorResponseDto errorResponse = ErrorResponseDto.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Lỗi xử lý file: " + ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Không tìm thấy tài nguyên: {}", ex.getMessage());
        ErrorResponseDto errorResponse = ErrorResponseDto.of(
                HttpStatus.NOT_FOUND,
                "Không tìm thấy tài nguyên: " + ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Đối số không hợp lệ (IllegalArgumentException): {} tại {}", ex.getMessage(), request.getRequestURI());
        ErrorResponseDto errorResponse = ErrorResponseDto.of(
                HttpStatus.BAD_REQUEST,
                "Yêu cầu không hợp lệ: " + ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalStateException(IllegalStateException ex, HttpServletRequest request) {
        log.error("Trạng thái không hợp lệ (IllegalStateException): {} tại {}", ex.getMessage(), request.getRequestURI());
        ErrorResponseDto errorResponse = ErrorResponseDto.of(
                HttpStatus.BAD_REQUEST,
                "Trạng thái không hợp lệ: " + ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}