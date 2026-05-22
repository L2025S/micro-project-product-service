package se.iths.lw.microprojectproductservice.exception;

public class InsufficientStockException extends InvalidDomainException{

    public InsufficientStockException(String message) {
        super(message);
    }
}
