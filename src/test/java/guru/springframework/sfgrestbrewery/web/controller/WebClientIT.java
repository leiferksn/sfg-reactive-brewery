package guru.springframework.sfgrestbrewery.web.controller;

import guru.springframework.sfgrestbrewery.bootstrap.BeerLoader;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import guru.springframework.sfgrestbrewery.web.model.BeerPagedList;
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
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static reactor.netty.http.client.HttpClient.create;

/**
 * Created by jt on 3/7/21.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class WebClientIT {

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
    void shouldGetBeerByUpc() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        Mono<BeerDto> beerDtoMono = webClient.get().uri("api/v1/beerUpc/" + BeerLoader.BEER_1_UPC)
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
    void shouldGetBeerById() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        Mono<BeerDto> beerDtoMono = webClient.get().uri("api/v1/beer/1")
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
    void testListBeers() throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(1);

        Mono<BeerPagedList> beerPagedListMono = webClient.get().uri("/api/v1/beer")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(BeerPagedList.class);

        beerPagedListMono.publishOn(Schedulers.parallel()).subscribe(beerPagedList -> {

            beerPagedList.getContent().forEach(beerDto -> System.out.println(beerDto.toString()));

            countDownLatch.countDown();
        });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    void testListBeersByName() throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(1);

        Mono<BeerPagedList> beerPagedListMono = webClient.get().uri(uriBuilder -> uriBuilder
                        .path("/api/v1/beer")
                        .queryParamIfPresent("beerName", Optional.ofNullable("Galaxy Cat"))
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(BeerPagedList.class);

        beerPagedListMono.publishOn(Schedulers.parallel()).subscribe(beerPagedList -> {
            assertThat(beerPagedList.getContent().size()).isEqualTo(1);
            countDownLatch.countDown();
        });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    void testListBeersByBeerStyle() throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(1);

        Mono<BeerPagedList> beerPagedListMono = webClient.get().uri(uriBuilder -> uriBuilder
                        .path("/api/v1/beer")
                        .queryParamIfPresent("beerStyle", Optional.ofNullable("WHEAT"))
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(BeerPagedList.class);


        beerPagedListMono.publishOn(Schedulers.parallel()).subscribe(beerPagedList -> {
            assertThat(beerPagedList.getContent().size()).isEqualTo(4);
            countDownLatch.countDown();
        });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    void testShouldCreateBeer() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        BeerDto beerDto = BeerDto.builder()
                .beerName("New Beer")
                .upc("123")
                .beerStyle("PALE_ALE")
                .price(new BigDecimal(12.34))
                .build();

        final var beerResponseMono = webClient.post().uri(uriBuilder -> uriBuilder
                .path("/api/v1/beer")
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
    void testShouldUpdateBeer() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        BeerDto beerDto = BeerDto.builder()
                .beerName("New Beer:UPDATED")
                .upc("123")
                .beerStyle("PALE_ALE")
                .price(new BigDecimal(12.34))
                .build();

        final var beerResponseMono = webClient.put().uri(uriBuilder -> uriBuilder
                        .path("/api/v1/beer/" + 1)
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
    void shouldUpdateTheFirstBeerInTheList() throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(3);

        webClient.get().uri("/api/v1/beer")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(BeerPagedList.class)
                .publishOn(Schedulers.single())
                .subscribe(pagedList -> {
                    countDownLatch.countDown();

                    // get existing beer

                    final var existingBeer = pagedList.getContent().get(0);

                    final var beerToUpdate = BeerDto.builder()
                            .price(existingBeer.getPrice())
                            .beerName(existingBeer.getBeerName() + ":updated")
                            .beerStyle(existingBeer.getBeerStyle())
                            .upc(existingBeer.getUpc())
                            .build();

                    // update existing beer
                    webClient.put().uri("/api/v1/beer/" + existingBeer.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(BodyInserters.fromValue(beerToUpdate))
                            .retrieve()
                            .toBodilessEntity()
                            .flatMap(responseEntity -> {

                                countDownLatch.countDown();
                                return webClient.get().uri("/api/v1/beer/" + existingBeer.getId() )
                                        .accept(MediaType.APPLICATION_JSON)
                                        .retrieve()
                                        .bodyToMono(BeerDto.class);
                            }).subscribe(updatedBeer -> {
                                assertThat(updatedBeer.getBeerName()).isEqualTo(existingBeer.getBeerName() + ":updated");
                                countDownLatch.countDown();
                            });
                });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }
}
