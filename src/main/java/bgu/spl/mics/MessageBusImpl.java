package bgu.spl.mics;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The {@link MessageBusImpl} class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {


	private static MessageBusImpl instance = null; // field for singleton

	private final Map<MicroService, Queue<Message>> microServices;
	private final Map<Class<? extends Broadcast>, List<MicroService>> broadcasts; //לבדוק אם צריך שגם הרשימה הפנימית תיהיה אטומית
	private final Map<Class<? extends Event<?>>, List<MicroService>> events;

	private final Map<Event<?>, Future<?>> eventFuture = new ConcurrentHashMap<>();

	private MessageBusImpl() {
		microServices = new ConcurrentHashMap<>();
		broadcasts = new ConcurrentHashMap<>();
		events = new ConcurrentHashMap<>();
	}

	public static synchronized MessageBusImpl getInstance() {
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
			events.put(type,new LinkedList<>());
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
			broadcasts.put(type,new LinkedList<>());
			broadcasts.get(type).add(m);
		}
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		synchronized (eventFuture) {
			Future<T> future = (Future<T>) eventFuture.get(e);
		if (future != null) {
			future.resolve(result);
			eventFuture.remove(e);
		}
			}

	}

	@Override
	public void sendBroadcast(Broadcast b) {
		List<MicroService> microServiceList = broadcasts.get(b);
		synchronized (microServiceList) {
			for (MicroService m : microServiceList) {
				addMsg(m, b);
			}
		}
	}

	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		List<MicroService> microServiceList = events.get(e.getClass());
		synchronized (microServiceList) {
			if (microServiceList.isEmpty()) { //if nobody subscribe this event
				return null;
			}

			//TODO: לממש את הרובין פשיין כדי לבחור למי מהמיקרוסרוויסים להביא אתת הevent

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

		for(List<MicroService> msList : broadcasts.values()){ //Remove from Broadcasts Map, if in
			synchronized (msList){
				msList.remove(m);
			}
		}

		for(List<MicroService> msList : events.values()){ //Remove from Events Map, if in
			synchronized (msList){
				msList.remove(m);
			}
		}
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		try {
			while (microServices.get(m).isEmpty()) {
				wait();
			}
		}
		catch (InterruptedException e){
			Thread.currentThread().interrupt();
		}

		return microServices.get(m).remove();
	}

	private void addMsg(MicroService m, Message msg) {
		microServices.get(m).add(msg);
		notifyAll();
	}
	

}
