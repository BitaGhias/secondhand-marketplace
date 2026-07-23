package com.secondhand.frontend.controller;

import com.secondhand.frontend.util.FrontendErrorHandler;

import com.secondhand.frontend.MainApplication;
import com.secondhand.frontend.service.AuthService;
import com.secondhand.frontend.util.Routes;
import com.secondhand.frontend.util.SessionManager;
import com.secondhand.frontend.util.ValidationUtil;
import com.secondhand.frontend.util.WindowUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

/**
 * JavaFX controller of the "register" screen.
 * <p>
 * This class is the JavaFX controller bound to its FXML file; it receives UI elements through the {@code @FXML} annotation, handles user events and talks to the backend through the service layer. Network calls run on a background thread and their results are applied on the UI thread via {@code Platform.runLater}.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
public class RegisterController extends BaseController {

    @FXML private TextField     fullNameField;
    @FXML private TextField     usernameField;
    @FXML private TextField     emailField;
    @FXML private TextField     phoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button        registerButton;
    @FXML private Hyperlink     loginLink;
    @FXML private Label         errorLabel;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private HBox          titleBar;

    /**
     * Initializes the controller after the FXML is loaded; wires event handlers and loads the initial data of the screen.
     */
    @FXML
    public void initialize() {
        WindowUtil.makeDraggable(titleBar);
    }

    /**
     * Handles register.
     */
    @FXML
    private void handleRegister() {
        String fullName         = fullNameField.getText().trim();
        String username         = usernameField.getText().trim();
        String email            = emailField.getText().trim();
        // FIX: تبدیل ارقام فارسی/عربی احتمالی شماره تلفن به انگلیسی قبل از اعتبارسنجی و ارسال
        String phone            = ValidationUtil.normalizeDigits(phoneField.getText().trim());
        String password         = passwordField.getText();
        String confirmPassword  = confirmPasswordField.getText();

        if (!validateForm(fullName, username, email, phone, password, confirmPassword)) return;

        setLoadingState(true);

        AuthService.register(fullName, username, email, phone, password, confirmPassword)
                .thenAccept(responseBody -> handleRegisterSuccess(username, password))
                .exceptionally(ex -> {
                    String errorMsg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    handleRegisterError(errorMsg);
                    return null;
                });
    }

    /**
     * Validates form.
     *
     * @param fullName the "full name" value of type {@code String}
     * @param username the username
     * @param email the email address
     * @param phone the "phone" value of type {@code String}
     * @param password the password
     * @param confirmPassword the "confirm password" value of type {@code String}
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    private boolean validateForm(String fullName, String username, String email,
                                 String phone, String password, String confirmPassword) {
        if (fullName.isEmpty())                              return showValidationError("نام کامل الزامی است!");
        if (!ValidationUtil.isValidFullName(fullName))       return showValidationError("نام کامل باید بین ۳ تا ۵۰ حرف باشد و فقط شامل حروف باشد!");

        if (username.isEmpty())                              return showValidationError("نام کاربری الزامی است!");
        if (!ValidationUtil.isValidUsername(username))       return showValidationError("نام کاربری باید بین ۳ تا ۲۰ کاراکتر باشد و فقط شامل حروف انگلیسی، عدد و _ باشد!");

        if (password.isEmpty())                              return showValidationError("رمز عبور الزامی است!");
        if (!ValidationUtil.isValidPassword(password, 6))    return showValidationError("رمز عبور باید حداقل ۶ کاراکتر باشد!");
        // FIX: بررسی حداکثر طول رمز عبور
        if (ValidationUtil.isPasswordTooLong(password, 100)) return showValidationError("رمز عبور نباید بیشتر از ۱۰۰ کاراکتر باشد!");
        // FIX: بررسی عدم وجود فاصله در رمز عبور
        if (ValidationUtil.containsSpace(password))          return showValidationError("رمز عبور نباید شامل فاصله باشد!");
        if (!password.equals(confirmPassword))               return showValidationError("رمز عبور و تکرار آن مطابقت ندارند!");

        if (email.isEmpty())                                 return showValidationError("ایمیل الزامی است!");
        if (!ValidationUtil.isValidEmail(email))             return showValidationError("فرمت ایمیل نامعتبر است!");

        if (phone.isEmpty())                                 return showValidationError("شماره تلفن الزامی است!");
        if (!ValidationUtil.isValidIranianPhone(phone))      return showValidationError("فرمت شماره تلفن نامعتبر است! باید با 09 شروع شود و 11 رقم باشد.");
        return true;
    }

    /**
     * Shows validation error.
     *
     * @param message the message text
     * @return {@code true} if the condition holds or the operation succeeds, {@code false} otherwise
     */
    private boolean showValidationError(String message) {
        showErrorLabel(message);
        return false;
    }

    /**
     * Handles register success.
     *
     * @param username the username
     * @param password the password
     */
    private void handleRegisterSuccess(String username, String password) {
        Platform.runLater(() -> showSuccessLabel("✅ ثبت‌نام با موفقیت انجام شد! در حال ورود به حساب شما..."));
        AuthService.login(username, password)
                .thenAccept(loginResponse -> Platform.runLater(() -> {
                    if (loginResponse != null && loginResponse.getUser() != null)
                        SessionManager.setCurrentUser(loginResponse.getUser());
                    setLoadingState(false);
                    try { MainApplication.changeScene(Routes.USER_PANEL, "دیباچه - پنل کاربر"); }
                    catch (Exception e) { FrontendErrorHandler.log(e); goToLogin(); }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> { setLoadingState(false); showSuccessLabel("✅ ثبت‌نام انجام شد! لطفاً وارد شوید."); goToLogin(); });
                    return null;
                });
    }

    /**
     * Handles register error.
     *
     * @param errorMessage the "error message" value of type {@code String}
     */
    private void handleRegisterError(String errorMessage) {
        Platform.runLater(() -> {
            setLoadingState(false);
            if (errorMessage.contains("نام کاربری تکراری") || errorMessage.contains("duplicate"))
                showErrorLabel("این نام کاربری قبلاً ثبت شده است");
            else if (errorMessage.contains("ایمیل تکراری") || errorMessage.contains("Email already"))
                showErrorLabel("این ایمیل قبلاً ثبت شده است");
            else if (errorMessage.contains("شماره تلفن تکراری") || errorMessage.contains("Phone already"))
                showErrorLabel("این شماره تلفن قبلاً ثبت شده است");
            else
                showErrorLabel("خطا در ثبت‌نام: " + errorMessage);
        });
    }

    /**
     * Sets loading state.
     *
     * @param isLoading the "is loading" value of type {@code boolean}
     */
    private void setLoadingState(boolean isLoading) {
        if (loadingIndicator != null) loadingIndicator.setVisible(isLoading);
        if (registerButton != null)   registerButton.setDisable(isLoading);
        if (isLoading && errorLabel != null) errorLabel.setVisible(false);
    }

    // ─── Label helpers (≠ BaseController — روی Label داخل فرم) ───────────────

    /**
     * Shows error label.
     *
     * @param message the message text
     */
    private void showErrorLabel(String message) {
        Platform.runLater(() -> {
            if (errorLabel != null) {
                errorLabel.setText("❌ " + message);
                errorLabel.setStyle("-fx-text-fill: #dc2626;");
                errorLabel.setVisible(true);
            }
        });
    }

    /**
     * Shows success label.
     *
     * @param message the message text
     */
    private void showSuccessLabel(String message) {
        Platform.runLater(() -> {
            if (errorLabel != null) {
                errorLabel.setText(message);
                errorLabel.setStyle("-fx-text-fill: #16a34a;");
                errorLabel.setVisible(true);
            }
        });
    }

    /**
     * Navigates to to login.
     */
    @FXML
    private void goToLogin() {
        try { MainApplication.changeScene(Routes.LOGIN, "ورود"); }
        catch (Exception e) { showErrorLabel("خطا در بارگذاری صفحه ورود"); }
    }
}