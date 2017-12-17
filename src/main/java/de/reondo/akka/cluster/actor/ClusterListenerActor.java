package de.reondo.akka.cluster.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.UnreachableMember;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class ClusterListenerActor extends AbstractActor {

    LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    Cluster cluster = Cluster.get(getContext().getSystem());
    private ActorRef consumerActor;

    public static Props props(ActorRef consumerActor) {
        return Props.create(ClusterListenerActor.class, () -> new ClusterListenerActor(consumerActor));
    }

    public ClusterListenerActor(ActorRef consumerActor) {
        this.consumerActor = consumerActor;
    }

    //subscribe to cluster changes
    @Override
    public void preStart() {
        cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(),
                MemberEvent.class, UnreachableMember.class);
    }

    //re-subscribe when restart
    @Override
    public void postStop() {
        cluster.unsubscribe(getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(MemberUp.class, mUp -> {
                log.info("Member is Up: {}", mUp.member());
                register(mUp.member());
            })
            .match(ClusterEvent.CurrentClusterState.class, state -> {
                for (Member member : state.getMembers()) {
                    if (member.status().equals(MemberStatus.up())) {
                        register(member);
                    }
                }
            })
            .build();
    }

    private void register(Member member) {
        if (member.hasRole("producer")) {
            getContext().actorSelection(member.address() + "/user/ProducerActor").tell(
                "consumerRegistration", consumerActor);
        }
    }
}