package chaitu.android.ychat.model;

public class Message {

    private String receiver;
    private String image;
    private String message;
    private String time;
    private String sender;
    private String seen;

    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Message(String receiver, String image, String message, String time, String sender) {

        this.receiver = receiver;
        this.image = image;
        this.message = message;
        this.time = time;
        this.sender = sender;
    }





    public Message() {
    }



    public String getReceiver() {
        return receiver;
    }

    public String getImage() {
        return image;
    }

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }
}
