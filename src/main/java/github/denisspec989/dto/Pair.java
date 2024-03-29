package github.denisspec989.dto;

import lombok.*;

import java.io.Serializable;
@Getter
@Setter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Pair<K,V> implements Serializable {
    private final K key;
    private final V value;
}
