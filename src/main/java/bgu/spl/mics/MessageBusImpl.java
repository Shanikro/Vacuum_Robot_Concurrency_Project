package bgu.spl.mics;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 The class is the implementation of the MessageBus interface.
 Write your implementation here! Only one public method
 (in addition to getters which can be public solely for unit testing)
 may be added to this class All other methods and members you add the class must be private.
 */
public class MessageBusImpl implements MessageBus {


	private static MessageBusImpl instance = null; // field for singleton

	private final Map<MicroService, Queue<Message>> microServices;
	private final Map<Class<? extends Broadcast>, Queue<MicroService>> broadcasts;
	private final Map<Class<? extends Event<?>>, Queue<MicroService>> events;

	private final Map<Event<?>, Future<?>> eventFuture = new ConcurrentHashMap<>();

	private MessageBusImpl() {
		microServices = new ConcurrentHashMap<>();
		broadcasts = new ConcurrentHashMap<>();
		events = new ConcurrentHashMap<>();
	}

	public static synchronized MessageBusImpl getInstance() {
		//not sure if this is the right way
		if (instance == null) {
			instance = new MessageBusImpl();
		}
		return instance;
	}

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		if(events.containsKey(type)){
			if(!events.get(type).contains(m))
				events.get(type).add(m);
		}
		else{
			events.put(type,new ConcurrentLinkedQueue<>());
			events.get(type).add(m);
		}
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		if(broadcasts.containsKey(type)){
			if(!broadcasts.get(type).contains(m))
				broadcasts.get(type).add(m);
		}
		else{
			broadcasts.put(type,new ConcurrentLinkedQueue<>());
			broadcasts.get(type).add(m);
		}
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		Future<T> future = (Future<T>) eventFuture.get(e);
		if (future != null) {
			future.resolve(result);
			eventFuture.remove(e);
			}
		}


	@Override
	public void sendBroadcast(Broadcast b) {
		Queue<MicroService> microServiceList = broadcasts.get(b.getClass());
		for (MicroService m : microServiceList) {
				addMsg(m, b);
		}
	}

	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		Queue<MicroService> microServiceQueue = events.get(e.getClass());
		synchronized (microServiceQueue) {
			if (microServiceQueue.isEmpty()) { //if nobody subscribe this event
				return null;
			}

			MicroService MS = microServiceQueue.remove(); //Take the next Micro-service according to round-robin fashion.
			addMsg(MS,e);
			microServiceQueue.add(MS); //Returns the Micro-service according to round-robin fashion.

			Future<T> future = new Future<>(); //Create future for the event
			eventFuture.put(e, future);

			return future;
		}
	}

	@Override
	public synchronized void register(MicroService m) {
		if(!microServices.containsKey(m)){
			microServices.put(m, new LinkedList<Message>());
		}
	}

	@Override
	public void unregister(MicroService m) {

		microServices.remove(m); //Remove from MicroServices Map

		for(Queue<MicroService> msList : broadcasts.values()){ //Remove from Broadcasts Map, if in
			synchronized (msList){
				msList.remove(m);
			}
		}

		for(Queue<MicroService> msQueue : events.values()){ //Remove from Events Map, if in
			synchronized (msQueue){
				msQueue.remove(m);
			}
		}
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		synchronized (microServices) {
			while (microServices.get(m).isEmpty()) {
				try {
					microServices.wait();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			return microServices.get(m).remove();
		}
	}

	private void addMsg(MicroService m, Message msg) {
		synchronized (microServices) {
			microServices.get(m).add(msg);
			microServices.notifyAll(); //For the next is waiting for message
		}
	}
	

}
