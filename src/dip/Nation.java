package dip;

public enum Nation {

    ENGLAND("English"),
    FRANCE("French"),
    GERMANY("German"),
    ITALY("Italian"),
    AUSTRIA("Austrian"),
    RUSSIA("Russian"),
    TURKEY("Turkish");

    private final String adjective;

    public String getAdjective() {
        return adjective;
    }

    private Nation(String adjective) {
        this.adjective = adjective;
    }

}