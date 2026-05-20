package se.iths.lw.microprojectproductservice.exception;

public abstract class InvalidDomainException extends RuntimeException{
    public InvalidDomainException(String message) {
        super(message);
    }
}
