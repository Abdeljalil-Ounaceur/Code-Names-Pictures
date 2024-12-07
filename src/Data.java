
public class Data {
    private String packet;
    
    // True if receiver should wait
    // False if sender should wait
    private boolean transfer = true;
 
    public synchronized String receive() {
        while (transfer) {
            try {
            	System.out.println(Thread.currentThread().getName()  + " <receive> calls wait()");
                wait();
                System.out.println(Thread.currentThread().getName() +  " notified.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); 
                System.err.println("Thread Interrupted");
            }
        }
        transfer = true;
        
        String returnPacket = packet;
        notifyAll();
        return returnPacket;
    }
 
    public synchronized void send(String packet) {
        while (!transfer) {
            try { 
            	System.out.println(Thread.currentThread().getName() + " <send> calls wait()");
                wait();
                System.out.println("notified()");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); 
                System.err.println("Thread Interrupted");
            }
        }
        transfer = false;
        
        this.packet = packet;
        System.out.println(Thread.currentThread().getName()  + " <send> calls notifyAll()");
        notifyAll();
    }
}