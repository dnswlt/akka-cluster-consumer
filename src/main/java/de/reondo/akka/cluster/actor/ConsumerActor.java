package de.reondo.akka.cluster.actor;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import de.reondo.akka.cluster.proto.product.ProductProtos;

/**
 * Created by denni on 17/12/2017.
 */
public class ConsumerActor extends AbstractActor {

    LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    int count;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ProductProtos.Product.class, this::onProduct)
                .build();
    }

    @Override
    public void postStop() throws Exception {
        log.info("ConsumerActor stopped");
    }

    @Override
    public void preStart() throws Exception {
        log.info("ConsumerActor started");
    }

    private void onProduct(ProductProtos.Product product) {
        log.info("Product received: \"{} \'{}\' ({})\"", product.getId(), product.getName(), product.getPrice());
        count++;
        if (count > 1000) {
            getContext().getSystem().stop(getSelf());
        }
    }

}
