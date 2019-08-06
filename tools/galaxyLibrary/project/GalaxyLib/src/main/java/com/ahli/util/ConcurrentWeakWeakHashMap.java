// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com
package com.ahli.util;

/*
 * Copyright Terracotta, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.AbstractMap.SimpleEntry;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Source: https://github.com/ehcache/ehcache3/blob/master/core/src/main/java/org/ehcache/core/collections/ConcurrentWeakIdentityHashMap.java
 *
 * @author Alex Snaps, modified by Ahli
 */
public class ConcurrentWeakWeakHashMap <K> implements ConcurrentMap<K, K> {
	
	private final ConcurrentMap<WeakReferenceWithHash<K>, WeakReferenceWithHash<K>> map;
	private final ReferenceQueue<K> queue = new ReferenceQueue<>();
	
	public ConcurrentWeakWeakHashMap() {
		this(8, 0.9F, 1);
	}
	
	public ConcurrentWeakWeakHashMap(final int initialCapacity, final float loadFactor, final int concurrencyLevel) {
		map = new ConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel);
	}
	
	@Override
	public K putIfAbsent(final K key, final K value) {
		purgeKeys();
		final var weakRef = newKey(key);
		final var result = map.putIfAbsent(weakRef, weakRef);
		return result == null ? null : result.get();
	}
	
	/**
	 * Removes GC-collected keys.
	 */
	public void purgeKeys() {
		Reference<? extends K> reference;
		while ((reference = queue.poll()) != null) {
			map.remove(reference);
		}
	}
	
	private WeakReferenceWithHash<K> newKey(final K key) {
		return new WeakReferenceWithHash<>(key, queue);
	}
	
	@Override
	public boolean remove(final Object key, final Object value) {
		purgeKeys();
		return map.remove(new WeakReferenceWithHash<>(key, null), key);
	}
	
	@Override
	public boolean replace(final K key, final K oldValue, final K newValue) {
		purgeKeys();
		final var weakKeyRefKey = newKey(key);
		return map.replace(weakKeyRefKey, weakKeyRefKey, weakKeyRefKey);
	}
	
	@Override
	public K replace(final K key, final K value) {
		purgeKeys();
		final var weakKeyRef = newKey(key);
		final var result = map.replace(weakKeyRef, weakKeyRef);
		return result == null ? null : result.get();
	}
	
	@Override
	public int size() {
		purgeKeys();
		return map.size();
	}
	
	@Override
	public boolean isEmpty() {
		purgeKeys();
		return map.isEmpty();
	}
	
	@Override
	public boolean containsValue(final Object value) {
		purgeKeys();
		return map.containsValue(newKeyByObj(value));
	}
	
	private WeakReferenceWithHash newKeyByObj(final Object obj) {
		return new WeakReferenceWithHash(obj, queue);
	}
	
	@Override
	public K get(final Object key) {
		purgeKeys();
		final var result = map.get(new WeakReferenceWithHash<>(key, null));
		return result == null ? null : result.get();
	}
	
	@Override
	public K put(final K key, final K value) {
		purgeKeys();
		final var keyWeakRef = newKey(key);
		final var result = map.put(keyWeakRef, keyWeakRef);
		return result == null ? null : result.get();
	}
	
	@Override
	public K remove(final Object key) {
		purgeKeys();
		final var result = map.remove(new WeakReferenceWithHash<>(key, null));
		return result == null ? null : result.get();
	}
	
	@Override
	public void putAll(final Map<? extends K, ? extends K> m) {
		purgeKeys();
		for (final Entry<? extends K, ? extends K> entry : m.entrySet()) {
			final var keyWeakRef = newKey(entry.getKey());
			map.put(keyWeakRef, keyWeakRef);
		}
	}
	
	@Override
	public void clear() {
		purgeKeys();
		map.clear();
	}
	
	@Override
	public Set<K> keySet() {
		return new AbstractSet<>() {
			@Override
			public Iterator<K> iterator() {
				purgeKeys();
				return new WeakSafeIterator<>(map.keySet().iterator()) {
					@Override
					protected K extract(final WeakReferenceWithHash<K> u) {
						return u.get();
					}
				};
			}
			
			@Override
			public boolean contains(final Object o) {
				return containsKey(o);
			}
			
			@Override
			public int size() {
				return map.size();
			}
		};
	}
	
	@Override
	public boolean containsKey(final Object key) {
		purgeKeys();
		return map.containsKey(new WeakReferenceWithHash<>(key, null));
	}
	
	@Override
	public Collection<K> values() {
		purgeKeys();
		final var values = map.values();
		final Collection<K> coll = new ArrayList<>(values.size());
		for (final var value : values) {
			coll.add(value.get());
		}
		return coll;
	}
	
	@Override
	public Set<Entry<K, K>> entrySet() {
		return new AbstractSet<>() {
			@Override
			public Iterator<Entry<K, K>> iterator() {
				purgeKeys();
				return new WeakSafeIterator<>(map.entrySet().iterator()) {
					@Override
					protected Entry<K, K> extract(final Entry<WeakReferenceWithHash<K>, WeakReferenceWithHash<K>> u) {
						final K key = u.getKey().get();
						if (key == null) {
							return null;
						} else {
							return new SimpleEntry<>(key, u.getValue().get());
						}
					}
				};
			}
			
			@Override
			public int size() {
				return map.size();
			}
		};
	}
	
	private static final class WeakReferenceWithHash <T> extends java.lang.ref.WeakReference<T> {
		
		private final int hashCode;
		
		private WeakReferenceWithHash(final T referent, final ReferenceQueue<? super T> q) {
			super(referent, q);
			hashCode = referent.hashCode();
		}
		
		@Override
		public boolean equals(final Object obj) {
			return obj != null && obj.getClass() == getClass() &&
					((this == obj || super.get() == ((WeakReferenceWithHash) obj).get()) ||
							(hashCode == ((WeakReferenceWithHash) obj).hashCode &&
									Objects.equals(super.get(), ((WeakReferenceWithHash) obj).get())));
		}
		
		@Override
		public int hashCode() {
			return hashCode;
		}
	}
	
	private abstract static class WeakSafeIterator <T, U> implements Iterator<T> {
		
		private final Iterator<U> weakIterator;
		protected T strongNext;
		
		public WeakSafeIterator(final Iterator<U> weakIterator) {
			this.weakIterator = weakIterator;
			advance();
		}
		
		private void advance() {
			while (weakIterator.hasNext()) {
				final U nextU = weakIterator.next();
				if ((strongNext = extract(nextU)) != null) {
					return;
				}
			}
			strongNext = null;
		}
		
		protected abstract T extract(U u);
		
		@Override
		public boolean hasNext() {
			return strongNext != null;
		}
		
		@Override
		public final T next() {
			final T next = strongNext;
			advance();
			return next;
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
