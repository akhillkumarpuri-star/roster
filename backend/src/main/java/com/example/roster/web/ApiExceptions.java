package com.example.roster.web;

public final class ApiExceptions {
    private ApiExceptions() {}

    public static class NotFound extends RuntimeException {
        public NotFound(String message) { super(message); }
    }

    public static class Conflict extends RuntimeException {
        public Conflict(String message) { super(message); }
    }

    /** A request that is well-formed but breaks a business rule (e.g. from-date after to-date). */
    public static class BadRequest extends RuntimeException {
        private final String field;
        public BadRequest(String field, String message) { super(message); this.field = field; }
        public String getField() { return field; }
    }
}
