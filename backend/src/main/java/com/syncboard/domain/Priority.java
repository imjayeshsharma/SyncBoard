// Stage 1 — Java 25 domain core
package com.syncboard.domain;

/**
 * Ticket priority. Constant names, wire values and sort rank are fixed by the
 * shared contract (01-CONTRACT.md §2).
 */
public enum Priority {
    CRITICAL("Critical", 0),
    HIGH("High", 1),
    MEDIUM("Medium", 2),
    LOW("Low", 3);

    private final String wireValue;
    private final int rank;

    Priority(String wireValue, int rank) {
        this.wireValue = wireValue;
        this.rank = rank;
    }

    /** The JSON/DB/TS wire value for this priority. */
    public String getWireValue() {
        return wireValue;
    }

    /** Sort rank for board ordering: Critical=0 … Low=3 (lower sorts first). */
    public int rank() {
        return rank;
    }

    /** Resolves a priority from its wire value; throws if unknown. */
    public static Priority fromWireValue(String wireValue) {
        for (Priority priority : values()) {
            if (priority.wireValue.equals(wireValue)) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Unknown Priority wire value: " + wireValue);
    }
}
