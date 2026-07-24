package com.secondhand.backend.exception;

import com.secondhand.backend.exception.custom.BadRequestException;
import com.secondhand.backend.exception.custom.ForbiddenException;
import com.secondhand.backend.exception.custom.ResourceNotFoundException;
import com.secondhand.backend.exception.custom.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

/**
 * Centralized REST exception handler that converts every backend error into a consistent JSON body with a proper HTTP status code (400/401/403/404/405/409/413/415/500) and logs unexpected failures.
 * <p>
 * This class is part of the centralized error-handling mechanism, ensuring that every error response has the same structure and a clear message.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
@RestControllerAdvice // کلاس سراسری که روی همه ی کنترلر ها نظارت داره و برگردوندن ان به صورت JSON
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Builds the standard error response body containing the message, status code, status title and request path.
     *
     * @param message the message text
     * @param status the status value
     * @param request request body received from the client
     * @return an HTTP response containing the operation result and a proper status code
     */
    private ResponseEntity<ErrorResponse> buildErrorResponse(
            String message,
            HttpStatus status,
            HttpServletRequest request
    ) {
        ErrorResponse error = new ErrorResponse(
                message,
                status.value(),
                status.getReasonPhrase(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(error, status);
    }

    // 400 Bad Request
    /**
     * Handles bad request.
     *
     * @param ex the thrown exception
     * @param request request body received from the client
     * @return an HTTP response containing the operation result and a proper status code
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            BadRequestException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    // 401 Unauthorized
    /**
     * Handles unauthorized.
     *
     * @param ex the thrown exception
     * @param request request body received from the client
     * @return an HTTP response containing the operation result and a proper status code
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            UnauthorizedException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED, request);
    }

    // 401 - خطاهای احراز هویت که از Spring Security می‌آیند
    /**
     * Handles security authentication.
     *
     * @param ex the thrown exception
     * @param request request body received from the client
     * @return an HTTP response containing the operation result and a proper status code
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleSecurityAuthentication(
            AuthenticationException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                "برای انجام این عملیات باید وارد حساب کاربری شوید.",
                HttpStatus.UNAUTHORIZED,
                request
        );
    }

    // 403 - دسترسی احراز شده ولی مجاز نیست
    /**
     * Handles security access denied.
     *
     * @param ex the thrown exception
     * @param request request body received from the client
     * @return an HTTP response containing the operation result and a proper status code
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleSecurityAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                "شما اجازه انجام این عملیات را ندارید.",
                HttpStatus.FORBIDDEN,
                request
        );
    }

    // 403 Forbidden
    /**
     * Handles forbidden.
     *
     * @param ex the thrown exception
     * @param request request body received from the client
     * @return an HTTP response containing the operation result and a proper status code
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(
            ForbiddenException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN, request);
    }

    // 404 Not Found
    /**
     * Handles not found.
     *
     * @param ex the thrown exception
     * @param request request body received from the client
     * @return an HTTP response containing the operation result and a proper status code
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND, request);
    }

    // 409 Conflict - برای خطاهای منطقی تکراری که دستی throw شده‌اند
    /**
     * Handles conflict.
     *
     * @param ex the thrown exception
     * @param request request body received from the client
     * @return an HTTP response containing the operation result and a proper status code
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            IllegalStateException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT, request);
    }

    // 409 Conflict - جلوگیری از تکمیل هم‌زمان خرید یک آگهی
    /**
     * Handles optimistic locking failure.
     *
     * @param ex the thrown exception
     * @param request request body received from the client
     * @return an HTTP response containing the operation result and a proper status code
     */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailure(
            OptimisticLockingFailureException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                "این آگهی هم‌زمان توسط کاربر دیگری تغییر کرده است. لطفاً دوباره وضعیت آگهی را بررسی کنید.",
                HttpStatus.CONFLICT,
                request
        );
    }

    // 409 Conflict - برای خطاهای constraint دیتابیس
    /**
     * Handles data integrity violation.
     *
     * @param ex the thrown exception
     * @param request request body received from the client
     * @return an HTTP response containing the operation result and a proper status code
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        String cause = ex.getMostSpecificCause() != null && ex.getMostSpecificCause().getMessage() != null
                ? ex.getMostSpecificCause().getMessage().toLowerCase()
                : "";

        String message;
        if (cause.contains("username")) {
            message = "نام کاربری تکراری است!";
        } else if (cause.contains("email")) {
            message = "ایمیل تکراری است!";
        } else if (cause.contains("phone_number") || cause.contains("phonenumber")) {
            message = "شماره تلفن تکراری است!";
        } else {
            message = "داده تکراری یا نامعتبر است و با محدودیت‌های پایگاه داده سازگار نیست.";
        }

        return buildErrorResponse(message, HttpStatus.CONFLICT, request);
    }

    // 400 - وقتی JSON خراب یا body نامعتبر ارسال شود
    /**
     * Handles unreadable message.
     *
     * @param ex the thrown exception
     * @param request request body received from the client
     * @return an HTTP response containing the operation result and a proper status code
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableMessage(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                "بدنه درخواست نامعتبر است یا قالب JSON درست نیست.",
                HttpStatus.BAD_REQUEST,
                request
        );
    }

    // 400 - وقتی پارامتر اجباری ارسال نشده باشد
    /**
     * Handles missing request parameter.
     *
     * @param ex the thrown exception
     * @param request request body received from the client
     * @return an HTTP response containing the operation result and a proper status code
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestParameter(
            MissingServletRequestParameterException ex,
            HttpServletRequest request
    ) {
        String message = "پارامتر اجباری '" + ex.getParameterName() + "' ارسال نشده است.";
        return buildErrorResponse(message, HttpStatus.BAD_REQUEST, request);
    }

    // 400 - وقتی نوع پارامتر اشتباه باشد، مثلا id باید عدد باشد ولی متن ارسال شده
    /**
     * Handles type mismatch.
     *
     * @param ex the thrown exception
     * @param request request body received from the client
     * @return an HTTP response containing the operation result and a proper status code
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        String requiredType = ex.getRequiredType() != null
                ? ex.getRequiredType().getSimpleName()
                : "نوع معتبر";

        String message = "مقدار پارامتر '" + ex.getName() + "' نامعتبر است. نوع مورد انتظار: " + requiredType;
        return buildErrorResponse(message, HttpStatus.BAD_REQUEST, request);
    }

    // 400 - برای validationهای @Valid در DTOها
    /**
     * Handles validation errors.
     *
     * @param ex the thrown exception
     * @param request request body received from the client
     * @return an HTTP response containing the operation result and a proper status code
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(" | "));

        if (message.isBlank()) {
            message = "داده‌های ورودی معتبر نیستند.";
        }

        return buildErrorResponse(message, HttpStatus.BAD_REQUEST, request);
    }

    // 405 Method Not Allowed
    /**
     * Handles method not supported.
     *
     * @param ex the thrown exception
     * @param request request body received from the client
     * @return an HTTP response containing the operation result and a proper status code
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request
    ) {
        String message = "متد HTTP '" + ex.getMethod() + "' برای این مسیر پشتیبانی نمی‌شود.";
        return buildErrorResponse(message, HttpStatus.METHOD_NOT_ALLOWED, request);
    }

    // 415 Unsupported Media Type
    /**
     * Handles unsupported media type.
     *
     * @param ex the thrown exception
     * @param request request body received from the client
     * @return an HTTP response containing the operation result and a proper status code
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                "نوع محتوای ارسال‌شده پشتیبانی نمی‌شود.",
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                request
        );
    }

    // 413 Payload Too Large
    /**
     * Handles max upload size exceeded.
     *
     * @param ex the thrown exception
     * @param request request body received from the client
     * @return an HTTP response containing the operation result and a proper status code
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                "حجم فایل ارسال‌شده بیشتر از حد مجاز است.",
                HttpStatus.PAYLOAD_TOO_LARGE,
                request
        );
    }

    // 400 - برای validationهای سطح پارامتر (@Validated روی کنترلر)
    /**
     * Handles constraint violation.
     *
     * @param ex the thrown exception
     * @param request request body received from the client
     * @return an HTTP response containing the operation result and a proper status code
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        String message = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(" | "));
        if (message.isBlank()) message = "داده‌های ورودی معتبر نیستند.";
        return buildErrorResponse(message, HttpStatus.BAD_REQUEST, request);
    }

    // 404 - وقتی مسیر (endpoint) درخواست‌شده اصلاً وجود ندارد
    /**
     * Handles no resource found.
     *
     * @param ex the thrown exception
     * @param request request body received from the client
     * @return an HTTP response containing the operation result and a proper status code
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            NoResourceFoundException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse("مسیر درخواست‌شده یافت نشد.", HttpStatus.NOT_FOUND, request);
    }

    // 500 Internal Server Error
    /**
     * Handles all unexpected errors; logs the full exception on the server and returns a 500 response with a generic message without exposing technical details.
     *
     * @param ex the thrown exception
     * @param request request body received from the client
     * @return an HTTP response containing the operation result and a proper status code
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {
        // لاگ کامل خطا برای دیباگ — بدون افشای جزئیات فنی به کلاینت
        logger.error("خطای پیش‌بینی‌نشده در مسیر {}", request.getRequestURI(), ex);
        return buildErrorResponse(
                "خطای داخلی سرور رخ داده است.",
                HttpStatus.INTERNAL_SERVER_ERROR,
                request
        );
    }
}