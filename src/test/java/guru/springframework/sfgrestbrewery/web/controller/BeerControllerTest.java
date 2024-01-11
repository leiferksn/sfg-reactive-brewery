package guru.springframework.sfgrestbrewery.web.controller;


import guru.springframework.sfgrestbrewery.bootstrap.BeerLoader;
import guru.springframework.sfgrestbrewery.services.BeerService;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import guru.springframework.sfgrestbrewery.web.model.BeerPagedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;

@WebFluxTest(BeerController.class)
public class BeerControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private BeerService beerService;

    BeerDto validBeer;

    @BeforeEach
    void setUp() {
        validBeer = BeerDto.builder()
                .beerName("Test beer")
                .beerStyle("PALE_ALE")
                .upc(BeerLoader.BEER_1_UPC)
                .build();
    }

    @Test
    void shouldGetABeerById() {
        UUID beerId = UUID.randomUUID();
        given(beerService.getById(any(), any())).willReturn(Mono.just(validBeer));

        webTestClient.get()
                .uri("/api/v1/beer/" + beerId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BeerDto.class)
                .value(beerDto -> beerDto.getBeerName(), equalTo(validBeer.getBeerName()));
    }

    @Test
    void shouldGetAllBeers() {

        given(beerService.listBeers(any(), any(), any(), any())).willReturn(Mono.just(new BeerPagedList(List.of(validBeer))));

        webTestClient.get()
                .uri("/api/v1/beer")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BeerPagedList.class)
                .value(beerDtos -> beerDtos.getSize(), equalTo(1));
    }

    @Test
    void shouldGetBeerByUpc() {
        final var upc = BeerLoader.BEER_1_UPC;
        given(beerService.getByUpc(upc)).willReturn(Mono.just(validBeer));

        webTestClient.get()
                .uri("/api/v1/beerUpc/" + upc)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BeerDto.class)
                .value(beerDto -> beerDto.getUpc(), equalTo(upc));

    }

    @Test
    void shouldCreateNewBeer() {
        final var idCreatedBeer = 1;
        final var createdBear = BeerDto.builder()
                .id(idCreatedBeer)
                .price(validBeer.getPrice())
                .beerName(validBeer.getBeerName())
                .beerStyle(validBeer.getBeerStyle())
                .upc(validBeer.getUpc())
                .build();
        given(beerService.saveNewBeer(validBeer)).willReturn(Mono.just(createdBear));

        webTestClient.post()
                .uri("/api/v1/beer")
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(validBeer))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader()
                .value("Location", equalTo("http://api.springframework.guru/api/v1/beer/" + idCreatedBeer));
    }

    @Test
    void shouldUpdateBeer() {
        final var uuidUpdatedBeer = UUID.randomUUID();
        final var beerToUpdate = BeerDto.builder()
                .price(validBeer.getPrice())
                .beerName(validBeer.getBeerName() + ":updated")
                .beerStyle(validBeer.getBeerStyle())
                .upc(validBeer.getUpc())
                .build();
        given(beerService.updateBeer(uuidUpdatedBeer, beerToUpdate)).willReturn(beerToUpdate);

        webTestClient.put()
                .uri("/api/v1/beer/" + uuidUpdatedBeer)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(beerToUpdate))
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void shouldDeleteBeer() {
        final var idBeerToDelete = 1;
        doNothing().when(beerService).deleteBeerById(idBeerToDelete);

        webTestClient.delete()
                .uri("/api/v1/beer/" + idBeerToDelete)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();
    }

}
