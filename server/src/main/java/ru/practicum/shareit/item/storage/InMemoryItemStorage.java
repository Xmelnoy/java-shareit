package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component
public class InMemoryItemStorage implements ItemStorage {
    private final Map<Long, Item> items = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    @Override
    public Item create(Item item) {
        item.setId(idGenerator.incrementAndGet());
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item item) {
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public List<Item> findAllByOwner(Long ownerId) {
        return items.values().stream()
                .filter(item -> item.getOwner() != null && ownerId.equals(item.getOwner().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> searchAvailable(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        String lowerText = text.toLowerCase();
        return items.values().stream()
                .filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(lowerText)
                        || item.getDescription().toLowerCase().contains(lowerText))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        items.remove(id);
    }
}