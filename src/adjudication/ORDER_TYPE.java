package adjudication;

public enum ORDER_TYPE {

    NONE,
    VOID,
    HOLD,
    MOVE,
    SUPPORT,
    CONVOY,
    BUILD,
    REMOVE,
    RETREAT;

    public static ORDER_TYPE fromAbbr(String abbreviation) {
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