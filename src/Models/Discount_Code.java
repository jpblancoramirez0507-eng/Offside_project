package Models;

public class Discount_Code {

    private final String code;
    private final int percentage;
    private boolean active;

    public Discount_Code(String code, int percentage, boolean active) {
        this.code = code.trim().toUpperCase();
        this.percentage = percentage;
        this.active = active;
    }

    public String getCode() {
        return code;
    }

    public int getPercentage() {
        return percentage;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
