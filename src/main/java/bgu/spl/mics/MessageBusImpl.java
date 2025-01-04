package bgu.spl.mics;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 The class is the implementation of the MessageBus interface.
 Write your implementation here! Only one public method
 (in addition to getters which can be public solely for unit testing)
 may be added to this class All other methods and members you add the class must be private.
 */
public class MessageBusImpl implements MessageBus {

	private final Map<MicroService, BlockingQueue<Message>> microServices;
	private final Map<Class<? extends Broadcast>, BlockingQueue<MicroService>> broadcasts;
	private final Map<Class<? extends Event<?>>, BlockingQueue<MicroService>> events;

	private final Map<Event<?>, Future<?>> eventFuture = new ConcurrentHashMap<>();

	//Private constructor
	private MessageBusImpl() {
		microServices = new ConcurrentHashMap<>();
		broadcasts = new ConcurrentHashMap<>();
		events = new ConcurrentHashMap<>();
	}

	//Internal static class that holds the Singleton
	private static class singletonHolder {
		private static final MessageBusImpl INSTANCE = new MessageBusImpl();
	}

	public static MessageBusImpl getInstance() {
		return singletonHolder.INSTANCE;
	}

	/**
	 * Subscribes a microservice to receive events of a specific type.
	 *
	 * @param type The class type of the event to subscribe to.
	 * @param m    The microservice subscribing to the event.
	 *
	 * @pre type != null
	 * @pre m != null
	 * @post events.containsKey(type)
	 * @post events.get(type).contains(m)
	 */
	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		if(events.containsKey(type)){
			if(!events.get(type).contains(m))
				events.get(type).add(m);
		}
		else{
			events.put(type,new LinkedBlockingQueue<>());
			events.get(type).add(m);
		}
	}

	/**
	 * Subscribes a microservice to receive broadcasts of a specific type.
	 *
	 * @param type The class type of the broadcast to subscribe to.
	 * @param m    The microservice subscribing to the broadcast.
	 *
	 * @pre type != null
	 * @pre m != null
	 * @post broadcasts.containsKey(type)
	 * @post broadcasts.get(type).contains(m)
	 */
	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		if(broadcasts.containsKey(type)){
			if(!broadcasts.get(type).contains(m))
				broadcasts.get(type).add(m);
		}
		else{
			broadcasts.put(type,new LinkedBlockingQueue<>());
			broadcasts.get(type).add(m);
		}
	}

	/**
	 * Completes an event and resolves its associated future with the given result.
	 *
	 * @param e      The event to complete.
	 * @param result The result to resolve the event's future with.
	 *
	 * @pre e != null
	 * @pre eventFuture.containsKey(e)
	 * @post eventFuture.get(e).isDone() == true
	 * @post eventFuture.get(e).get() == result
	 * @post !eventFuture.containsKey(e)
	 */
	@Override
	public <T> void complete(Event<T> e, T result) {
		Future<T> future = (Future<T>) eventFuture.get(e);
		if (future != null) {
			future.resolve(result);
			eventFuture.remove(e);
			}
		}


	/**
	 * Sends a broadcast message to all subscribed microservices.
	 *
	 * @param b The broadcast message to send.
	 *
	 * @pre b != null
	 * @pre broadcasts.containsKey(b.getClass())
	 * @post Each microservice subscribed to the broadcast receives the message.
	 */
	@Override
	public void sendBroadcast(Broadcast b) {
		BlockingQueue<MicroService> microServiceList = broadcasts.get(b.getClass());
		if (microServiceList != null) {
			for (MicroService m : microServiceList) {
				addMsg(m, b); //Add the message to the register MicroService
			}
		}
	}

	/**
	 * Sends an event to a subscribed microservice using a round-robin fashion.
	 *
	 * @param e The event to send.
	 * @param <T> The type of the result expected from the event.
	 *
	 * @pre e != null
	 * @pre events.containsKey(e.getClass())
	 * @pre events.get(e.getClass()).size() > 0
	 * @post Future object associated with the event is created.
	 * @post The event is assigned to the next microservice in the round-robin order.
	 * @return A Future object that can be used to retrieve the result of processing the event.
	 */
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {

		BlockingQueue<MicroService> microServiceQueue = events.get(e.getClass());

		synchronized (microServiceQueue) {
			MicroService ms = microServiceQueue.poll(); //Take the next Micro-service according to round-robin fashion.
			addMsg(ms, e);
			microServiceQueue.add(ms); //Returns the Micro-service according to round-robin fashion.

			Future<T> future = new Future<>(); //Create future for the event
			eventFuture.put(e, future);

			return future;
		}
	}

	/**
	 * Registers a microservice to the message bus.
	 *
	 * @param m The microservice to register.
	 *
	 * @pre m != null
	 * @post microServices.containsKey(m)
	 * @post microServices.get(m).isEmpty()
	 */
	@Override
	public synchronized void register(MicroService m) {
		if(!microServices.containsKey(m)){
			microServices.put(m, new LinkedBlockingQueue<Message>());
			System.out.println(m.getName() + " register microServices");
		}
	}

	/**
	 * Unregisters a microservice from the message bus, removing it from all subscriptions.
	 *
	 * @param m The microservice to unregister.
	 *
	 * @pre m != null
	 * @pre microServices.containsKey(m)
	 * @post !microServices.containsKey(m)
	 * @post All references to m are removed from events and broadcasts.
	 */
	@Override
	public void unregister(MicroService m) {

		synchronized (microServices) {
			microServices.remove(m); //Remove from MicroServices Map
		}

		synchronized (broadcasts) {
			for (BlockingQueue<MicroService> msList : broadcasts.values()) {
				msList.remove(m); //Remove from Broadcasts Map
			}
		}

		synchronized (events) {
			for (BlockingQueue<MicroService> msQueue : events.values()) {
				msQueue.remove(m); //Remove from Events Map
			}
		}
	}

	/**
	 * Blocks until a message is available for the given microservice, then retrieves it.
	 *
	 * @param m The microservice waiting for a message.
	 * @return The next message available for the given microservice.
	 *
	 * @pre m != null
	 * @pre microServices.containsKey(m)
	 * @post microServices.get(m).size() == @pre(microServices.get(m).size()) - 1
	 */
	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {

		BlockingQueue<Message> queue = microServices.get(m);
		if (queue == null) { //If any Micro Service not register yet
			throw new IllegalStateException("MicroService is not registered");
		}
		return queue.take();
	}

	private void addMsg(MicroService m, Message msg) {
		synchronized (microServices) {
			microServices.get(m).add(msg);
			microServices.notifyAll(); //For the next is waiting for message
		}
	}
	

}
