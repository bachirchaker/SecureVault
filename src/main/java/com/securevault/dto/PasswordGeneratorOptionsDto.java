package com.securevault.dto;

public class PasswordGeneratorOptionsDto {

    private int length = 16;
    private boolean uppercase = true;
    private boolean lowercase = true;
    private boolean numbers = true;
    private boolean symbols = true;
    private boolean avoidAmbiguous = false;

    public PasswordGeneratorOptionsDto() {
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public boolean isUppercase() {
        return uppercase;
    }

    public void setUppercase(boolean uppercase) {
        this.uppercase = uppercase;
    }

    public boolean isLowercase() {
        return lowercase;
    }

    public void setLowercase(boolean lowercase) {
        this.lowercase = lowercase;
    }

    public boolean isNumbers() {
        return numbers;
    }

    public void setNumbers(boolean numbers) {
        this.numbers = numbers;
    }

    public boolean isSymbols() {
        return symbols;
    }

    public void setSymbols(boolean symbols) {
        this.symbols = symbols;
    }

    public boolean isAvoidAmbiguous() {
        return avoidAmbiguous;
    }

    public void setAvoidAmbiguous(boolean avoidAmbiguous) {
        this.avoidAmbiguous = avoidAmbiguous;
    }
}
