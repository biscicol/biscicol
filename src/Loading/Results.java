package Loading;

/**
 * Created by IntelliJ IDEA.
 * User: biocode
 * Date: Jul 18, 2011
 * Time: 2:39:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class Results {
    private boolean success = true;
    private long start;
    private long end;
                private String message = "";

    public Results() {
        reset();
        start = System.currentTimeMillis();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void end() {
        end = System.currentTimeMillis();
    }

    public long duration() {
        return (long)((end - start)*.001);
    }

    public void reset() {
        start = 0;
        end = 0;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean printSuccess() {
        System.out.println(duration() + " sec; " + getMessage());
        return isSuccess();        
    }
    public boolean closeAndPrint(String message) {
        this.end();
        System.out.println(message + " ("+ duration() + " seconds)");
        return isSuccess();
    }
}
