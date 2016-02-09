package src.wsa.web;

import java.net.URL;
import java.util.concurrent.*;

/** Gestisce piu' Loader in maniera concorrente per permettere il download di pagine in modo asincrono; implementa AsyncLoader.*/
public class SimpleAsyncLoader implements AsyncLoader {
    private ConcurrentLinkedQueue<Loader> loaderPool;
    private ExecutorService pool;

    /** Costruttore di SimpleAsyncLoader
     Mantiene una coda concorrente di Loader, ognuno utilizzabile per scaricare una pagina*/
    public SimpleAsyncLoader(){
        loaderPool = new ConcurrentLinkedQueue<>();
        int cpu=Runtime.getRuntime().availableProcessors();
        for(int i=0;i<=cpu;i++)
            loaderPool.add(WebFactory.getLoader());
        pool=Executors.newFixedThreadPool(cpu, tf -> {
            Thread t = new Thread(tf);
            t.setDaemon(true);
            return t;
        });
    }

    /** Sottomette il downloading della pagina dello specificato URL e ritorna
     * un Future per ottenere il risultato in modo asincrono.
     * @param url  un URL di una pagina web
     * @throws IllegalStateException se il loader e' chiuso
     * @return Future per ottenere il risultato in modo asincrono */
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

    /** Chiude il loader e rilascia tutte le risorse. Dopo di cio' non puo' piu'
     * essere usato. */
    @Override
    public void shutdown() {
        pool.shutdown();
    }

    /** Ritorna true se e' chiuso.
     * @return true se e' chiuso */
    @Override
    public boolean isShutdown() {
        return pool.isShutdown();
    }
}