package guru.springframework.sfgrestbrewery.web.functional;

import guru.springframework.sfgrestbrewery.services.BeerService;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;


@Slf4j
@Component
@RequiredArgsConstructor
public class BeerHandlerV2 {

    private final BeerService beerService;
    private final Validator validator;

    public Mono<ServerResponse> beerById(ServerRequest serverRequest) {
        final var beerId = Integer.valueOf(serverRequest.pathVariable("beerId"));
        final var showInventory = Boolean.valueOf(serverRequest.queryParam("showInventory").orElse("false"));

        return beerService.getById(beerId, showInventory)
                .flatMap(beerDto -> ServerResponse.ok().bodyValue(beerDto)).switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> beerByUpc(ServerRequest serverRequest) {
        final var upc = String.valueOf(serverRequest.pathVariable("upc"));

        return beerService.getByUpc(upc)
                .flatMap(beerDto -> ServerResponse.ok().bodyValue(beerDto)).switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> newBeer(ServerRequest serverRequest) {
        Mono<BeerDto> beerDtoMono = serverRequest.bodyToMono(BeerDto.class).doOnNext(this::validate);

        return beerService.saveNewBeer(beerDtoMono)
                .flatMap(beerDto ->  {
                    return ServerResponse.ok().header("Location", BeerRouterConfig.BEER_V2_URL + "/" + beerDto.getId()).build();
                });
    }

    private void validate(BeerDto beerDto) {
        final var errors = new BeanPropertyBindingResult(beerDto, "beerDto");
        validator.validate(beerDto, errors);

        if (errors.hasErrors()) {
            throw new ServerWebInputException(errors.toString());
        }
    }


}
