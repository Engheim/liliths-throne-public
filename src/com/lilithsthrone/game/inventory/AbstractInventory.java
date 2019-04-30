package com.lilithsthrone.game.inventory;

import java.util.*;
import java.util.function.Function;

/**
 * AbstractInventory serves to deduplicate code between the handling of Item, Clothing and Weapon in CharacterInventory.
 *
 * Package-privateness is a feature in that case.
 * @param <T>
 */
class AbstractInventory<T extends AbstractCoreItem, U extends AbstractCoreType> {
	private final Comparator<T> comparator;
	private final Function<T, U> typeRetriever;
	private Map<T, Integer> duplicateCounts;

	AbstractInventory(Comparator<T> comparator, Function<T, U> typeRetriever) {
		this.comparator = comparator;
		this.typeRetriever = typeRetriever;
		duplicateCounts = new LinkedHashMap<>();
	}

	public void clear() {
		duplicateCounts.clear();
	}

	boolean isEmpty() {
		return duplicateCounts.isEmpty();
	}

	public void sort() {
		if (duplicateCounts.size() < 2) {
			return;
		}
		List<T> weaponsToSort = new ArrayList<>(duplicateCounts.keySet());
		weaponsToSort.sort(comparator);

		Map<T, Integer> newlySortedMap = new LinkedHashMap<>();
		for(T w : weaponsToSort) {
			newlySortedMap.put(w, duplicateCounts.get(w));
		}
		duplicateCounts = newlySortedMap;
	}

	/**
	 * @return a Non-modifiable Map
	 */
	Map<T, Integer> getDuplicateCounts() {
		return Collections.unmodifiableMap(duplicateCounts);
	}

	int getQuestEntryCount() {
		return (int) duplicateCounts.keySet().stream().filter(e -> e.getRarity().equals(Rarity.QUEST)).count();
	}

	public int getTotalItemCount() {
		return duplicateCounts.values().stream().mapToInt(e -> e).sum();
	}

	public int getItemCount(T item) {
		return duplicateCounts.getOrDefault(item, 0);
	}

	void addFromMap(Map<T, Integer> map) {
		map.forEach(this::addItem);
	}

	void addItem(T item, int count) {
		duplicateCounts.merge(item, count, Integer::sum);
	}

	boolean hasItem(T item) {
		return duplicateCounts.containsKey(item);
	}

	private boolean removeItem(T item) {
		return removeItem(item, 1);
	}

	boolean removeItem(T item, int count) {
		return duplicateCounts.computeIfPresent(item, (ignoredKey, currentCount) -> {
			int newValue = currentCount - count;
			return newValue > 0 ? newValue : null; //removes from map if 0 or less items left
		}) != null;
	}

	private Optional<T> getItemByType(U type) {
		return duplicateCounts.keySet().stream().filter(item -> typeRetriever.apply(item).equals(type)).findAny();
	}

	boolean hasItemType(U itemType) {
		return getItemByType(itemType).isPresent();
	}

	boolean removeItemByType(U itemType) {
		return getItemByType(itemType).map(this::removeItem).orElse(false);
	}
}
