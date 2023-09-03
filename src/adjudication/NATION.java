package adjudication;

public enum NATION {

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

    private NATION(String adjective) {
        this.adjective = adjective;
    }

}