package guru.springframework.sfgrestbrewery.web.controller;

import guru.springframework.sfgrestbrewery.bootstrap.BeerLoader;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static guru.springframework.sfgrestbrewery.web.functional.BeerRouterConfig.*;
import static org.assertj.core.api.Assertions.assertThat;
import static reactor.netty.http.client.HttpClient.create;

/**
 * Created by jt on 3/7/21.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class WebClientV2IT {

    public static final String BASE_URL = "http://localhost:8080";

    WebClient webClient;

    @BeforeEach
    void setUp() {
        webClient = WebClient.builder()
                .baseUrl(BASE_URL)
                .clientConnector(new ReactorClientHttpConnector(create().wiretap(true)))
                .build();
    }

    @Test
    void shouldGetBeerById() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        Mono<BeerDto> beerDtoMono = webClient.get().uri(uriBuilder -> uriBuilder.path(BEER_V2_URL_BEER_ID).build(5))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(BeerDto.class);

        beerDtoMono.subscribe(beerDto -> {
            assertThat(beerDto).isNotNull();
            assertThat(beerDto.getBeerName()).isNotNull();

            countDownLatch.countDown();
        });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);


    }

    @Test
    void shouldGetBeerByUpc() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        Mono<BeerDto> beerDtoMono = webClient.get().uri(uriBuilder -> uriBuilder.path(BEER_V2_URL_UPC).build(BeerLoader.BEER_7_UPC))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(BeerDto.class);

        beerDtoMono.subscribe(beerDto -> {
            assertThat(beerDto).isNotNull();
            assertThat(beerDto.getBeerName()).isNotNull();

            countDownLatch.countDown();
        });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);


    }

    @Test
    void shouldGetBeerByIdNotFound() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        Mono<BeerDto> beerDtoMono = webClient.get().uri(uriBuilder -> uriBuilder.path(BEER_V2_URL_BEER_ID).build(1337))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(BeerDto.class);

        beerDtoMono.subscribe(beerDto -> {

        }, throwable -> {
            countDownLatch.countDown();
        });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    void shouldGetBeerByUpcNotFound() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        Mono<BeerDto> beerDtoMono = webClient.get().uri(uriBuilder -> uriBuilder.path(BEER_V2_URL_UPC).build("unknown-upc"))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(BeerDto.class);

        beerDtoMono.subscribe(beerDto -> {

        }, throwable -> {
            countDownLatch.countDown();
        });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    void shouldCreateNewBeer() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        BeerDto beerDto = BeerDto.builder()
                .beerName("New Beer")
                .upc("123")
                .beerStyle("PALE_ALE")
                .price(new BigDecimal(12.34))
                .build();

        final var beerResponseMono = webClient.post().uri(uriBuilder -> uriBuilder
                        .path(BEER_V2_URL)
                        .build())
                .body(BodyInserters.fromValue(beerDto))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().toBodilessEntity();

        beerResponseMono.publishOn(Schedulers.parallel()).subscribe(responseEntity -> {
            assertThat(responseEntity.getStatusCode().is2xxSuccessful());
            countDownLatch.countDown();
        });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    void shouldCreateNewBeerBadRequest() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        BeerDto beerDto = BeerDto.builder()
                .price(new BigDecimal(12.34))
                .build();

        final var beerResponseMono = webClient.post().uri(uriBuilder -> uriBuilder
                        .path(BEER_V2_URL)
                        .build())
                .body(BodyInserters.fromValue(beerDto))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().toBodilessEntity();

        beerResponseMono.publishOn(Schedulers.parallel()).subscribe(responseEntity -> {

        }, throwable -> {
            countDownLatch.countDown();
        });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }



}
