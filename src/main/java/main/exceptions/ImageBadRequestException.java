package main.exceptions;

public class ImageBadRequestException extends RuntimeException{
    public ImageBadRequestException(String message) {
        super(message);
    }
}
