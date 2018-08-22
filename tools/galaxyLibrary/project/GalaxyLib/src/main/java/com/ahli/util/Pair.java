package com.ahli.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * <p> A convenience class to represent name-value pairs. </p>
 */
@JsonTypeInfo (use = JsonTypeInfo.Id.MINIMAL_CLASS)
@JsonInclude (JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect (fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Pair <K, V> {
	
	/**
	 * Key of this <code>Pair</code>.
	 */
	private K key;
	/**
	 * Value of this this <code>Pair</code>.
	 */
	private V value;
	
	/**
	 * Creates a new pair
	 *
	 * @param key
	 * 		The key for this pair
	 * @param value
	 * 		The value to use for this pair
	 */
	@JsonCreator
	public Pair(@JsonProperty ("key") final K key, @JsonProperty ("value") final V value) {
		this.key = key;
		this.value = value;
	}
	
	/**
	 * Gets the key for this pair.
	 *
	 * @return key for this pair
	 */
	public K getKey() {
		return key;
	}
	
	public void setKey(final K key) {
		this.key = key;
		
	}
	
	/**
	 * Gets the value for this pair.
	 *
	 * @return value for this pair
	 */
	public V getValue() {
		return value;
	}
	
	public void setValue(final V value) {
		this.value = value;
		
	}
	
	/**
	 * <p> <code>String</code> representation of this <code>Pair</code>. </p> <p> The default name/value delimiter '='
	 * is always used. </p>
	 *
	 * @return <code>String</code> representation of this <code>Pair</code>
	 */
	@Override
	public String toString() {
		return key + "=" + value;
	}
	
	/**
	 * <p> Generate a hash code for this <code>Pair</code>. </p> <p> The hash code is calculated using only the key of
	 * the <code>Pair</code>. </p>
	 *
	 * @return hash code for this <code>Pair</code>
	 */
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + (key != null ? key.hashCode() : 0);
		return hash;
	}
	
	/**
	 * <p> Test this <code>Pair</code> for equality with another <code>Object</code>. </p> <p> If the
	 * <code>Object</code> to be tested is not a <code>Pair</code> or is <code>null</code>, then this method returns
	 * <code>false</code>. </p> <p> Two <code>Pair</code>s are considered equal if and only if the key is equal. </p>
	 *
	 * @param o
	 * 		the <code>Object</code> to test for equality with this <code>Pair</code>
	 * @return <code>true</code> if the given <code>Object</code> is equal to this <code>Pair</code> else
	 * 		<code>false</code>
	 */
	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof Pair) {
			final Pair<?, ?> pair = (Pair<?, ?>) o;
			return key != null ? key.equals(pair.key) : pair.key == null;
		}
		return false;
	}
}
