package course.concurrency.m2_async.cf.min_price;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class PriceAggregator {

    private PriceRetriever priceRetriever = new PriceRetriever();

    public void setPriceRetriever(PriceRetriever priceRetriever) {
        this.priceRetriever = priceRetriever;
    }

    private Collection<Long> shopIds = Set.of(10l, 45l, 66l, 345l, 234l, 333l, 67l, 123l, 768l);

    public void setShops(Collection<Long> shopIds) {
        this.shopIds = shopIds;
    }

    public double getMinPrice(long itemId) {
        List<CompletableFuture<Double>> futures = new ArrayList<>();
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (Long shopId: shopIds) {
            CompletableFuture<Double> future = CompletableFuture
                    .supplyAsync(() -> priceRetriever.getPrice(itemId, shopId), executorService)
                    .completeOnTimeout(Double.NaN,2900, TimeUnit.MILLISECONDS)
                    .handle((result, ex) -> {
                        if (ex != null) {
                            return Double.NaN;
                        } else {
                            return result;
                        }
                    });
            futures.add(future);
        }
        return futures.stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (InterruptedException | ExecutionException e) {
                        return Double.NaN;
                    }
                }).min(Double::compareTo).get();
    }
}
