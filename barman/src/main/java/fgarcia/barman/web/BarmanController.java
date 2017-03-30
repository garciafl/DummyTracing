package fgarcia.barman.web;

import com.netflix.hystrix.HystrixCommand;
import fgarcia.barman.feign.ClerkClient;
import fgarcia.barman.feign.ShakerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by Flo on 17/01/2017.
 */
@RestController
public class BarmanController {

    private static final Logger logger = LoggerFactory.getLogger(BarmanController.class);

    private ClerkClient clerkClient;
    private ShakerClient shakerClient;

    private Tracer tracer;

    @Autowired
    public BarmanController(ClerkClient clerkClient, ShakerClient shakerClient, Tracer tracer) {
        this.clerkClient = clerkClient;
        this.shakerClient = shakerClient;
        this.tracer = tracer;
    }

    /**
     * Prepare mulled wine.
     *
     * @return the recipe to prepare the wine and the elapsed time.
     */
    @RequestMapping(value = "/make", method = RequestMethod.GET)
    public String prepareCocktail() throws ExecutionException, InterruptedException {
        long startTime = System.currentTimeMillis();

        // Ask clerk for ingredients
        Future<String> ingredients = clerkClient.fetchIngredients().queue();

        // In the mean time, the barman fetch a glass
        getGlass();

        // We block, waiting for ingredients
        ingredients.get();

        // Then we shake
        shakerClient.shake();

        return "Served in " + (System.currentTimeMillis() - startTime) + " by " + tracer.getCurrentSpan().traceIdString();
    }

    /**
     * Pendant la démo => passer ça en async
     *
     * @throws InterruptedException
     */
    private void getGlass() throws InterruptedException {
        Span s = tracer.createSpan("glass");
        logger.info("Fetching a glass");
        tracer.addTag("type", "old fashioned");
        Thread.sleep(100);
        tracer.close(s);
    }
}
