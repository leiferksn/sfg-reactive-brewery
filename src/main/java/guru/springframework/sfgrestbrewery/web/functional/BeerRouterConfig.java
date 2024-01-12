package guru.springframework.sfgrestbrewery.web.functional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class BeerRouterConfig {

    public static final String BEER_V2_URL = "/api/v2/beer";
    public static final String BEER_V2_URL_BEER_ID = BEER_V2_URL + "/{beerId}";
    public static final String BEER_V2_URL_UPC= "/api/v2/beerUpc/{upc}";

    @Bean
    public RouterFunction<ServerResponse> beerRoutesV2(BeerHandlerV2 handler) {
        return route().GET(BEER_V2_URL_BEER_ID, accept(MediaType.APPLICATION_JSON), handler::beerById)
                .GET(BEER_V2_URL_UPC, accept(MediaType.APPLICATION_JSON), handler::beerByUpc)
                .POST(BEER_V2_URL, accept(MediaType.APPLICATION_JSON), handler::newBeer)
                .PUT(BEER_V2_URL_BEER_ID, accept(MediaType.APPLICATION_JSON), handler::updateBeer)
                .DELETE(BEER_V2_URL_BEER_ID, accept(MediaType.APPLICATION_JSON), handler::deleteBeer)
                .build();
    }

}
