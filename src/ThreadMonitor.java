import java.lang.reflect.Array;

public class ThreadMonitor {

    ThreadGroup tg;

    ThreadMonitor(Thread t){
        tg = t.getThreadGroup();
    }

    private static Object [ ]  prepend ( Object [ ]  oldArray, Object  o ){

        Object [ ]  newArray = ( Object [ ] ) Array.newInstance (
                oldArray.getClass ( ).getComponentType ( ), oldArray.length + 1 );

        System.arraycopy ( oldArray, 0, newArray, 1, oldArray.length );

        newArray [ 0 ] = o;

        return newArray;
    }

    private ThreadGroup getSystemThreadGroup(){
        ThreadGroup current;
        do{
            current = tg.getParent();
        }while (!current.getName().equals("system"));

        return current;
    }

    private void getThreadGroups(ThreadGroup[] tgArray){
        tg.enumerate(tgArray, true);
    }

    public Thread[] getThreadsInGroup(ThreadGroup threadGroup){
        Thread[] threadArray = new Thread[threadGroup.activeCount()];
        threadGroup.enumerate(threadArray);
        return threadArray;
    }

    public ThreadGroup[] getThreadGroupArray(){
        tg = getSystemThreadGroup();
        int totalThreadGroupEstimate = tg.activeGroupCount();
        ThreadGroup[] threadGroupArray = new ThreadGroup[totalThreadGroupEstimate];
        getThreadGroups(threadGroupArray);
        threadGroupArray = (ThreadGroup[]) prepend(threadGroupArray, tg);

        return threadGroupArray;
    }

}
