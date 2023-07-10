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
        for (Long shopId: shopIds) {
            CompletableFuture<Double> future = CompletableFuture
                    .supplyAsync(() -> priceRetriever.getPrice(itemId, shopId))
                    .completeOnTimeout(Double.NaN,2900, TimeUnit.MILLISECONDS);
            futures.add(future);
        }
        List<Double> prices = futures.stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (/*TimeoutException |*/ InterruptedException | ExecutionException e) {
                        return Double.NaN;
                    }
                }).collect(Collectors.toList());
        return Collections.min(prices);
    }
}
