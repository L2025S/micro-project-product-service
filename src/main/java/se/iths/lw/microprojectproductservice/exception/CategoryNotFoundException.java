package se.iths.lw.microprojectproductservice.exception;

public class CategoryNotFoundException  extends RuntimeException {
    public CategoryNotFoundException(String message) {
        super(message);
    }
}
