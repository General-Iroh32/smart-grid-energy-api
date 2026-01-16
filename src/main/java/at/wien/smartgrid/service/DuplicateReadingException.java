package at.wien.smartgrid.service;

public class DuplicateReadingException extends RuntimeException {

    public DuplicateReadingException(String meterId) {
        super("A reading already exists for meter %s at this timestamp".formatted(meterId));
    }
}

