package net.birgun;

public enum ActionType {
    IN("GİRİŞ"), OUT("ÇIKIŞ");
    private String property;

    ActionType(String property) {
	this.property = property;
    }

    public String getProperty() {
	return property;
    }
}
