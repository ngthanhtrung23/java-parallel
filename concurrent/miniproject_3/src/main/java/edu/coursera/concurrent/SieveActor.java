package edu.coursera.concurrent;

import edu.rice.pcdp.Actor;

import java.util.ArrayList;
import java.util.List;

import static edu.rice.pcdp.PCDP.finish;

/**
 * An actor-based implementation of the Sieve of Eratosthenes.
 *
 * TODO Fill in the empty SieveActorActor actor class below and use it from
 * countPrimes to determine the number of primes <= limit.
 */
public final class SieveActor extends Sieve {
    /**
     * {@inheritDoc}
     *
     * TODO Use the SieveActorActor class to calculate the number of primes <=
     * limit in parallel. You might consider how you can model the Sieve of
     * Eratosthenes as a pipeline of actors, each corresponding to a single
     * prime number.
     */
    @Override
    public int countPrimes(final int limit) {
		System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "4");
    	if (limit < 2) return 0;

		SieveActorActor firstActor = new SieveActorActor(2);
    	finish(() -> {
			for (int x = 2; x <= limit; x++) {
				firstActor.send(x);
			}
			firstActor.send(0);
		});
    	return firstActor.countPrimes();
    }

    /**
     * An actor class that helps implement the Sieve of Eratosthenes in
     * parallel.
     */
    public static final class SieveActorActor extends Actor {
    	private SieveActorActor nextActor;
    	private List<Integer> values;

    	SieveActorActor(int value) {
    		values = new ArrayList<>();
    		values.add(value);
		}

		int countPrimes() {
    		if (nextActor == null) {
    			return values.size();
			} else {
    			return values.size() + nextActor.countPrimes();
			}
		}

        /**
         * Process a single message sent to this actor.
         *
         * TODO complete this method.
         *
         * @param msg Received message
         */
        @Override
        public void process(final Object msg) {
            final int x = (Integer) msg;
            if (x <= 0) {
            	if (nextActor != null) {
            		nextActor.send(x);
				}
			} else {
            	boolean isPrimeCandidate = checkPrime(x);
            	if (isPrimeCandidate) {
            		if (this.values.size() < 30) {
            			this.values.add(x);
					} else if (nextActor == null) {
            			nextActor = new SieveActorActor(x);
					} else {
            			nextActor.send(msg);
					}
				}
			}
        }

        private boolean checkPrime(int x) {
        	for (int value : values) {
        		if (x % value == 0) return false;
			}
        	return true;
		}
    }
}
