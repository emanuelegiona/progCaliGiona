package src.wsa.web;
import java.net.URL;
import java.util.concurrent.*;

public class SimpleAsyncLoader implements AsyncLoader {
    private ConcurrentLinkedQueue<Loader> loaderPool;
    private ExecutorService pool;

    public SimpleAsyncLoader(){
        loaderPool = new ConcurrentLinkedQueue<>();
        int cpu=Runtime.getRuntime().availableProcessors();
        for(int i=0;i<cpu;i++)
            loaderPool.add(WebFactory.getLoader());
        pool=Executors.newFixedThreadPool(cpu, tf -> {
            Thread t = new Thread(tf);
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public Future<LoadResult> submit(URL url) {
        if (isShutdown()) throw new IllegalStateException();

        return pool.submit(()->{
            while (loaderPool.isEmpty()) {
                Thread.sleep(10);
            }

            Loader loader=loaderPool.poll();
            LoadResult r=loader.load(url);
            loaderPool.add(loader);
            return r;
        });
    }

    @Override
    public void shutdown() {
        pool.shutdown();
    }

    @Override
    public boolean isShutdown() {
        return pool.isShutdown();
    }
}
