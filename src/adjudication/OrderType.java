package adjudication;

public enum OrderType {

    NONE,
    VOID,
    HOLD,
    MOVE,
    SUPPORT,
    CONVOY,
    BUILD,
    REMOVE,
    RETREAT;

    public static OrderType fromAbbr(String abbreviation) {
        return switch (abbreviation) {
            case "H" -> HOLD;
            case "-" -> MOVE;
            case "S" -> SUPPORT;
            case "C" -> CONVOY;
            case "B" -> BUILD;
            case "V" -> VOID;
            case "R" -> RETREAT;
            case "P" -> REMOVE;
            default -> NONE;
        };
    }

}