package interfaces;

import java.util.Set;

public interface Storage<K,V> {
    Set<K> getKeys();
    V remove(K key);
    V listValues(K key);
    void addValues(K key, V value);
    void removeValues(K key, V value);
}
