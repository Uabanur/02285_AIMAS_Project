package dtu.aimas.errors;

public class InvalidArgument extends Throwable{
    public InvalidArgument(String message){
        super(message);
    }
}