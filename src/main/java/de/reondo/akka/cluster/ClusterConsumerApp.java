package de.reondo.akka.cluster;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import de.reondo.akka.cluster.actor.ClusterListenerActor;
import de.reondo.akka.cluster.actor.ConsumerActor;

/**
 * Created by denni on 17/12/2017.
 */
public class ClusterConsumerApp {

    public static void main( String[] args )
    {
        ActorSystem actorSystem = ActorSystem.create("ClusterSystem");
        ActorRef consumerActor = actorSystem.actorOf(Props.create(ConsumerActor.class), "ConsumerActor");
        actorSystem.actorOf(ClusterListenerActor.props(consumerActor), "ClusterListenerActor");
    }

}
