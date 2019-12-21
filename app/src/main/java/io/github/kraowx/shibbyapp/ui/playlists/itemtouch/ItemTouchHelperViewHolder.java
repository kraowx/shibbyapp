package io.github.kraowx.shibbyapp.ui.playlists.itemtouch;

public interface ItemTouchHelperViewHolder
{
	/**
	 * Called when the {@link ItemTouchHelper} first registers an item as being moved or swiped.
	 * Implementations should update the item view to indicate it's active state.
	 */
	void onItemSelected();
	
	/**
	 * Called when the {@link ItemTouchHelper} has completed the move or swipe, and the active item
	 * state should be cleared.
	 */
	void onItemClear();
}
