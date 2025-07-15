package gift.exception;

public enum ErrorStatus {
    INTERNAL_SERVER_ERROR(-1, "Internal Server Error"),
    ENTITY_NOT_FOUND(2, "Entity Not Found"),
    ENTITY_ALREADY_EXISTS(3, "Entity Already Exists"),
    MD_APPROVAL_REQUIRED(4, "MD Approval Required"),
    VALIDATION_ERROR(-2, "Validation error"),
    REQUEST_BODY_ERROR(-3, "Request body error"),
    METHOD_NOT_ALLOWED(-4, "Method Not Allowed"),
    INVALID_CREDENTIALS(1, "Invalid Credentials");

    private final int code;
    private final String defaultMessage;

    ErrorStatus(int code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public int getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
