package se.iths.lw.microprojectproductservice.exception;

public class CategoryAlreadyExistsException  extends RuntimeException {
    public CategoryAlreadyExistsException(String message) {
        super (message);
    }
}
