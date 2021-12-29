package main.exceptions;

public class TextCommentNotFoundException extends RuntimeException{
    public TextCommentNotFoundException(String message) {
        super(message);
    }
}
