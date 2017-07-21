package ru.iris.facade.model.status;

public class ErrorStatus {

    private String text;

    public ErrorStatus(String text) {
        this.text = text;
    }

    public String getStatus() {
        return "ERROR";
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
