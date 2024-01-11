package guru.springframework.sfgrestbrewery.repositories;


import guru.springframework.sfgrestbrewery.domain.Beer;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;


public interface BeerRepository extends ReactiveCrudRepository<Beer, Integer> {
    // public interface BeerRepository extends JpaRepository<Beer, UUID> {
//    Page<Beer> findAllByBeerName(String beerName, Pageable pageable);
//
//    Page<Beer> findAllByBeerStyle(BeerStyleEnum beerStyle, Pageable pageable);
//
//    Page<Beer> findAllByBeerNameAndBeerStyle(String beerName, BeerStyleEnum beerStyle, Pageable pageable);

    // do not work at the moment, but may in the future
    //    Flux<Page<Beer>> findAllByBeerName(String beerName, Pageable pageable);
//
//    Flux<Page<Beer>> findAllByBeerStyle(BeerStyleEnum beerStyle, Pageable pageable);
//
//    Flux<Page<Beer>> findAllByBeerNameAndBeerStyle(String beerName, BeerStyleEnum beerStyle, Pageable pageable);

    Mono<Beer> findByUpc(String upc);
}
