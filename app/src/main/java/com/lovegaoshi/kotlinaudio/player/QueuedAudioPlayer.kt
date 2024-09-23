@file: OptIn(UnstableApi::class) package com.lovegaoshi.kotlinaudio.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.IllegalSeekPositionException
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import com.lovegaoshi.kotlinaudio.models.*
import java.util.*
import kotlin.math.max
import kotlin.math.min

class QueuedAudioPlayer(
    context: Context,
    options: PlayerOptions = PlayerOptions()
) : AudioPlayer(context, options) {
    private val queue = LinkedList<MediaItem>()

    val currentIndex
        get() = exoPlayer.currentMediaItemIndex

    override val currentItem: AudioItem?
        get() = MediaItem2AudioItem(queue.getOrNull(currentIndex))

    val nextIndex: Int?
        get() {
            return if (exoPlayer.nextMediaItemIndex == C.INDEX_UNSET) null
            else exoPlayer.nextMediaItemIndex
        }

    val previousIndex: Int?
        get() {
            return if (exoPlayer.previousMediaItemIndex == C.INDEX_UNSET) null
            else exoPlayer.previousMediaItemIndex
        }

    val items: List<AudioItem>
        get() = queue.map { MediaItem2AudioItem(it)!! }

    val previousItems: List<AudioItem>
        get() {
            return if (queue.isEmpty()) emptyList()
            else queue
                .subList(0, exoPlayer.currentMediaItemIndex)
                .map { MediaItem2AudioItem(it)!! }
        }

    val nextItems: List<AudioItem>
        get() {
            return if (queue.isEmpty()) emptyList()
            else queue
                .subList(exoPlayer.currentMediaItemIndex, queue.lastIndex)
                .map { MediaItem2AudioItem(it)!! }
        }

    val nextItem: AudioItem?
        get() = items.getOrNull(currentIndex + 1)

    val previousItem: AudioItem?
        get() = items.getOrNull(currentIndex - 1)

    override fun load(item: AudioItem, playWhenReady: Boolean) {
        load(item)
        exoPlayer.playWhenReady = playWhenReady
    }

    override fun load(item: AudioItem) {
        if (queue.isEmpty()) {
            add(item)
        } else {
            exoPlayer.addMediaItem(currentIndex + 1, AudioItem2MediaItem(item))
            exoPlayer.removeMediaItem(currentIndex)
            exoPlayer.seekTo(currentIndex, C.TIME_UNSET);
            exoPlayer.prepare()
        }
    }

    /**
     * Add a single item to the queue. If the AudioPlayer has no item loaded, it will load the `item`.
     * @param item The [AudioItem] to add.
     */
    fun add(item: AudioItem, playWhenReady: Boolean) {
        exoPlayer.playWhenReady = playWhenReady
        add(item)
    }

    /**
     * Add a single item to the queue. If the AudioPlayer has no item loaded, it will load the `item`.
     * @param item The [AudioItem] to add.
     * @param playWhenReady Whether playback starts automatically.
     */
    fun add(item: AudioItem) {
        val mediaSource = AudioItem2MediaItem(item)
        queue.add(mediaSource)
        exoPlayer.addMediaItem(mediaSource)
        exoPlayer.prepare()
    }

    /**
     * Add multiple items to the queue. If the AudioPlayer has no item loaded, it will load the first item in the list.
     * @param items The [AudioItem]s to add.
     * @param playWhenReady Whether playback starts automatically.
     */
    fun add(items: List<AudioItem>, playWhenReady: Boolean) {
        exoPlayer.playWhenReady = playWhenReady
        add(items)
    }

    /**
     * Add multiple items to the queue. If the AudioPlayer has no item loaded, it will load the first item in the list.
     * @param items The [AudioItem]s to add.
     */
    fun add(items: List<AudioItem>) {
        val mediaSources = items.map { AudioItem2MediaItem(it) }
        queue.addAll(mediaSources)
        exoPlayer.addMediaItems(mediaSources)
        exoPlayer.prepare()
    }


    /**
     * Add multiple items to the queue.
     * @param items The [AudioItem]s to add.
     * @param atIndex  Index to insert items at, if no items loaded this will not automatically start playback.
     */
    fun add(items: List<AudioItem>, atIndex: Int) {
        val mediaSources = items.map { AudioItem2MediaItem(it) }
        queue.addAll(atIndex, mediaSources)
        exoPlayer.addMediaItems(atIndex, mediaSources)
        exoPlayer.prepare()
    }

    /**
     * Remove an item from the queue.
     * @param index The index of the item to remove.
     */
    fun remove(index: Int) {
        queue.removeAt(index)
        exoPlayer.removeMediaItem(index)
    }

    /**
     * Remove items from the queue.
     * @param indexes The indexes of the items to remove.
     */
    fun remove(indexes: List<Int>) {
        val sorted = indexes.toMutableList()
        // Sort the indexes in descending order so we can safely remove them one by one
        // without having the next index possibly newly pointing to another item than intended:
        sorted.sortDescending()
        sorted.forEach {
            remove(it)
        }
    }

    /**
     * Skip to the next item in the queue, which may depend on the current repeat mode.
     * Does nothing if there is no next item to skip to.
     */
    fun next() {
        exoPlayer.seekToNextMediaItem()
        exoPlayer.prepare()
    }

    /**
     * Skip to the previous item in the queue, which may depend on the current repeat mode.
     * Does nothing if there is no previous item to skip to.
     */
    fun previous() {
        exoPlayer.seekToPreviousMediaItem()
        exoPlayer.prepare()
    }

    /**
     * Move an item in the queue from one position to another.
     * @param fromIndex The index of the item ot move.
     * @param toIndex The index to move the item to. If the index is larger than the size of the queue, the item is moved to the end of the queue instead.
     */
    fun move(fromIndex: Int, toIndex: Int) {
        exoPlayer.moveMediaItem(fromIndex, toIndex)
        val item = queue[fromIndex]
        queue.removeAt(fromIndex)
        queue.add(max(0, min(items.size, if (toIndex > fromIndex) toIndex else toIndex - 1)), item)
    }

    /**
     * Jump to an item in the queue.
     * @param index the index to jump to
     * @param playWhenReady Whether playback starts automatically.
     */
    fun jumpToItem(index: Int, playWhenReady: Boolean) {
        exoPlayer.playWhenReady = playWhenReady
        jumpToItem(index)
    }

    /**
     * Jump to an item in the queue.
     * @param index the index to jump to
     */
    fun jumpToItem(index: Int) {
        try {
            exoPlayer.seekTo(index, C.TIME_UNSET)
            exoPlayer.prepare()
        } catch (e: IllegalSeekPositionException) {
            throw Error("This item index $index does not exist. The size of the queue is ${queue.size} items.")
        }
    }

    /**
     * Replaces item at index in queue.
     * If updating current index, we update the notification metadata if [automaticallyUpdateNotificationMetadata] is true.
     */
    fun replaceItem(index: Int, item: AudioItem) {
        val mediaSource = AudioItem2MediaItem(item)
        queue[index] = mediaSource
        exoPlayer.replaceMediaItem(index, mediaSource)
    }

    /**
     * Removes all the upcoming items, if any (the ones returned by [next]).
     */
    fun removeUpcomingItems() {
        if (queue.lastIndex == -1 || currentIndex == -1) return
        val lastIndex = queue.lastIndex + 1
        val fromIndex = currentIndex + 1

        exoPlayer.removeMediaItems(fromIndex, lastIndex)
        queue.subList(fromIndex, lastIndex).clear()
    }

    /**
     * Removes all the previous items, if any (the ones returned by [previous]).
     */
    fun removePreviousItems() {
        exoPlayer.removeMediaItems(0, currentIndex)
        queue.subList(0, currentIndex).clear()
    }

    override fun destroy() {
        queue.clear()
        super.destroy()
    }

    override fun clear() {
        queue.clear()
        super.clear()
    }
}
