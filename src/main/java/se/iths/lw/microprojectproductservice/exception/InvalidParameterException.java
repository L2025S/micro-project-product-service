package se.iths.lw.microprojectproductservice.exception;

public class InvalidParameterException extends RuntimeException {

    public InvalidParameterException(String message){
        super(message);
    }
}
