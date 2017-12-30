/**
 * Created by beepbeat/holladiewal on 05.12.2017.
 */
class WatchableMutableList<E>(val callback : () -> Unit) : MutableList<E> {

    private val internalList = mutableListOf<E>()

    override val size: Int
        get() = internalList.size


    override fun contains(element: E): Boolean {
        return internalList.contains(element)
    }

    override fun containsAll(elements: Collection<E>): Boolean {
        return internalList.containsAll(elements)
    }

    override fun get(index: Int): E {
        callback()
        return internalList.get(index)
    }

    override fun indexOf(element: E): Int {
        return internalList.indexOf(element)
    }

    override fun isEmpty(): Boolean {
        return internalList.isEmpty()
    }

    override fun iterator(): MutableIterator<E> {
        return internalList.iterator()
    }

    override fun lastIndexOf(element: E): Int {
        return internalList.lastIndexOf(element)
    }

    override fun add(element: E): Boolean {
        callback()
        return internalList.add(element)
    }

    override fun add(index: Int, element: E) {
        callback()
        return internalList.add(index, element)
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        callback()
        return internalList.addAll(index, elements)
    }

    override fun addAll(elements: Collection<E>): Boolean {
        callback()
        return internalList.addAll(elements)
    }

    override fun clear() {
        callback()
        internalList.clear()
    }

    override fun listIterator(): MutableListIterator<E> {
        callback()
        return internalList.listIterator()
    }

    override fun listIterator(index: Int): MutableListIterator<E> {
        callback()
        return listIterator(index)
    }

    override fun remove(element: E): Boolean {
        callback()
        return internalList.remove(element)
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        callback()
        return internalList.removeAll(elements)
    }

    override fun removeAt(index: Int): E {
        callback()
        return internalList.removeAt(index)
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        callback()
        return internalList.retainAll(elements)
    }

    override fun set(index: Int, element: E): E {
        callback()
        return set(index, element)
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> {
        callback()
        return internalList.subList(fromIndex, toIndex)
    }
}