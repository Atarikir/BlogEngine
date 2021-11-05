package main.model.enums;

public enum Permission {
    USER("user:write"),
    MODERATE("user:moderate");

    private final String permit;

    Permission(String permit) {
        this.permit = permit;
    }

    public String getPermit() {
        return permit;
    }
}
