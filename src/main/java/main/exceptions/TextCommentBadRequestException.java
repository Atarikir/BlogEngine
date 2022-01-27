package main.exceptions;

public class TextCommentBadRequestException extends RuntimeException{
    public TextCommentBadRequestException(String message) {
        super(message);
    }
}
